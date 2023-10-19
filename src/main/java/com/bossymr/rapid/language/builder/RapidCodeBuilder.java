package com.bossymr.rapid.language.builder;

import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface RapidCodeBuilder {

    default @NotNull ReferenceExpression createVariable(@NotNull RapidType type) {
        return createVariable(null, null, type);
    }

    @NotNull ReferenceExpression createVariable(@Nullable String name,
                                                @Nullable FieldType fieldType,
                                                @NotNull RapidType type);

    default @NotNull ReferenceExpression getVariable(@NotNull String name) {
        return getVariable(null, name);
    }

    @NotNull ReferenceExpression getVariable(@Nullable RapidReferenceExpression element,
                                             @NotNull String name);

    default @NotNull ReferenceExpression getArgument(@NotNull String name) {
        return getArgument(null, name);
    }


    @NotNull ReferenceExpression getArgument(@Nullable RapidReferenceExpression element,
                                             @NotNull String name);


    default @NotNull ReferenceExpression getField(@NotNull String moduleName,
                                                  @NotNull String name,
                                                  @NotNull RapidType valueType) {
        return getField(null, moduleName, name, valueType);
    }

    @NotNull ReferenceExpression getField(@Nullable RapidReferenceExpression element,
                                          @NotNull String moduleName,
                                          @NotNull String name,
                                          @NotNull RapidType valueType);

    default @NotNull IndexExpression index(@NotNull ReferenceExpression variable,
                                           @NotNull Expression index) {
        return index(null, variable, index);
    }

    @NotNull IndexExpression index(@Nullable RapidIndexExpression element,
                                   @NotNull ReferenceExpression variable,
                                   @NotNull Expression index);


    default @NotNull Expression component(@NotNull RapidType type,
                                          @NotNull ReferenceExpression variable,
                                          @NotNull String name) {
        return component(null, type, variable, name);
    }

    @NotNull Expression component(@Nullable RapidReferenceExpression element,
                                  @NotNull RapidType type,
                                  @NotNull ReferenceExpression variable,
                                  @NotNull String name);


    default @NotNull Expression aggregate(@NotNull RapidType aggregateType,
                                          @NotNull List<? extends Expression> expressions) {
        return aggregate(null, aggregateType, expressions);
    }

    @NotNull Expression aggregate(@Nullable RapidAggregateExpression element,
                                  @NotNull RapidType aggregateType,
                                  @NotNull List<? extends Expression> expressions);

    default @NotNull Expression literal(@NotNull Object value) {
        return literal(null, value);
    }

    @NotNull Expression literal(@Nullable RapidLiteralExpression element,
                                @NotNull Object value);

    default @NotNull Expression binary(@NotNull BinaryOperator operator,
                                       @NotNull Expression left,
                                       @NotNull Expression right) {
        return binary(null, operator, left, right);
    }

    @NotNull Expression binary(@Nullable RapidBinaryExpression element,
                               @NotNull BinaryOperator operator,
                               @NotNull Expression left,
                               @NotNull Expression right);

    default @NotNull Expression unary(@NotNull UnaryOperator operator,
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
