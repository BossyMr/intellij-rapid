package com.bossymr.flow.expression;

import com.bossymr.flow.value.Variable;
import com.bossymr.flow.value.type.ValueType;
import io.github.cvc5.Term;
import io.github.cvc5.TermManager;

import java.util.function.Function;

/**
 * An {@code Expression} represents an expression.
 */
public interface Expression {

    /**
     * Returns the return type of this expression.
     *
     * @return the return type of this expression.
     */
    ValueType getType();

    /**
     * Converts this expression into a constraint.
     *
     * @param manager the manager used to create the constraint.
     * @param variable a function used to convert variables into constraints.
     * @return this expression as a constraint.
     */
    Term getConstraint(TermManager manager, Function<Variable, Term> variable);

}
