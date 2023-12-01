package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.builder.RapidCodeBlockBuilder;
import com.bossymr.rapid.language.builder.RapidModuleBuilder;
import com.bossymr.rapid.language.builder.RapidRoutineBuilder;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.BlockDescriptor;
import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.symbol.RapidField;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.virtual.VirtualField;
import com.bossymr.rapid.language.symbol.virtual.VirtualParameterGroup;
import com.bossymr.rapid.language.symbol.virtual.VirtualRoutine;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class ControlFlowModuleBuilder implements RapidModuleBuilder {

    private final @NotNull String moduleName;
    private final @NotNull Map<BlockDescriptor, Block> controlFlow;

    public ControlFlowModuleBuilder(@NotNull String name, @NotNull Map<BlockDescriptor, Block> controlFlow) {
        this.moduleName = name;
        this.controlFlow = controlFlow;
    }

    @Override
    public @NotNull RapidModuleBuilder withField(@NotNull String name, @NotNull FieldType fieldType, @NotNull RapidType valueType, @NotNull Consumer<RapidCodeBlockBuilder> consumer) {
        BlockDescriptor blockDescriptor = new BlockDescriptor(moduleName, name);
        VirtualField field = new VirtualField(moduleName, name, fieldType, valueType, true);
        Block.FieldBlock block = new Block.FieldBlock(field, moduleName);
        ControlFlowBlockBuilder blockBuilder = new ControlFlowBlockBuilder(block);
        ControlFlowCodeBlockBuilder codeBlockBuilder = new ControlFlowCodeBlockBuilder(block, blockBuilder);
        consumer.accept(codeBlockBuilder);
        if(blockBuilder.isInScope()) {
            codeBlockBuilder.returnValue(codeBlockBuilder.error(null, valueType));
        }
        controlFlow.put(blockDescriptor, block);
        return this;
    }

    @Override
    public @NotNull RapidModuleBuilder withField(@NotNull RapidField field) {
        RapidType type = Objects.requireNonNullElse(field.getType(), RapidPrimitiveType.ANYTYPE);
        return null;
    }

    @Override
    public @NotNull RapidModuleBuilder withRoutine(@NotNull String name, @NotNull RoutineType routineType, @Nullable RapidType returnType, @NotNull Consumer<RapidRoutineBuilder> consumer) {
        BlockDescriptor blockDescriptor = new BlockDescriptor(moduleName, name);
        List<VirtualParameterGroup> parameterGroups = routineType != RoutineType.TRAP ? new ArrayList<>() : null;
        VirtualRoutine routine = new VirtualRoutine(moduleName, name, routineType, returnType, parameterGroups);
        Block.FunctionBlock block = new Block.FunctionBlock(routine, moduleName);
        ControlFlowRoutineBuilder builder = new ControlFlowRoutineBuilder(block, routine);
        consumer.accept(builder);
        controlFlow.put(blockDescriptor, block);
        return this;
    }

    @Override
    public @NotNull RapidModuleBuilder withRoutine(@NotNull RapidRoutine routine) {
        return null;
    }
}
