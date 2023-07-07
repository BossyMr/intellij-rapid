package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.condition.ConditionType;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.constraint.StringConstraint;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.resolve.RapidResolveService;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DataFlowBlockVisitor extends ControlFlowVisitor {

    private final @NotNull Block.FunctionBlock functionBlock;
    private final @NotNull DataFlowBlock dataFlowBlock;
    private final @NotNull Map<BlockDescriptor, DataFlowFunction> functions;

    private final @NotNull Map<Map<Argument, Constraint>, Set<DataFlowFunction.Result>> results = new HashMap<>();

    public DataFlowBlockVisitor(@NotNull Block.FunctionBlock functionBlock, @NotNull DataFlowBlock block, @NotNull Map<BlockDescriptor, DataFlowFunction> functions) {
        this.functionBlock = functionBlock;
        this.dataFlowBlock = block;
        this.functions = functions;
    }


    private @NotNull Map<Argument, Constraint> getArguments(@NotNull Block.FunctionBlock block, @NotNull Map<ArgumentDescriptor, Value> values) {
        List<Argument> arguments = block.getArgumentGroups().stream()
                .flatMap(argumentGroup -> argumentGroup.arguments().stream())
                .toList();
        Map<Argument, Constraint> result = new HashMap<>();
        values.forEach((index, value) -> {
            Argument argument;
            if (index instanceof ArgumentDescriptor.Required required) {
                argument = arguments.get(required.index());
            } else if (index instanceof ArgumentDescriptor.Optional optional) {
                argument = arguments.stream()
                        .filter(element -> element.name().equals(optional.name()))
                        .findFirst()
                        .orElseThrow();
            } else {
                throw new AssertionError();
            }
            Constraint constraint = dataFlowBlock.getConstraint(value);
            result.put(argument, constraint);
        });
        return result;
    }

    private @NotNull List<DataFlowState> getLateBindingConstraint(@NotNull RapidType type, @NotNull BranchingInstruction.CallInstruction instruction) {
        Value routine = instruction.routine();
        Constraint constraint = dataFlowBlock.getConstraint(routine);
        if (!(constraint instanceof StringConstraint stringConstraint)) {
            return dataFlowBlock.states();
        }
        List<DataFlowState> states = new ArrayList<>();
        PsiElement element = instruction.element();
        Project project = element.getProject();
        RapidResolveService service = RapidResolveService.getInstance(project);
        for (String sequence : stringConstraint.sequences()) {
            String[] strings = sequence.split(":");
            BlockDescriptor descriptor;
            /*
             * If this block calls "continue". The expression might be invalid, which would fail at runtime.
             * This is currently not encoded in the block, and would need to be recomputed if used in an inspection.
             */
            if (strings.length == 2) {
                descriptor = new BlockDescriptor(strings[0], strings[1]);
            } else if (strings.length == 1) {
                List<RapidSymbol> symbols = service.findSymbols(element, strings[0]);
                if (symbols.isEmpty()) {
                    continue;
                }
                RapidSymbol symbol = symbols.get(0);
                if (symbol instanceof VirtualSymbol) {
                    descriptor = new BlockDescriptor("", strings[0]);
                } else {
                    PhysicalModule module = PhysicalModule.getModule(element);
                    if (module == null) {
                        continue;
                    }
                    String name = module.getName();
                    if (name == null) {
                        continue;
                    }
                    descriptor = new BlockDescriptor(name, strings[0]);
                }
            } else {
                continue;
            }
            DataFlowFunction function = functions.get(descriptor);
            DataFlowFunction.Result output = function.getOutput(dataFlowBlock, getArguments(function.getBlock(), instruction.arguments()));
            if (routine instanceof ConstantValue) {
                for (DataFlowState dataFlowState : dataFlowBlock.states()) {
                    processResult(dataFlowState, instruction.returnValue(), output);
                }
            } else if (routine instanceof ReferenceValue referenceValue) {
                List<DataFlowState> split = dataFlowBlock.split(state -> {
                    state.setCondition(new Condition(referenceValue, ConditionType.EQUALITY, new VariableExpression(new ConstantValue(RapidType.STRING, sequence))));
                    processResult(state, instruction.returnValue(), output);
                    return List.of(state);
                });
                states.addAll(split);
            }
        }
        return states;
    }

    private @NotNull Map<Argument, Constraint> getArguments() {
        Map<Argument, Constraint> constraints = new HashMap<>();
        List<Argument> arguments = functionBlock.getArgumentGroups().stream()
                .flatMap(argumentGroup -> argumentGroup.arguments().stream())
                .toList();
        for (Argument argument : arguments) {
            constraints.put(argument, dataFlowBlock.getConstraint(new VariableReference(argument.type(), argument)));
        }
        return constraints;
    }

    private void processResult(@NotNull Map<Argument, Constraint> arguments, @NotNull DataFlowFunction.Result result) {
        if (!(results.containsKey(arguments))) {
            results.put(arguments, new HashSet<>());
        }
        results.get(arguments).add(result);
    }

    private void processResult(@NotNull List<DataFlowState> states, @NotNull Block.FunctionBlock functionBlock, @NotNull DataFlowState state, @Nullable ReferenceValue returnValue, @NotNull DataFlowFunction.Result result) {
        if (result instanceof DataFlowFunction.Result.Success success) {
            // The state of the function can be used in the future, as, if an argument to the function is known, it can be used
            // to calculate the return value of the function.
        }
        if (result instanceof DataFlowFunction.Result.Error) {
            // TODO: 2023-07-05 If this function has an error clause, it should be called instead of returning immediately.
            processResult(getArguments(), result);
        }

    }

    @Override
    public void visitCallInstruction(BranchingInstruction.@NotNull CallInstruction instruction) {
        super.visitCallInstruction(instruction);
    }
}
