package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class UnaryExpression implements Expression {

    private final @NotNull RapidType type;
    private final @NotNull UnaryOperator operator;
    private final @NotNull Expression expression;

    public UnaryExpression(@NotNull RapidType type, @NotNull UnaryOperator operator, @NotNull Expression expression) {
        this.type = type;
        this.operator = operator;
        this.expression = expression;
    }

    public @NotNull UnaryOperator getOperator() {
        return operator;
    }

    public @NotNull Expression getExpression() {
        return expression;
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitUnaryExpression(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnaryExpression that = (UnaryExpression) o;
        return Objects.equals(type, that.type) && operator == that.operator && Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, operator, expression);
    }

    @Override
    public String toString() {
        return "UnaryExpression{" +
                "type=" + type +
                ", operator=" + operator +
                ", expression=" + expression +
                '}';
    }
}
