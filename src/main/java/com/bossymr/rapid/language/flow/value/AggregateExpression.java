package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AggregateExpression implements Expression {

    private final @Nullable SmartPsiElementPointer<RapidExpression> expression;
    private final @NotNull RapidType type;
    private final @NotNull List<Expression> expressions;

    public AggregateExpression(@NotNull RapidType type, @NotNull List<Expression> expressions) {
        this(null, type, expressions);
    }

    public AggregateExpression(@Nullable RapidExpression expression, @NotNull RapidType type, @NotNull List<Expression> expressions) {
        this.expression = expression != null ? SmartPointerManager.createPointer(expression) : null;
        this.type = type;
        this.expressions = expressions;
    }

    public @NotNull List<Expression> getComponents() {
        return expressions;
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitAggregateExpression(this);
    }

    @Override
    public @Nullable RapidExpression getElement() {
        return expression != null ? expression.getElement() : null;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        AggregateExpression that = (AggregateExpression) object;
        return Objects.equals(type, that.type) && Objects.equals(expressions, that.expressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, expressions);
    }

    @Override
    public String toString() {
        return expressions.stream()
                .map(Expression::toString)
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
