package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.builder.RapidCodeBlockBuilder;
import com.bossymr.rapid.language.builder.RapidParameterGroupBuilder;
import com.bossymr.rapid.language.builder.RapidRoutineBuilder;
import com.bossymr.rapid.language.flow.ArgumentGroup;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.psi.BlockType;
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
        ArgumentGroup argumentGroup = new ArgumentGroup(isOptional, new ArrayList<>());
        block.getArgumentGroups().add(argumentGroup);
        parameters.add(parameterGroup);
        ControlFlowParameterGroupBuilder builder = new ControlFlowParameterGroupBuilder(block, parameterGroup, argumentGroup);
        consumer.accept(builder);
        return this;
    }

    @Override
    public @NotNull RapidRoutineBuilder withCode(@Nullable BlockType codeType, @NotNull Consumer<RapidCodeBlockBuilder> consumer) {
        ControlFlowBlockBuilder blockBuilder = new ControlFlowBlockBuilder(block);
        ControlFlowCodeBlockBuilder builder = new ControlFlowCodeBlockBuilder(block, blockBuilder);
        if(codeType != null) {
            blockBuilder.addCommand(instruction -> {
                if(block.getEntryInstruction(codeType) == null) {
                    block.setEntryInstruction(codeType, instruction);
                }
            });
        }
        blockBuilder.enterScope();
        consumer.accept(builder);
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
        blockBuilder.addCommand(instruction -> {
            block.setErrorClause(exceptions, instruction);
        });
        blockBuilder.enterScope();
        consumer.accept(builder);
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
