package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class AggregateExpression implements Expression {

    private final @NotNull RapidType type;
    private final @NotNull List<Expression> expressions;

    public AggregateExpression(@NotNull RapidType type, @NotNull List<Expression> expressions) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AggregateExpression that = (AggregateExpression) o;
        return Objects.equals(type, that.type) && Objects.equals(expressions, that.expressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, expressions);
    }

    @Override
    public String toString() {
        return "AggregateExpression{" +
                "type=" + type +
                ", expressions=" + expressions +
                '}';
    }
}
