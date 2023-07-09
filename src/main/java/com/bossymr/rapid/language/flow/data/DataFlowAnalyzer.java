package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.ReferenceValue;
import com.bossymr.rapid.language.flow.value.VariableReference;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class DataFlowAnalyzer {

    private final @NotNull Block.FunctionBlock functionBlock;
    private final @NotNull List<ReferenceValue> variables;

    private final @NotNull Map<BasicBlock, DataFlowBlock> blocks;
    private final @NotNull DataFlowFunctionMap functionMap;
    private final @NotNull Deque<DataFlowBlock> workList;

    public DataFlowAnalyzer(@NotNull Block.FunctionBlock functionBlock, @NotNull DataFlowFunctionMap functionMap) {
        this(functionBlock, functionMap, functionBlock.getBasicBlocks());
    }

    public DataFlowAnalyzer(@NotNull Block.FunctionBlock functionBlock, @NotNull DataFlowFunctionMap functionMap, @NotNull Collection<BasicBlock> blocks) {
        this.functionBlock = functionBlock;
        this.functionMap = functionMap;
        List<ReferenceValue> variables = new ArrayList<>();
        for (ArgumentGroup argumentGroup : functionBlock.getArgumentGroups()) {
            for (Argument argument : argumentGroup.arguments()) {
                variables.add(new VariableReference(argument));
            }
        }
        for (Variable variable : functionBlock.getVariables()) {
            variables.add(new VariableReference(variable));
        }
        this.variables = List.copyOf(variables);
        List<BasicBlock> basicBlocks = functionBlock.getBasicBlocks();
        this.workList = new ArrayDeque<>(basicBlocks.size());
        Map<BasicBlock, DataFlowBlock> blockMap = new HashMap<>();
        for (BasicBlock basicBlock : basicBlocks) {
            DataFlowBlock block = new DataFlowBlock(basicBlock, new HashSet<>(1), new HashSet<>(1), new HashSet<>(1));
            blockMap.put(basicBlock, block);
        }
        for (BasicBlock block : blocks) {
            workList.addLast(blockMap.get(block));
        }
        this.blocks = Map.copyOf(blockMap);
    }

    private @NotNull Map<ReferenceValue, Constraint> getConstraints(@NotNull DataFlowBlock block) {
        return variables.stream().collect(Collectors.toMap(value -> value, block::getConstraint));
    }

    public void process() {
        while (!(workList.isEmpty())) {
            DataFlowBlock block = workList.removeFirst();
            Set<DataFlowEdge> successors = block.successors();
            Map<ReferenceValue, Constraint> before = getConstraints(block);
            process(block);
            Map<ReferenceValue, Constraint> after = getConstraints(block);
            boolean modified = variables.stream().anyMatch(variable -> !(before.get(variable).contains(after.get(variable)))) || !(successors.equals(block.successors()));
            if (modified) {
                for (DataFlowEdge successor : block.successors()) {
                    workList.add(successor.block());
                }
            }
        }
    }

    private void process(@NotNull DataFlowBlock block) {
        block.states().clear();
        for (DataFlowEdge successor : block.successors()) {
            successor.block().predecessors().remove(successor);
        }
        for (DataFlowEdge predecessors : block.predecessors()) {
            block.states().addAll(predecessors.states());
        }
        BasicBlock basicBlock = block.basicBlock();
        Map<Map<Argument, Constraint>, Set<DataFlowFunction.Result>> results = new HashMap<>();
        DataFlowAnalyzerVisitor visitor = new DataFlowAnalyzerVisitor(functionBlock, block, blocks, functionMap);
        for (LinearInstruction instruction : basicBlock.getInstructions()) {
            instruction.accept(visitor);
        }
        BranchingInstruction terminator = basicBlock.getTerminator();
        terminator.accept(visitor);
        for (DataFlowEdge successor : block.successors()) {
            successor.block().predecessors().add(successor);
        }

    }
}
