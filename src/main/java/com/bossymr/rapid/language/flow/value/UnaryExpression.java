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

public class UnaryExpression implements Expression {

    private final @Nullable SmartPsiElementPointer<RapidExpression> expression;
    private final @NotNull RapidType type;
    private final @NotNull UnaryOperator operator;
    private final @NotNull Expression component;

    public UnaryExpression(@NotNull UnaryOperator operator, @NotNull Expression component) {
        this(null, operator, component);
    }

    public UnaryExpression(@Nullable RapidExpression expression, @NotNull UnaryOperator operator, @NotNull Expression component) {
        this.expression = expression != null ? SmartPointerManager.createPointer(expression) : null;
        this.type = Objects.requireNonNull(getType(operator, component), "Could not create unary expression for: " + operator + " " + component + " [" + component.getType() + "]");
        this.operator = operator;
        this.component = component;
    }

    private static @Nullable RapidType getType(@NotNull UnaryOperator operator, @NotNull Expression expression) {
        return switch (operator) {
            case NOT -> {
                if (expression.getType().isAssignable(RapidPrimitiveType.BOOLEAN)) {
                    yield RapidPrimitiveType.BOOLEAN;
                }
                yield null;
            }
            case NEGATE -> {
                if (expression.getType().isAssignable(RapidPrimitiveType.NUMBER)) {
                    yield RapidPrimitiveType.NUMBER;
                }
                if (expression.getType().isAssignable(RapidPrimitiveType.DOUBLE)) {
                    yield RapidPrimitiveType.DOUBLE;
                }
                yield null;
            }
            case PRESENT -> RapidPrimitiveType.BOOLEAN;
            case DIMENSION -> {
                if(!(expression instanceof ReferenceExpression)) {
                    yield null;
                }
                if (expression.getType().getDimensions() <= 0) {
                    yield null;
                }
                yield RapidPrimitiveType.NUMBER;
            }
        };
    }

    public @NotNull UnaryOperator getOperator() {
        return operator;
    }

    public @NotNull Expression getExpression() {
        return component;
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
    }

    @Override
    public @Nullable RapidExpression getElement() {
        return expression != null ? expression.getElement() : null;
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
        return Objects.equals(type, that.type) && operator == that.operator && Objects.equals(component, that.component);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, operator, component);
    }

    @Override
    public String toString() {
        return operator.getText() + component;
    }
}
