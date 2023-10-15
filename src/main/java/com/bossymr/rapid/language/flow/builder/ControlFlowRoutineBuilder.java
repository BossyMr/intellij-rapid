package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.builder.RapidCodeBlockBuilder;
import com.bossymr.rapid.language.builder.RapidParameterGroupBuilder;
import com.bossymr.rapid.language.builder.RapidRoutineBuilder;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.psi.StatementListType;
import com.bossymr.rapid.language.symbol.virtual.VirtualParameterGroup;
import com.bossymr.rapid.language.symbol.virtual.VirtualRoutine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ControlFlowRoutineBuilder implements RapidRoutineBuilder {

    private final @NotNull Block.FunctionBlock block;
    private final @NotNull VirtualRoutine routine;

    public ControlFlowRoutineBuilder(@NotNull Block.FunctionBlock block, @NotNull VirtualRoutine routine) {
        this.block = block;
        this.routine = routine;
    }

    @Override
    public @NotNull RapidRoutineBuilder withParameterGroup(boolean isOptional, @NotNull Consumer<RapidParameterGroupBuilder> consumer) {
        List<VirtualParameterGroup> parameters = routine.getParameters();
        if (parameters == null) {
            throw new IllegalStateException();
        }
        VirtualParameterGroup parameterGroup = new VirtualParameterGroup(routine, isOptional, new ArrayList<>());
        ControlFlowParameterGroupBuilder builder = new ControlFlowParameterGroupBuilder(parameterGroup);
        consumer.accept(builder);
        parameters.add(parameterGroup);
        return this;
    }

    @Override
    public @NotNull RapidRoutineBuilder withCode(@NotNull StatementListType codeType, @NotNull Consumer<RapidCodeBlockBuilder> consumer) {
        ControlFlowBlockBuilder blockBuilder = new ControlFlowBlockBuilder(block);
        ControlFlowCodeBlockBuilder builder = new ControlFlowCodeBlockBuilder(block, blockBuilder);
        consumer.accept(builder);
        if (!(blockBuilder.getInstructions().isEmpty())) {
            block.setEntryInstruction(codeType, blockBuilder.getInstructions().get(0));
        }
        if(blockBuilder.isInScope()) {
            if(routine.getType() != null) {
                throw new IllegalArgumentException();
            } else {
                builder.returnValue();
            }
        }
        return this;
    }

    @Override
    public @NotNull RapidRoutineBuilder withCode(@Nullable List<Integer> exceptions, @NotNull Consumer<RapidCodeBlockBuilder> consumer) {
        ControlFlowBlockBuilder blockBuilder = new ControlFlowBlockBuilder(block);
        ControlFlowCodeBlockBuilder builder = new ControlFlowCodeBlockBuilder(block, blockBuilder);
        consumer.accept(builder);
        if (!(blockBuilder.getInstructions().isEmpty())) {
            block.setErrorClause(exceptions, blockBuilder.getInstructions().get(0));
        }
        if(blockBuilder.isInScope()) {
            if(routine.getType() != null) {
                throw new IllegalArgumentException();
            } else {
                builder.returnValue();
            }
        }
        return this;
    }
}
