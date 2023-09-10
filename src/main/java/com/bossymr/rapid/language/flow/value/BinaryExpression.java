package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BinaryExpression implements Expression {

    private final @NotNull RapidType type;
    private final @NotNull BinaryOperator operator;
    private final @NotNull Expression left;
    private final @NotNull Expression right;

    public BinaryExpression(@NotNull BinaryOperator operator, @NotNull RapidType type, @NotNull Expression left, @NotNull Expression right) {
        this.operator = operator;
        this.type = type;
        this.left = left;
        this.right = right;
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
    }

    public @NotNull BinaryOperator getOperator() {
        return operator;
    }

    public @NotNull Expression getLeft() {
        return left;
    }

    public @NotNull Expression getRight() {
        return right;
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitBinaryExpression(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BinaryExpression that = (BinaryExpression) o;
        return Objects.equals(type, that.type) && operator == that.operator && Objects.equals(left, that.left) && Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, operator, left, right);
    }

    @Override
    public String toString() {
        return "BinaryExpression{" +
                "type=" + type +
                ", operator=" + operator +
                ", left=" + left +
                ", right=" + right +
                '}';
    }
}
