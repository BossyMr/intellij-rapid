package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.builder.RapidCodeBlockBuilder;
import com.bossymr.rapid.language.builder.RapidParameterGroupBuilder;
import com.bossymr.rapid.language.builder.RapidRoutineBuilder;
import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.ArgumentGroup;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.psi.BlockType;
import com.bossymr.rapid.language.symbol.RapidParameter;
import com.bossymr.rapid.language.symbol.RapidParameterGroup;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.virtual.VirtualParameterGroup;
import com.bossymr.rapid.language.symbol.virtual.VirtualRoutine;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class ControlFlowRoutineBuilder implements RapidRoutineBuilder {

    private final @NotNull Block.FunctionBlock block;
    private final @NotNull RapidRoutine routine;

    public ControlFlowRoutineBuilder(@NotNull Block.FunctionBlock block, @NotNull RapidRoutine routine) {
        this.block = block;
        this.routine = routine;
    }

    @Override
    public @NotNull RapidRoutineBuilder withParameterGroup(boolean isOptional, @NotNull Consumer<RapidParameterGroupBuilder> consumer) {
        if (!(routine instanceof VirtualRoutine virtualRoutine)) {
            throw new IllegalArgumentException("Cannot add parameter group to physical routine");
        }
        List<VirtualParameterGroup> parameters = virtualRoutine.getParameters();
        if (parameters == null) {
            throw new IllegalStateException("Cannot add parameter group to routine of type: " + routine.getRoutineType());
        }
        VirtualParameterGroup parameterGroup = new VirtualParameterGroup(virtualRoutine, isOptional, new ArrayList<>());
        ArgumentGroup argumentGroup = new ArgumentGroup(isOptional, new ArrayList<>());
        block.getArgumentGroups().add(argumentGroup);
        parameters.add(parameterGroup);
        ControlFlowParameterGroupBuilder builder = new ControlFlowParameterGroupBuilder(block, parameterGroup, argumentGroup);
        consumer.accept(builder);
        return this;
    }

    @Override
    public @NotNull RapidRoutineBuilder withParameterGroup(@NotNull RapidParameterGroup parameterGroup) {
        List<ArgumentGroup> argumentGroups = block.getArgumentGroups();
        ArgumentGroup argumentGroup = new ArgumentGroup(parameterGroup.isOptional(), new ArrayList<>());
        for (RapidParameter parameter : parameterGroup.getParameters()) {
            String name = parameter.getName();
            if (name == null) {
                continue;
            }
            RapidType type = Objects.requireNonNullElse(parameter.getType(), RapidPrimitiveType.ANYTYPE);
            Argument argument = block.createArgument(name, type, parameter.getParameterType(), parameter, null);
            argumentGroup.arguments().add(argument);
        }
        argumentGroups.add(argumentGroup);
        return this;
    }

    @Override
    public @NotNull RapidRoutineBuilder withCode(@Nullable BlockType codeType, @NotNull Consumer<RapidCodeBlockBuilder> consumer) {
        ControlFlowBlockBuilder blockBuilder = new ControlFlowBlockBuilder(block);
        ControlFlowCodeBlockBuilder builder = new ControlFlowCodeBlockBuilder(block, blockBuilder);
        BlockType blockType = Objects.requireNonNullElse(codeType, BlockType.STATEMENT_LIST);
        blockBuilder.addCommand(instruction -> {
            if (block.getEntryInstruction(blockType) == null) {
                block.setEntryInstruction(blockType, instruction);
            }
        });
        blockBuilder.enterScope();
        consumer.accept(builder);
        if (routine.getRoutineType() == RoutineType.FUNCTION) {
            builder.returnValue(builder.any(Objects.requireNonNullElse(routine.getType(), RapidPrimitiveType.ANYTYPE)));
        } else {
            builder.returnValue();
        }
        return this;
    }

    @Override
    public @NotNull RapidRoutineBuilder withCode(@NotNull Function<RapidCodeBlockBuilder, List<Expression>> exceptions, @NotNull Consumer<RapidCodeBlockBuilder> consumer) {
        ControlFlowBlockBuilder blockBuilder = new ControlFlowBlockBuilder(block);
        ControlFlowCodeBlockBuilder builder = new ControlFlowCodeBlockBuilder(block, blockBuilder);
        List<Expression> expressions = exceptions.apply(builder);
        blockBuilder.addCommand(instruction -> {
            block.setErrorClause(expressions, instruction);
        });
        blockBuilder.enterScope();
        consumer.accept(builder);
        if (routine.getRoutineType() == RoutineType.FUNCTION) {
            builder.returnValue(builder.any(Objects.requireNonNullElse(routine.getType(), RapidPrimitiveType.ANYTYPE)));
        } else {
            builder.returnValue();
        }
        return this;
    }
}
