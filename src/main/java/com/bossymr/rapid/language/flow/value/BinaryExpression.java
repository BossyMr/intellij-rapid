package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class BinaryExpression implements Expression {

    private final @Nullable SmartPsiElementPointer<RapidExpression> expression;
    private final @NotNull RapidType type;
    private final @NotNull BinaryOperator operator;
    private final @NotNull Expression left;
    private final @NotNull Expression right;

    public BinaryExpression(@NotNull BinaryOperator operator, @NotNull Expression left, @NotNull Expression right) {
        this(null, operator, left, right);
    }

    public BinaryExpression(@Nullable RapidExpression expression, @NotNull BinaryOperator operator, @NotNull Expression left, @NotNull Expression right) {
        this.expression = expression != null ? SmartPointerManager.createPointer(expression) : null;
        this.operator = operator;
        this.type = Objects.requireNonNull(getType(operator, left, right));
        this.left = left;
        this.right = right;
    }

    private static @Nullable RapidType getType(@NotNull BinaryOperator operator, @NotNull Expression left, @NotNull Expression right) {
        return switch (operator) {
            case ADD, SUBTRACT, MULTIPLY, DIVIDE, INTEGER_DIVIDE, MODULO -> {
                if(operator == BinaryOperator.AND) {
                    if (left.getType().isAssignable(RapidPrimitiveType.STRING) && left.getType().isAssignable(RapidPrimitiveType.STRING)) {
                        yield RapidPrimitiveType.STRING;
                    }
                }
                if (!(left.getType().isAssignable(RapidPrimitiveType.NUMBER) || left.getType().isAssignable(RapidPrimitiveType.DOUBLE))) {
                    yield null;
                }
                if (!(right.getType().isAssignable(RapidPrimitiveType.NUMBER) || right.getType().isAssignable(RapidPrimitiveType.DOUBLE))) {
                    yield null;
                }
                if (left.getType().isAssignable(RapidPrimitiveType.DOUBLE) || left.getType().isAssignable(RapidPrimitiveType.DOUBLE)) {
                    yield RapidPrimitiveType.DOUBLE;
                }
                yield RapidPrimitiveType.NUMBER;
            }
            case LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL -> {
                if (!(left.getType().isAssignable(RapidPrimitiveType.NUMBER) || left.getType().isAssignable(RapidPrimitiveType.DOUBLE))) {
                    yield null;
                }
                if (!(right.getType().isAssignable(RapidPrimitiveType.NUMBER) || right.getType().isAssignable(RapidPrimitiveType.DOUBLE))) {
                    yield null;
                }
                yield RapidPrimitiveType.BOOLEAN;
            }
            case EQUAL_TO, NOT_EQUAL_TO -> RapidPrimitiveType.BOOLEAN;
            case AND, XOR, OR -> {
                if (!(left.getType().isAssignable(RapidPrimitiveType.BOOLEAN))) {
                    yield null;
                }
                if (!(right.getType().isAssignable(RapidPrimitiveType.BOOLEAN))) {
                    yield null;
                }
                yield RapidPrimitiveType.BOOLEAN;
            }
        };
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
    public @Nullable RapidExpression getElement() {
        return expression != null ? expression.getElement() : null;
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
