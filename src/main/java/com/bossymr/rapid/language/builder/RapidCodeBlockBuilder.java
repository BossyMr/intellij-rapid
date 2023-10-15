package com.bossymr.rapid.language.builder;

import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.psi.RapidElement;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface RapidCodeBlockBuilder extends RapidCodeBuilder {

    default @NotNull Label label() {
        return label(null, null);
    }

    @NotNull Label label(@Nullable RapidElement element, @Nullable String name);

    @NotNull RapidCodeBlockBuilder error(@Nullable RapidElement element);

    default @NotNull RapidCodeBlockBuilder ifThen(@NotNull Expression expression,
                                                  @NotNull Consumer<RapidCodeBlockBuilder> thenConsumer) {
        return ifThen(null, expression, thenConsumer);
    }

    @NotNull RapidCodeBlockBuilder ifThen(@Nullable RapidElement element,
                                          @NotNull Expression expression,
                                          @NotNull Consumer<RapidCodeBlockBuilder> thenConsumer);

    @NotNull
    default RapidCodeBlockBuilder ifThenElse(@NotNull Expression expression,
                                             @NotNull Consumer<RapidCodeBlockBuilder> thenConsumer,
                                             @NotNull Consumer<RapidCodeBlockBuilder> elseConsumer) {
        return ifThenElse(null, expression, thenConsumer, elseConsumer);
    }

    @NotNull RapidCodeBlockBuilder ifThenElse(@Nullable RapidElement element,
                                              @NotNull Expression expression,
                                              @NotNull Consumer<RapidCodeBlockBuilder> thenConsumer,
                                              @NotNull Consumer<RapidCodeBlockBuilder> elseConsumer);

    @NotNull
    default RapidCodeBlockBuilder goTo(@NotNull Label label) {
        return goTo(null, label);
    }

    @NotNull RapidCodeBlockBuilder goTo(@Nullable RapidElement element,
                                        @NotNull Label label);

    @NotNull
    default RapidCodeBlockBuilder throwException() {
        return throwException(null);
    }

    @NotNull
    default RapidCodeBlockBuilder throwException(@Nullable Expression expression) {
        return throwException(null, expression);
    }

    @NotNull RapidCodeBlockBuilder throwException(@Nullable RapidElement element,
                                                  @Nullable Expression expression);

    @NotNull
    default RapidCodeBlockBuilder tryNextInstruction() {
        return tryNextInstruction(null);
    }

    @NotNull RapidCodeBlockBuilder tryNextInstruction(@Nullable RapidElement element);

    @NotNull
    default RapidCodeBlockBuilder exit() {
        return exit(null);
    }

    @NotNull RapidCodeBlockBuilder exit(@Nullable RapidElement element);

    @NotNull
    default RapidCodeBlockBuilder retryInstruction() {
        return retryInstruction(null);
    }

    @NotNull RapidCodeBlockBuilder retryInstruction(@Nullable RapidElement element);

    @NotNull
    default RapidCodeBuilder assign(@NotNull ReferenceExpression variable,
                                    @NotNull Expression expression) {
        return assign(null, variable, expression);
    }

    @NotNull RapidCodeBuilder assign(@Nullable RapidElement element,
                                     @NotNull ReferenceExpression variable,
                                     @NotNull Expression expression);

    @NotNull
    default RapidCodeBuilder connect(@NotNull ReferenceExpression variable,
                                     @NotNull Expression expression) {
        return connect(null, variable, expression);
    }

    @NotNull RapidCodeBuilder connect(@Nullable RapidElement element,
                                      @NotNull ReferenceExpression variable,
                                      @NotNull Expression expression);

    default @NotNull Expression call(@NotNull String routine,
                                     @NotNull RapidType returnType,
                                     @NotNull Consumer<RapidArgumentBuilder> arguments) {
        return call(literal(routine), returnType, arguments);
    }

    default @NotNull Expression call(@NotNull Expression routine,
                                     @NotNull RapidType returnType,
                                     @NotNull Consumer<RapidArgumentBuilder> arguments) {
        return call(null, routine, returnType, arguments);
    }

    @NotNull Expression call(@Nullable RapidElement element,
                             @NotNull Expression routine,
                             @NotNull RapidType returnType,
                             @NotNull Consumer<RapidArgumentBuilder> arguments);


    default @NotNull RapidCodeBlockBuilder invoke(@NotNull String routine,
                                                  @NotNull Consumer<RapidArgumentBuilder> arguments) {
        return invoke(literal(routine), arguments);
    }

    default @NotNull RapidCodeBlockBuilder invoke(@NotNull Expression routine,
                                                  @NotNull Consumer<RapidArgumentBuilder> arguments) {
        return invoke(null, routine, arguments);
    }

    @NotNull RapidCodeBlockBuilder invoke(@Nullable RapidElement element,
                                          @NotNull Expression routine,
                                          @NotNull Consumer<RapidArgumentBuilder> arguments);
}
