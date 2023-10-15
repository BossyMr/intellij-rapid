package com.bossymr.rapid.language.builder;

import com.bossymr.rapid.language.flow.value.BinaryOperator;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.flow.value.UnaryOperator;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface RapidCodeBuilder {

    @NotNull ReferenceExpression getVariable(@NotNull String name);

    @NotNull ReferenceExpression getArgument(@NotNull String name);

    @NotNull ReferenceExpression getField(@NotNull String moduleName, @NotNull String name, @NotNull RapidType valueType);

    @NotNull
    default Expression aggregate(@NotNull RapidType aggregateType,
                                 @NotNull List<? extends Expression> expressions) {
        return aggregate(null, aggregateType, expressions);
    }

    @NotNull Expression aggregate(@Nullable RapidAggregateExpression element,
                                  @NotNull RapidType aggregateType,
                                  @NotNull List<? extends Expression> expressions);

    @NotNull
    default Expression literal(@NotNull Object value) {
        return literal(null, value);
    }

    @NotNull Expression literal(@Nullable RapidLiteralExpression element,
                                @NotNull Object value);

    @NotNull
    default Expression binary(@NotNull BinaryOperator operator,
                              @NotNull Expression left,
                              @NotNull Expression right) {
        return binary(null, operator, left, right);
    }

    @NotNull Expression binary(@Nullable RapidBinaryExpression element,
                               @NotNull BinaryOperator operator,
                               @NotNull Expression left,
                               @NotNull Expression right);

    @NotNull
    default Expression unary(@NotNull UnaryOperator operator,
                             @NotNull Expression expression) {
        return unary(null, operator, expression);
    }

    @NotNull Expression unary(@Nullable RapidUnaryExpression element,
                              @NotNull UnaryOperator operator,
                              @NotNull Expression expression);

    @NotNull Expression error(@Nullable RapidElement element,
                              @NotNull RapidType type);

    default void returnValue() {
        returnValue(null);
    }

    default void returnValue(@Nullable Expression expression) {
        returnValue(null, expression);
    }

    void returnValue(@Nullable RapidReturnStatement statement,
                     @Nullable Expression expression);
}
