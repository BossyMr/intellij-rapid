package com.bossymr.flow.instruction;


import com.bossymr.flow.value.Variable;
import com.bossymr.flow.expression.Expression;

import java.util.Objects;

/**
 * An {@code AssignmentInstruction} represents an instruction where a variable is assigned a new value.
 */
public class AssignmentInstruction extends Instruction {

    private final Variable variable;
    private final Expression expression;

    /**
     * Create a new {@code AssignmentInstruction}.
     *
     * @param predecessor the predecessor.
     * @param variable the variable being assigned.
     * @param expression the expression being assigned.
     */
    protected AssignmentInstruction(Instruction predecessor, Variable variable, Expression expression) {
        super(predecessor);
        if (!variable.getType().equals(expression.getType())) {
            throw new IllegalArgumentException("cannot assign expression of type '" + expression.getType() + "' to variable of type '" + variable.getType() + "'");
        }
        this.variable = variable;
        this.expression = expression;
    }

    /**
     * Returns the variable being assigned.
     *
     * @return the variable being assigned.
     */
    public Variable getVariable() {
        return variable;
    }

    /**
     * Returns the expression being assigned.
     *
     * @return the expression being assigned.
     */
    public Expression getExpression() {
        return expression;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AssignmentInstruction that = (AssignmentInstruction) o;
        return Objects.equals(variable, that.variable) && Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), variable, expression);
    }

    @Override public String toString() {
        return variable + " => " + expression;
    }
}
