package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.builder.RapidCodeBlockBuilder;
import com.bossymr.rapid.language.builder.RapidModuleBuilder;
import com.bossymr.rapid.language.builder.RapidRoutineBuilder;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.BlockDescriptor;
import com.bossymr.rapid.language.flow.Variable;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.psi.BlockType;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidStatement;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.virtual.VirtualField;
import com.bossymr.rapid.language.symbol.virtual.VirtualParameterGroup;
import com.bossymr.rapid.language.symbol.virtual.VirtualRoutine;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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
        ControlFlowCodeBlockBuilder builder = new ControlFlowCodeBlockBuilder(block, new ControlFlowBlockBuilder(block));
        consumer.accept(builder);
        // If the initializer does not return a value, return an error expression.
        // If the initializer has returned a value, this statement will not be added.
        builder.returnValue(builder.any(valueType));
        controlFlow.put(blockDescriptor, block);
        return this;
    }

    @Override
    public @NotNull RapidModuleBuilder withField(@NotNull RapidField field) {
        String name = field.getName();
        if (name == null) {
            return this;
        }
        BlockDescriptor blockDescriptor = new BlockDescriptor(moduleName, name);
        Block.FieldBlock block = new Block.FieldBlock(field, moduleName);
        ControlFlowCodeBlockBuilder builder = new ControlFlowCodeBlockBuilder(block, new ControlFlowBlockBuilder(block));
        builder.returnValue(getInitializer(builder, field));
        controlFlow.put(blockDescriptor, block);
        return this;
    }

    private @NotNull Expression getInitializer(@NotNull ControlFlowCodeBlockBuilder builder, @NotNull RapidField field) {
        RapidType type = Objects.requireNonNullElse(field.getType(), RapidPrimitiveType.ANYTYPE);
        RapidExpression initializer = field.getInitializer();
        if (initializer != null) {
            return builder.expression(initializer);
        } else {
            return Objects.requireNonNullElseGet(getDefaultValue(builder, type), () -> builder.any(type));
        }
    }

    public @Nullable Expression getDefaultValue(@NotNull ControlFlowCodeBlockBuilder builder, @NotNull RapidType type) {
        if (type.isAssignable(RapidPrimitiveType.NUMBER)) {
            return builder.literal(0);
        }
        if (type.isAssignable(RapidPrimitiveType.STRING)) {
            return builder.literal("");
        }
        if (type.isAssignable(RapidPrimitiveType.BOOLEAN)) {
            return builder.literal(false);
        }
        if (type.getDimensions() > 0) {
            Variable variable = builder.createVariable(type);
            return builder.getReference(variable);
        }
        if (type.getRootStructure() instanceof RapidRecord) {
            Variable variable = builder.createVariable(type);
            return builder.getReference(variable);
        }
        throw new IllegalArgumentException("Could not get default value for: " + type);
    }

    @Override
    public @NotNull RapidModuleBuilder withRoutine(@NotNull String name, @NotNull RoutineType routineType, @Nullable RapidType returnType, @NotNull Consumer<RapidRoutineBuilder> consumer) {
        BlockDescriptor blockDescriptor = new BlockDescriptor(moduleName, name);
        List<VirtualParameterGroup> parameterGroups = routineType != RoutineType.TRAP ? new ArrayList<>() : null;
        VirtualRoutine routine = new VirtualRoutine(moduleName, name, routineType, getRoutineType(routineType, returnType), parameterGroups);
        Block.FunctionBlock block = new Block.FunctionBlock(routine, moduleName);
        ControlFlowRoutineBuilder builder = new ControlFlowRoutineBuilder(block, routine);
        consumer.accept(builder);
        controlFlow.put(blockDescriptor, block);
        return this;
    }

    @Override
    public @NotNull RapidModuleBuilder withRoutine(@NotNull RapidRoutine routine) {
        String name = routine.getName();
        if (name == null) {
            return this;
        }
        BlockDescriptor blockDescriptor = new BlockDescriptor(moduleName, name);
        Block.FunctionBlock block = new Block.FunctionBlock(routine, moduleName);
        Map<RapidField, Variable> variables = new HashMap<>();
        for (RapidField field : routine.getFields()) {
            variables.put(field, block.createVariable(field.getName(), field.getFieldType(), Objects.requireNonNullElse(field.getType(), RapidPrimitiveType.ANYTYPE)));
        }
        ControlFlowRoutineBuilder builder = new ControlFlowRoutineBuilder(block, routine);
        List<? extends RapidParameterGroup> parameters = routine.getParameters();
        if (parameters != null) {
            for (RapidParameterGroup parameterGroup : routine.getParameters()) {
                builder.withParameterGroup(parameterGroup);
            }
        }
        for (BlockType blockType : BlockType.values()) {
            List<RapidStatement> statements = routine.getStatements(blockType);
            if (statements != null) {
                if (blockType == BlockType.ERROR_CLAUSE) {
                    List<RapidExpression> errorClause = Objects.requireNonNullElseGet(routine.getErrorClause(), ArrayList::new);
                    builder.withCode(codeBuilder -> errorClause.stream()
                            .map(codeBuilder::expression)
                                                               .toList(), getConsumer(variables, routine, statements));
                } else {
                    builder.withCode(blockType, getConsumer(variables, routine, statements));
                }
            }
        }
        controlFlow.put(blockDescriptor, block);
        return this;
    }

    private @NotNull Consumer<RapidCodeBlockBuilder> getConsumer(@NotNull Map<RapidField, Variable> variables, @NotNull RapidRoutine routine, @NotNull List<RapidStatement> statements) {
        return builder -> {
            for (RapidField field : variables.keySet()) {
                Variable variable = variables.get(field);
                RapidExpression initializer = field.getInitializer();
                if (initializer != null) {
                    Expression expression = builder.expression(initializer);
                    if (expression == null || !(variable.getType().isAssignable(expression.getType()))) {
                        expression = builder.any(variable.getType());
                    }
                    builder.assign(builder.getReference(variable), expression);
                }
            }
            for (RapidStatement statement : statements) {
                builder.statement(statement);
            }
            if (routine.getRoutineType() == RoutineType.FUNCTION) {
                builder.returnValue(builder.any(Objects.requireNonNullElse(routine.getType(), RapidPrimitiveType.ANYTYPE)));
            } else {
                builder.returnValue();
            }
        };
    }

    private @Nullable RapidType getRoutineType(@NotNull RoutineType routineType, @Nullable RapidType type) {
        if (routineType != RoutineType.FUNCTION) {
            return null;
        }
        return Objects.requireNonNullElse(type, RapidPrimitiveType.ANYTYPE);
    }
}
