package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.constraint.Optionality;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.stream.Collectors;

public class DataFlowAnalyzer {

    private final @NotNull Block.FunctionBlock functionBlock;
    private final @NotNull List<ReferenceValue> variables;

    private final @NotNull Map<BasicBlock, DataFlowBlock> blocks;
    private final @NotNull DataFlowFunctionMap functionMap;
    private final @NotNull Deque<DataFlowBlock> workList;

    private final @NotNull DataFlowState defaultState;

    public DataFlowAnalyzer(@NotNull Block.FunctionBlock functionBlock, @NotNull DataFlowFunctionMap functionMap, @NotNull Map<BasicBlock, DataFlowBlock> blocks, @NotNull Deque<DataFlowBlock> workList) {
        this.functionBlock = functionBlock;
        this.functionMap = functionMap;
        this.variables = getVariables(functionBlock);
        this.workList = workList;
        this.blocks = blocks;
        this.defaultState = createDefaultState();
    }

    public static @NotNull Map<BasicBlock, DataFlowBlock> analyze(@NotNull Block.FunctionBlock functionBlock, @NotNull DataFlowFunctionMap functionMap) {
        List<BasicBlock> basicBlocks = functionBlock.getBasicBlocks();
        Map<BasicBlock, DataFlowBlock> blocks = basicBlocks.stream().collect(Collectors.toMap(block -> block, DataFlowBlock::new));
        Deque<DataFlowBlock> workList = new ArrayDeque<>(basicBlocks.size());
        for (BasicBlock basicBlock : basicBlocks) {
            workList.add(blocks.get(basicBlock));
        }
        DataFlowAnalyzer analyzer = new DataFlowAnalyzer(functionBlock, functionMap, blocks, workList);
        analyzer.process();
        return blocks;
    }

    public static void reanalyze(@NotNull Block.FunctionBlock functionBlock, @NotNull DataFlowFunctionMap functionMap, @NotNull Map<BasicBlock, DataFlowBlock> blocks, @NotNull Set<DataFlowBlock> workList) {
        Deque<DataFlowBlock> deque = new ArrayDeque<>(workList);
        DataFlowAnalyzer analyzer = new DataFlowAnalyzer(functionBlock, functionMap, blocks, deque);
        analyzer.process();
    }

    private static @NotNull @Unmodifiable List<ReferenceValue> getVariables(@NotNull Block.FunctionBlock functionBlock) {
        List<ReferenceValue> variables = new ArrayList<>();
        for (ArgumentGroup argumentGroup : functionBlock.getArgumentGroups()) {
            for (Argument argument : argumentGroup.arguments()) {
                variables.add(new VariableValue(argument));
            }
        }
        for (Variable variable : functionBlock.getVariables()) {
            variables.add(new VariableValue(variable));
        }
        return List.copyOf(variables);
    }

    private @NotNull Map<ReferenceValue, Constraint> getConstraints(@NotNull DataFlowBlock block) {
        return variables.stream().collect(Collectors.toMap(value -> value, block::getConstraint));
    }

    public void process() {
        while (!(workList.isEmpty())) {
            DataFlowBlock block = workList.removeFirst();
            Set<DataFlowEdge> successors = block.getSuccessors();
            Map<ReferenceValue, Constraint> before = getConstraints(block);
            process(block);
            Map<ReferenceValue, Constraint> after = getConstraints(block);
            boolean modified = variables.stream().anyMatch(variable -> !(before.get(variable).contains(after.get(variable)))) || !(successors.equals(block.getSuccessors()));
            if (modified) {
                for (DataFlowEdge successor : block.getSuccessors()) {
                    workList.add(successor.getBlock());
                }
            }
        }
    }

    private void process(@NotNull DataFlowBlock block) {
        block.getStates().clear();
        for (DataFlowEdge successor : block.getSuccessors()) {
            successor.getBlock().getPredecessors().remove(successor);
        }
        for (DataFlowEdge predecessors : block.getPredecessors()) {
            block.getStates().addAll(predecessors.getStates());
        }
        BasicBlock basicBlock = block.getBasicBlock();
        DataFlowAnalyzerVisitor visitor = new DataFlowAnalyzerVisitor(functionBlock, block, blocks, functionMap);
        block.getStates().add(new DataFlowState(defaultState));
        for (LinearInstruction instruction : basicBlock.getInstructions()) {
            instruction.accept(visitor);
        }
        BranchingInstruction terminator = basicBlock.getTerminator();
        terminator.accept(visitor);
        for (DataFlowEdge successor : block.getSuccessors()) {
            successor.getBlock().getPredecessors().add(successor);
        }
    }

    private @NotNull DataFlowState createDefaultState() {
        DataFlowState state = new DataFlowState(functionBlock);
        for (Variable variable : functionBlock.getVariables()) {
            initializeVariable(state, new VariableValue(variable));
        }
        for (ArgumentGroup argumentGroup : functionBlock.getArgumentGroups()) {
            for (Argument argument : argumentGroup.arguments()) {
                Optionality optionality = argumentGroup.isOptional() ? Optionality.UNKNOWN : Optionality.PRESENT;
                state.assign(new VariableValue(argument), Constraint.any(argument.type(), optionality));
            }
        }
        return state;
    }

    private void initializeVariable(@NotNull DataFlowState state, @NotNull ReferenceValue value) {
        RapidType type = value.getType();
        if (type.isAssignable(RapidType.NUMBER) || type.isAssignable(RapidType.DOUBLE)) {
            state.assign(value, Expression.numericConstant(0));
        } else if (type.isAssignable(RapidType.BOOLEAN)) {
            state.assign(value, Expression.booleanConstant(false));
        } else if (type.isAssignable(RapidType.STRING)) {
            state.assign(value, Expression.stringConstant(""));
        } else if (type.getDimensions() > 0) {
            VariableSnapshot snapshot = new VariableSnapshot(RapidType.NUMBER);
            state.assign(snapshot, Constraint.any(RapidType.NUMBER));
            IndexValue indexValue = new IndexValue(value, snapshot);
            state.assign(indexValue, Constraint.any(indexValue.getType()));
            if (type.getDimensions() > 1) {
                initializeVariable(state, indexValue);
            }
        } else if (type.getTargetStructure() instanceof RapidRecord record) {
            for (RapidComponent component : record.getComponents()) {
                RapidType componentType = component.getType();
                Objects.requireNonNull(componentType);
                String componentName = component.getName();
                Objects.requireNonNull(componentName);
                initializeVariable(state, new ComponentValue(componentType, value, componentName));
            }
        } else {
            state.assign(value, Constraint.any(value.getType()));
        }
    }
}
