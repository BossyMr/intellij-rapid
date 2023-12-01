package com.bossymr.rapid.language.builder;

import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.Field;
import com.bossymr.rapid.language.flow.Variable;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.RapidVariable;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A builder for {@code Rapid} code blocks.
 */
public interface RapidCodeBlockBuilder {

    /**
     * Adds a new variable to this code block.
     *
     * @param type the type of the variable.
     * @return the variable.
     */
    @NotNull Variable createVariable(@NotNull RapidType type);

    /**
     * Adds the specified variable to this code block.
     *
     * @param variable the variable.
     * @return the variable.
     */
    @NotNull Variable createVariable(@NotNull RapidVariable variable);

    /**
     * Returns the argument with the specified name.
     *
     * @param name the name of the argument.
     * @return the argument with the specified name, or {@code null} if no argument with the specified was found.
     */
    @Nullable Argument getArgument(@NotNull String name);

    /**
     * Returns a reference to the specified field.
     *
     * @param field the field.
     * @return a reference to the specified field.
     */
    @NotNull ReferenceExpression getReference(@NotNull Field field);

    /**
     * Creates a new reference to the specified field.
     *
     * @param field the field.
     * @return a new reference to the specified field.
     */
    @NotNull ReferenceExpression createReference(@NotNull Field field);

    /**
     * Returns a reference which matches the specified expression.
     *
     * @param expression the expression.
     * @return a reference which matches the specified expression.
     */
    @Nullable ReferenceExpression getReference(@NotNull RapidReferenceExpression expression);

    /**
     * Creates a new reference which matches the specified expression.
     *
     * @param expression the expression.
     * @return a new reference which matches the specified expression.
     */
    @Nullable ReferenceExpression createReference(@NotNull RapidReferenceExpression expression);

    /**
     * Returns a reference to the specified index of the specified variable.
     *
     * @param variable the variable.
     * @param index the index.
     * @return a reference to the specified index of the specified variable.
     */
    @NotNull IndexExpression index(@NotNull ReferenceExpression variable, @NotNull Expression index);

    /**
     * Returns a reference which matches the specified expression.
     *
     * @param expression the expression
     * @return a reference which matches the specified expression.
     */
    @Nullable IndexExpression index(@NotNull RapidIndexExpression expression);

    /**
     * Returns a reference to the specified component of the specified variable.
     *
     * @param variable the variable.
     * @param name the name of the component.
     * @return a reference to the specified component of the specified variable.
     */
    @NotNull ComponentExpression component(@NotNull ReferenceExpression variable,
                                           @NotNull String name);

    /**
     * Returns a reference which matches the specified expression.
     *
     * @param expression the expression
     * @return a reference which matches the specified expression.
     */
    @Nullable ComponentExpression component(@NotNull RapidReferenceExpression expression);


    /**
     * Returns a new aggregate expression with the specified components.
     *
     * @param aggregateType the type of the aggregate expression.
     * @param expressions the components.
     * @return a new aggregate expression.
     */
    @NotNull AggregateExpression aggregate(@NotNull RapidType aggregateType,
                                           @NotNull List<? extends Expression> expressions);

    /**
     * Returns a reference which matches the specified expression.
     *
     * @param expression the expression
     * @return a reference which matches the specified expression.
     */
    @Nullable AggregateExpression aggregate(@NotNull RapidAggregateExpression expression);

    /**
     * Returns a new literal expression.
     *
     * @param value the value.
     * @return a new literal expression.
     */
    @NotNull LiteralExpression literal(@NotNull Object value);

    /**
     * Returns a reference which matches the specified expression.
     *
     * @param expression the expression
     * @return a reference which matches the specified expression.
     */
    @Nullable LiteralExpression literal(@NotNull RapidLiteralExpression expression);

    /**
     * Returns a new binary expression.
     *
     * @param operator the operator.
     * @param left the left expression.
     * @param right the right expression.
     * @return a new binary expression.
     */
    @NotNull BinaryExpression binary(@NotNull BinaryOperator operator,
                                     @NotNull Expression left,
                                     @NotNull Expression right);

    /**
     * Returns a reference which matches the specified expression.
     *
     * @param expression the expression
     * @return a reference which matches the specified expression.
     */
    @Nullable BinaryExpression binary(@NotNull RapidBinaryExpression expression);

    /**
     * Returns a new unary expression.
     *
     * @param operator the operator.
     * @param expression the expression.
     * @return a new unary expression.
     */
    @NotNull UnaryExpression unary(@NotNull UnaryOperator operator,
                                   @NotNull Expression expression);

    /**
     * Returns a reference which matches the specified expression.
     *
     * @param expression the expression
     * @return a reference which matches the specified expression.
     */
    @Nullable UnaryExpression unary(@Nullable RapidUnaryExpression expression);

    /**
     * Creates a new label without a name.
     *
     * @return the label.
     */
    default @NotNull Label createLabel() {
        return createLabel(null);
    }

    /**
     * Creats a new label with the specified name.
     *
     * @param name the name of the label.
     * @return the label.
     */
    @NotNull Label createLabel(@Nullable String name);

    /**
     * Returns the label with the specified name.
     *
     * @param name the name of the label.
     * @return the label, or {@code null} if no label with the specified name was found.
     */
    @Nullable Label getLabel(@NotNull String name);

    /**
     * Adds a new return statement to this code block.
     */
    default void returnValue() {
        returnValue((Expression) null);
    }

    /**
     * Adds a new return statement to this code block, which returns the specified expression.
     *
     * @param expression the expression.
     */
    void returnValue(@Nullable Expression expression);

    /**
     * Adds the specified return statement to this code block.
     *
     * @param statement the statement.
     */
    void returnValue(@NotNull RapidReturnStatement statement);

    /**
     * Adds a new if statement to this code block.
     *
     * @param condition the condition of the if statement.
     * @param thenConsumer the handler which can define the then-branch of the if statement.
     * @return this builder.
     */
    @NotNull RapidCodeBlockBuilder ifThen(@NotNull Expression condition,
                                          @NotNull Consumer<RapidCodeBlockBuilder> thenConsumer);

    /**
     * Adds the specified if statement to this code block.
     *
     * @param statement the statement.
     * @return this builder.
     */
    default @NotNull RapidCodeBlockBuilder ifThen(@NotNull RapidIfStatement statement) {
        return ifThenElse(statement);
    }

    /**
     * Adds a new if statement to this code block.
     *
     * @param condition the condition of the if statement.
     * @param thenConsumer the handler which can define the then-branch of the if statement.
     * @param elseConsumer the handler which can define the else-branch of the if statement.
     * @return this builder.
     */
    @NotNull RapidCodeBlockBuilder ifThenElse(@NotNull Expression condition,
                                              @NotNull Consumer<RapidCodeBlockBuilder> thenConsumer,
                                              @NotNull Consumer<RapidCodeBlockBuilder> elseConsumer);

    /**
     * Adds the specified if statement to this code block.
     *
     * @param statement the statement.
     * @return this builder.
     */
    @NotNull RapidCodeBlockBuilder ifThenElse(@NotNull RapidIfStatement statement);

    /**
     * Adds a new loop to this code block.
     *
     * @param condition the condition of the loop.
     * @param consumer the handler which can define the body of the loop.
     * @return this builder.
     */
    @NotNull RapidCodeBlockBuilder whileLoop(@NotNull Expression condition,
                                             @NotNull Consumer<RapidCodeBlockBuilder> consumer);

    /**
     * Adds the specified while statement to this code block.
     *
     * @param statement the statement.
     * @return this builder.
     */
    @NotNull RapidCodeBlockBuilder whileLoop(@NotNull RapidWhileStatement statement);

    /**
     * Adds a new for loop to this code block.
     *
     * @param name the name of the index variable.
     * @param fromExpression the start expression.
     * @param toExpression the end expression.
     * @param stepExpression the step expression.
     * @param consumer the handler which can define the body of the loop.
     * @return this builder.
     */
    @NotNull RapidCodeBlockBuilder forLoop(@Nullable String name,
                                           @NotNull Expression fromExpression,
                                           @NotNull Expression toExpression,
                                           @Nullable Expression stepExpression,
                                           @NotNull BiConsumer<Variable, RapidCodeBlockBuilder> consumer);

    /**
     * Adds the specified for loop statement to this code block.
     *
     * @param statement the statement.
     * @return this builder.
     */
    @NotNull RapidCodeBlockBuilder forLoop(@NotNull RapidForStatement statement);

    /**
     * Adds a new test statement to this code block.
     *
     * @param condition the condition of the test statement.
     * @param consumer the handler which can define the test statement.
     * @return this builder.
     */
    @NotNull RapidCodeBlockBuilder test(@NotNull Expression condition,
                                        @NotNull Consumer<RapidTestBlockBuilder> consumer);

    /**
     * Adds the specified test statement to this code block.
     *
     * @param statement the statement.
     * @return this builder.
     */
    @NotNull RapidCodeBlockBuilder test(@NotNull RapidTestStatement statement);

    /**
     * Adds a new goto statement.
     *
     * @param label the label to jump to.
     */
    void goTo(@NotNull Label label);

    /**
     * Adds the specified goto statement.
     *
     * @param statement the statement.
     */
    void goTo(@NotNull RapidGotoStatement statement);

    /**
     * Adds a new raise statement which will raise the current exception.
     */
    default void raise() {
        raise((Expression) null);
    }

    /**
     * Adds a new raise statement which will raise the specified exception.
     *
     * @param expression the exception, or {@code null} to raise the current exception.
     */
    void raise(@Nullable Expression expression);

    /**
     * Adds the specified raise statement.
     *
     * @param statement the statement.
     */
    void raise(@NotNull RapidRaiseStatement statement);

    /**
     * Adds a new trynext statement.
     */
    void tryNext();

    /**
     * Adds the specified trynext statement.
     *
     * @param statement the statement.
     */
    void tryNext(@NotNull RapidTryNextStatement statement);

    /**
     * Adds a new exit statement.
     */
    void exit();

    /**
     * Adds the specified exit statement.
     *
     * @param statement the statement.
     */
    void exit(@NotNull RapidExitStatement statement);

    /**
     * Adds a new retry statement.
     */
    void retry();

    /**
     * Adds the specified retry statement.
     *
     * @param statement the statement.
     */
    void retry(@NotNull RapidRetryStatement statement);

    /**
     * Adds a new assignment statement which will assign the specified expression to the specified variable.
     *
     * @param variable the variable.
     * @param expression the expression.
     * @return this builder.
     */
    @NotNull RapidCodeBlockBuilder assign(@NotNull ReferenceExpression variable,
                                          @NotNull Expression expression);

    /**
     * Adds the specified assignment statement.
     *
     * @param statement the statement.
     * @return this builder.
     */
    @NotNull RapidCodeBlockBuilder assign(@NotNull RapidAssignmentStatement statement);

    /**
     * Adds a new connect statement which will connect the specified expression to the specified variable.
     *
     * @param variable the variable.
     * @param routine the name of the routine.
     * @return this builder.
     */
    @NotNull RapidCodeBlockBuilder connect(@NotNull ReferenceExpression variable,
                                           @NotNull String routine);

    /**
     * Adds the specified connect statement.
     *
     * @param statement the statement.
     * @return this builder.
     */
    @NotNull RapidCodeBlockBuilder connect(@NotNull RapidConnectStatement statement);

    /**
     * Adds a new function call expression.
     *
     * @param routine the name of the routine.
     * @param returnType the return type of the routine.
     * @param arguments the handler which can define the function call arguments.
     * @return the expression.
     */
    default @NotNull Expression call(@NotNull String routine,
                                     @NotNull RapidType returnType,
                                     @NotNull Consumer<RapidArgumentBuilder> arguments) {
        return call(literal(routine), returnType, arguments);
    }

    /**
     * Adds a new function call expression.
     *
     * @param routine a string expression with the name of the routine.
     * @param returnType the return type of the routine.
     * @param arguments the handler which can define the function call arguments.
     * @return the expression.
     */
    @NotNull Expression call(@NotNull Expression routine,
                             @NotNull RapidType returnType,
                             @NotNull Consumer<RapidArgumentBuilder> arguments);

    /**
     * Adds the specified expression to this code block.
     *
     * @param expression the expression.
     * @return the expression.
     */
    @NotNull Expression call(@NotNull RapidFunctionCallExpression expression);

    /**
     * Adds a new procedure call statement to this code block.
     *
     * @param routine the name of the routine.
     * @param arguments the handler which can define the procedure call arguments.
     * @return this builder.
     */
    default @NotNull RapidCodeBlockBuilder invoke(@NotNull String routine,
                                                  @NotNull Consumer<RapidArgumentBuilder> arguments) {
        return invoke(literal(routine), arguments);
    }

    /**
     * Adds a new procedure call statement to this code block.
     *
     * @param routine a string expression with the name of the routine.
     * @param arguments the handler which can define the procedure call arguments.
     * @return this builder.
     */
    @NotNull RapidCodeBlockBuilder invoke(@NotNull Expression routine,
                                          @NotNull Consumer<RapidArgumentBuilder> arguments);

    /**
     * Adds the specified procedure call statement to this code block.
     *
     * @param statement the statement.
     * @return this builder.
     */
    @NotNull RapidCodeBlockBuilder invoke(@NotNull RapidProcedureCallStatement statement);


}
