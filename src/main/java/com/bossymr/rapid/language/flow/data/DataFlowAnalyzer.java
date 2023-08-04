package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.data.block.DataFlowEdge;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.data.snapshots.ArrayEntry;
import com.bossymr.rapid.language.flow.data.snapshots.ArraySnapshot;
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

    public static void reanalyze(@NotNull DataFlowBlock block, @NotNull DataFlowFunctionMap functionMap, @NotNull Map<BasicBlock, DataFlowBlock> blocks) {
        Deque<DataFlowBlock> deque = new ArrayDeque<>();
        deque.addLast(block);
        Block.FunctionBlock functionBlock = (Block.FunctionBlock) block.getBasicBlock().getBlock();
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
            boolean modified = isModified(before, after) || isModified(successors, block.getSuccessors());
            if (modified) {
                for (DataFlowEdge successor : block.getSuccessors()) {
                    DataFlowBlock successorBlock = successor.getDestination();
                    if (workList.contains(successorBlock)) {
                        continue;
                    }
                    workList.add(successorBlock);
                }
            } else {
                for (DataFlowEdge afterEdge : block.getSuccessors()) {
                    for (DataFlowEdge beforeEdge : successors) {
                        if (!(isModified(beforeEdge, afterEdge))) {
                            afterEdge.setLatest(beforeEdge.getLatest());
                            break;
                        }
                    }
                }
            }
        }
    }

    private boolean isModified(@NotNull Set<DataFlowEdge> before, @NotNull Set<DataFlowEdge> after) {
        if (before.size() != after.size()) {
            return true;
        }
        if (before.equals(after)) {
            return false;
        }
        List<DataFlowEdge> beforeList = List.copyOf(before);
        List<DataFlowEdge> afterList = List.copyOf(after);
        for (int i = 0; i < beforeList.size(); i++) {
            DataFlowEdge beforeEdge = beforeList.get(i);
            DataFlowEdge afterEdge = afterList.get(i);
            if (isModified(beforeEdge, afterEdge)) {
                return true;
            }
        }
        return false;
    }

    private boolean isModified(@NotNull DataFlowEdge beforeEdge, @NotNull DataFlowEdge afterEdge) {
        if (!(beforeEdge.getSource().equals(afterEdge.getSource()))) {
            return true;
        }
        if (!(beforeEdge.getDestination().equals(afterEdge.getDestination()))) {
            return true;
        }
        return isModified(beforeEdge.getStates(), afterEdge.getStates());
    }

    private boolean isModified(@NotNull List<DataFlowState> before, @NotNull List<DataFlowState> after) {
        if (before.equals(after)) {
            return false;
        }
        if (before.isEmpty() || after.isEmpty()) {
            return true;
        }
        for (ReferenceValue variable : variables) {
            RapidType type = variable.getType();
            if (type.getDimensions() > 0) {
                if (isArrayModified(before, after, variable)) {
                    return true;
                }
            } else if (type.getTargetStructure() instanceof RapidRecord record) {
                for (RapidComponent component : record.getComponents()) {
                    String name = component.getName();
                    RapidType componentType = component.getType();
                    if (componentType == null || name == null) {
                        continue;
                    }
                    ComponentValue componentValue = new ComponentValue(componentType, variable, name);
                    if (isModified(before, after, componentValue)) {
                        return true;
                    }
                }
            } else {
                if (isModified(before, after, variable)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isArrayModified(@NotNull List<DataFlowState> before, @NotNull List<DataFlowState> after, @NotNull ReferenceValue variable) {
        Map<List<Constraint>, Constraint> beforeConstraints = getArrayAssignments(before, variable);
        Map<List<Constraint>, Constraint> afterConstraints = getArrayAssignments(after, variable);
        for (List<Constraint> beforeIndexes : beforeConstraints.keySet()) {
            for (List<Constraint> afterIndexes : afterConstraints.keySet()) {
                if (contains(beforeIndexes, afterIndexes)) {
                    Constraint beforeResult = beforeConstraints.get(beforeIndexes);
                    Constraint afterResult = afterConstraints.get(afterIndexes);
                    if (!(beforeResult.contains(afterResult))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean contains(@NotNull List<Constraint> before, @NotNull List<Constraint> after) {
        for (int i = 0; i < before.size(); i++) {
            Constraint beforeConstraint = before.get(i);
            Constraint afterConstraint = after.get(i);
            if (!(beforeConstraint.intersects(afterConstraint))) {
                return false;
            }
        }
        return true;
    }

    private @NotNull Map<List<Constraint>, Constraint> getArrayAssignments(@NotNull List<DataFlowState> states, @NotNull ReferenceValue variable) {
        Map<List<Constraint>, Constraint> results = new HashMap<>();
        for (DataFlowState state : states) {
            getArrayAssignments(results, new ArrayList<>(), state, variable);
        }
        return results;
    }

    private void getArrayAssignments(@NotNull Map<List<Constraint>, Constraint> results, @NotNull List<Constraint> indexes, @NotNull DataFlowState state, @NotNull ReferenceValue variable) {
        if (variable.getType().getDimensions() > 0) {
            ArraySnapshot arraySnapshot = (ArraySnapshot) state.getSnapshot(variable);
            for (ArrayEntry entry : arraySnapshot.getAssignments(state, Constraint.any(RapidType.NUMBER))) {
                if (entry instanceof ArrayEntry.DefaultValue defaultValue) {
                    List<Constraint> copy = new ArrayList<>(indexes);
                    copy.add(Constraint.any(RapidType.NUMBER));
                    Constraint result = state.getConstraint(defaultValue.defaultValue());
                    putArrayAssignment(results, copy, result);
                } else if (entry instanceof ArrayEntry.Assignment assignment) {
                    List<Constraint> copy = new ArrayList<>(indexes);
                    copy.add(state.getConstraint(assignment.index()));
                    Constraint result = state.getConstraint(assignment.value());
                    putArrayAssignment(results, copy, result);
                }
            }
        } else {
            Constraint result = state.getConstraint(variable);
            putArrayAssignment(results, indexes, result);
        }
    }

    private void putArrayAssignment(@NotNull Map<List<Constraint>, Constraint> results, @NotNull List<Constraint> indexes, @NotNull Constraint result) {
        if (results.containsKey(indexes)) {
            Constraint previousResult = results.get(indexes);
            results.put(indexes, result.or(previousResult));
        } else {
            results.put(indexes, result);
        }
    }

    private boolean isModified(@NotNull List<DataFlowState> before, @NotNull List<DataFlowState> after, @NotNull ReferenceValue variable) {
        Constraint beforeConstraint = before.stream().map(state -> state.getConstraint(variable)).collect(Constraint.or(variable.getType()));
        Constraint afterConstraint = after.stream().map(state -> state.getConstraint(variable)).collect(Constraint.or(variable.getType()));
        // TODO: 2023-08-01 This might need to be adjusted, but as #contains it would ignore further modification as first gave the variable any variable
        return !(beforeConstraint.equals(afterConstraint));
    }

    private void process(@NotNull DataFlowBlock block) {
        block.getStates().clear();
        for (DataFlowEdge successor : block.getSuccessors()) {
            successor.getDestination().getPredecessors().remove(successor);
        }
        block.getSuccessors().clear();
        for (DataFlowEdge predecessors : block.getPredecessors()) {
            block.getStates().addAll(predecessors.getStates());
        }
        BasicBlock basicBlock = block.getBasicBlock();
        DataFlowAnalyzerVisitor visitor = new DataFlowAnalyzerVisitor(functionBlock, block, blocks, functionMap);
        if (block.getStates().isEmpty()) {
            if (block.getBasicBlock() instanceof BasicBlock.IntermediateBasicBlock) {
                /*
                 * This block has no predecessors and is not the entry point of a function, as such, assume that any
                 * variable might be equal to any value.
                 */
                block.getStates().add(DataFlowState.createUnknownState(functionBlock));
            } else {
                block.getStates().add(DataFlowState.createState(functionBlock));
            }
        }
        for (LinearInstruction instruction : basicBlock.getInstructions()) {
            instruction.accept(visitor);
        }
        BranchingInstruction terminator = basicBlock.getTerminator();
        terminator.accept(visitor);
        for (DataFlowEdge successor : block.getSuccessors()) {
            successor.getDestination().getPredecessors().add(successor);
        }
    }
}
