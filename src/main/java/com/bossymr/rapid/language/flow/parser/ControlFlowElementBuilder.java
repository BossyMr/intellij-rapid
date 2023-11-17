package com.bossymr.rapid.language.flow.parser;

import com.bossymr.rapid.language.builder.RapidCodeBlockBuilder;
import com.bossymr.rapid.language.builder.RapidModuleBuilder;
import com.bossymr.rapid.language.builder.RapidParameterGroupBuilder;
import com.bossymr.rapid.language.builder.RapidRoutineBuilder;
import com.bossymr.rapid.language.flow.ControlFlow;
import com.bossymr.rapid.language.flow.builder.ControlFlowBuilder;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.physical.*;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ControlFlowElementBuilder {

    private final @NotNull ControlFlowBuilder builder;

    public ControlFlowElementBuilder() {
        this.builder = new ControlFlowBuilder();
    }

    public @NotNull ControlFlow getControlFlow() {
        return builder.getControlFlow();
    }

    public void process(@NotNull PhysicalModule module) {
        String name = module.getName();
        if (name == null) {
            name = RapidSymbol.getDefaultText();
        }
        builder.withModule(name, builder -> {
            for (PhysicalField field : module.getFields()) {
                process(field, builder);
            }
            for (PhysicalRoutine routine : module.getRoutines()) {
                process(routine, builder);
            }
        });
    }

    private void process(@NotNull PhysicalField field, @NotNull RapidModuleBuilder builder) {
        RapidType valueType = field.getType();
        if (valueType == null) {
            valueType = RapidPrimitiveType.ANYTYPE;
        }
        String name = field.getName();
        if (name == null) {
            name = RapidSymbol.getDefaultText();
        }
        FieldType fieldType = field.getFieldType();
        builder.withField(field, name, fieldType, valueType, fieldBuilder -> {
            RapidExpression initializer = field.getInitializer();
            if (initializer != null) {
                fieldBuilder.withInitializer(codeBuilder -> {
                    Expression expression = ControlFlowExpressionVisitor.getExpression(initializer, codeBuilder);
                    codeBuilder.returnValue(expression);
                });
            }
        });
    }

    private void process(@NotNull PhysicalRoutine routine, @NotNull RapidModuleBuilder builder) {
        RoutineType routineType = routine.getRoutineType();
        RapidType returnType = routine.getType();
        if (returnType != null && routineType != RoutineType.FUNCTION) {
            returnType = null;
        }
        if (returnType == null && routineType == RoutineType.FUNCTION) {
            returnType = RapidPrimitiveType.ANYTYPE;
        }
        String name = routine.getName();
        if (name == null) {
            name = RapidSymbol.getDefaultText();
        }
        builder.withRoutine(routine, name, routineType, returnType, getRoutineConsumer(routine));
    }

    public static void processExpression(@NotNull PhysicalRoutine routine, @NotNull RapidStatementList statementList, @NotNull RapidCodeBlockBuilder builder) {
        ControlFlowStatementVisitor visitor = new ControlFlowStatementVisitor(builder, routine);
        for (RapidElement element : PsiTreeUtil.getChildrenOfAnyType(statementList, RapidStatement.class, PhysicalField.class)) {
            if (element instanceof PhysicalField field) {
                ReferenceExpression variable = builder.createVariable(field.getName(), field.getFieldType(), field.getType() != null ? field.getType() : RapidPrimitiveType.ANYTYPE);
                if (field.getInitializer() != null) {
                    builder.assign(variable, ControlFlowExpressionVisitor.getExpression(field.getInitializer(), builder));
                }
            }
            if (element instanceof RapidStatement statement) {
                statement.accept(visitor);
            }
        }
    }

    private @NotNull Consumer<RapidParameterGroupBuilder> getParameterGroupConsumer(@NotNull PhysicalParameterGroup parameterGroup) {
        return builder -> {
            for (PhysicalParameter parameter : parameterGroup.getParameters()) {
                String name = parameter.getName();
                if(name == null) {
                    name = RapidSymbol.getDefaultText();
                }
                RapidType type = parameter.getType();
                if(type == null) {
                    type = RapidPrimitiveType.ANYTYPE;
                }
                builder.withParameter(name, parameter.getParameterType(), type);
            }
        };
    }

    private @Nullable List<Integer> getExceptions(@Nullable List<RapidExpression> expressions) {
        if (expressions == null) {
            return null;
        }
        List<Integer> exceptions = new ArrayList<>();
        for (RapidExpression expression : expressions) {
            if (expression instanceof RapidLiteralExpression literal) {
                Object value = literal.getValue();
                if (value instanceof Integer exception) {
                    exceptions.add(exception);
                }
            }
        }
        return exceptions;
    }

    private @NotNull Consumer<RapidRoutineBuilder> getRoutineConsumer(@NotNull PhysicalRoutine routine) {
        return builder -> {
            if (routine.getParameters() != null) {
                RoutineType routineType = routine.getRoutineType();
                List<PhysicalParameterGroup> parameters = routine.getParameters();
                if (routineType == RoutineType.TRAP) {
                    parameters = List.of();
                }
                if (parameters == null) {
                    parameters = List.of();
                }
                for (PhysicalParameterGroup parameterGroup : parameters) {
                    builder.withParameterGroup(parameterGroup.isOptional(), getParameterGroupConsumer(parameterGroup));
                }
            }
            for (RapidStatementList statementList : routine.getStatementLists()) {
                BlockType blockType = statementList.getStatementListType();
                if (blockType == BlockType.ERROR_CLAUSE) {
                    List<Integer> exceptions = getExceptions(statementList.getExpressions());
                    builder.withCode(exceptions, codeBuilder -> processExpression(routine, statementList, codeBuilder));
                } else {
                    builder.withCode(blockType, codeBuilder -> processExpression(routine, statementList, codeBuilder));
                }
            }
        };
    }
}
