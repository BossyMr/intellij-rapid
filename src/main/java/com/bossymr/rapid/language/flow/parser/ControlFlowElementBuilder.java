package com.bossymr.rapid.language.flow.parser;

import com.bossymr.rapid.language.builder.RapidCodeBlockBuilder;
import com.bossymr.rapid.language.builder.RapidModuleBuilder;
import com.bossymr.rapid.language.builder.RapidParameterGroupBuilder;
import com.bossymr.rapid.language.builder.RapidRoutineBuilder;
import com.bossymr.rapid.language.flow.ControlFlow;
import com.bossymr.rapid.language.flow.builder.ControlFlowBuilder;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.physical.*;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ControlFlowElementBuilder {

    private final @NotNull ControlFlowBuilder builder;

    public ControlFlowElementBuilder(@NotNull Project project) {
        this.builder = new ControlFlowBuilder(project);
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
        List<PhysicalParameterGroup> parameters = routine.getParameters();
        if (parameters != null && routineType == RoutineType.TRAP) {
            parameters = null;
        }
        if (parameters == null && routineType != RoutineType.TRAP) {
            parameters = List.of();
        }
        String name = routine.getName();
        if (name == null) {
            name = RapidSymbol.getDefaultText();
        }
        builder.withRoutine(routine, name, routineType, returnType, getRoutineConsumer(routine));
    }

    private @NotNull Consumer<RapidRoutineBuilder> getRoutineConsumer(@NotNull PhysicalRoutine routine) {
        return builder -> {
            if (routine.getParameters() != null) {
                for (PhysicalParameterGroup parameterGroup : routine.getParameters()) {
                    builder.withParameterGroup(parameterGroup.isOptional(), getParameterGroupConsumer(parameterGroup));
                }
            }
            for (RapidStatementList statementList : routine.getStatementLists()) {
                StatementListType statementListType = statementList.getStatementListType();
                if (statementListType == StatementListType.ERROR_CLAUSE) {
                    List<Integer> exceptions = getExceptions(statementList.getExpressions());
                    builder.withCode(exceptions, codeBuilder -> processExpression(statementList, codeBuilder));
                } else {
                    builder.withCode(statementListType, codeBuilder -> processExpression(statementList, codeBuilder));
                }
            }
        };
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

    private void processExpression(@NotNull RapidStatementList statementList, @NotNull RapidCodeBlockBuilder builder) {
        ControlFlowStatementVisitor visitor = new ControlFlowStatementVisitor(builder);
        for (RapidStatement statement : statementList.getStatements()) {
            statement.accept(visitor);
        }
    }
}
