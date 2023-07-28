package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.ComponentValue;
import com.bossymr.rapid.language.flow.value.ReferenceValue;
import com.bossymr.rapid.language.flow.value.VariableValue;
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

    public DataFlowAnalyzer(@NotNull Block.FunctionBlock functionBlock, @NotNull DataFlowFunctionMap functionMap, @NotNull Map<BasicBlock, DataFlowBlock> blocks, @NotNull Deque<DataFlowBlock> workList) {
        this.functionBlock = functionBlock;
        this.functionMap = functionMap;
        this.variables = getVariables(functionBlock);
        this.workList = workList;
        this.blocks = blocks;
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

    public void process() {
        while (!(workList.isEmpty())) {
            DataFlowBlock block = workList.removeFirst();
            Set<DataFlowEdge> successors = Set.copyOf(block.getSuccessors());
            List<DataFlowState> before = List.copyOf(block.getStates());
            process(block);
            List<DataFlowState> after = block.getStates();
            boolean modified = isModified(before, after) || !(successors.equals(block.getSuccessors()));
            if (modified) {
                for (DataFlowEdge successor : block.getSuccessors()) {
                    DataFlowBlock successorBlock = successor.getBlock();
                    if (workList.contains(successorBlock)) {
                        continue;
                    }
                    workList.add(successorBlock);
                }
            }
        }
    }

    private boolean isModified(@NotNull List<DataFlowState> before, @NotNull List<DataFlowState> after) {
        if (before.isEmpty() || after.isEmpty()) {
            return !(before.isEmpty() && after.isEmpty());
        }
        if (before.equals(after)) {
            return false;
        }
        for (ReferenceValue variable : variables) {
            RapidType type = variable.getType();
            if (type.getDimensions() == 0 && !(type.getStructure() instanceof RapidRecord)) {
                if (isModified(before, after, variable)) {
                    return true;
                }
            }
            if (type.getStructure() instanceof RapidRecord record) {
                for (RapidComponent component : record.getComponents()) {
                    String name = component.getName();
                    RapidType componentType = component.getType();
                    if (componentType == null || name == null) {
                        continue;
                    }
                    if (isModified(before, after, new ComponentValue(componentType, variable, name))) {
                        return true;
                    }
                }
            }
            if (type.getDimensions() > 0) {
                Map<Constraint, Constraint> beforeMap = getIndexConstraints(variable, before);
                Map<Constraint, Constraint> afterMap = getIndexConstraints(variable, after);
                for (Constraint afterConstraint : afterMap.keySet()) {
                    for (Constraint beforeConstraint : beforeMap.keySet()) {
                        if (afterConstraint.intersects(beforeConstraint)) {
                            if (!(afterMap.get(afterConstraint).contains(beforeMap.get(beforeConstraint)))) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private @NotNull Map<Constraint, Constraint> getIndexConstraints(@NotNull ReferenceValue referenceValue, @NotNull List<DataFlowState> states) {
        Map<Constraint, Constraint> result = new HashMap<>();
        for (DataFlowState state : states) {
            Map<Constraint, Constraint> indexConstraint = state.getIndexConstraint(referenceValue);
            for (Constraint constraint : indexConstraint.keySet()) {
                result.replaceAll((key, value) -> {
                    if (constraint.contains(key)) {
                        return indexConstraint.get(constraint).and(value);
                    } else if (constraint.intersects(key)) {
                        return indexConstraint.get(constraint).or(value);
                    }
                    return value;
                });
                if (!(result.containsKey(constraint))) {
                    result.put(constraint, indexConstraint.get(constraint));
                }
            }
        }
        return result;
    }

    private boolean isModified(@NotNull List<DataFlowState> before, @NotNull List<DataFlowState> after, @NotNull ReferenceValue variable) {
        Constraint beforeConstraint = DataFlowBlock.getConstraint(before, variable);
        Constraint afterConstraint = DataFlowBlock.getConstraint(after, variable);
        return !(beforeConstraint.contains(afterConstraint));
    }

    private void process(@NotNull DataFlowBlock block) {
        block.getStates().clear();
        for (DataFlowEdge successor : block.getSuccessors()) {
            successor.getBlock().getPredecessors().removeIf(edge -> edge.getBlock().equals(block));
        }
        block.getSuccessors().clear();
        for (DataFlowEdge predecessors : block.getPredecessors()) {
            block.getStates().addAll(predecessors.getStates());
        }
        BasicBlock basicBlock = block.getBasicBlock();
        DataFlowAnalyzerVisitor visitor = new DataFlowAnalyzerVisitor(functionBlock, block, blocks, functionMap);
        if (block.getStates().isEmpty()) {
            block.getStates().add(DataFlowState.createFull(functionBlock));
        }
        for (LinearInstruction instruction : basicBlock.getInstructions()) {
            instruction.accept(visitor);
        }
        BranchingInstruction terminator = basicBlock.getTerminator();
        terminator.accept(visitor);
        for (DataFlowEdge successor : block.getSuccessors()) {
            successor.getBlock().getPredecessors().add(new DataFlowEdge(block, successor.getStates()));
        }
    }
}
