package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.type.RapidArrayType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class IndexExpression implements ReferenceExpression {

    private final @NotNull RapidType type;
    private final @NotNull ReferenceExpression variable;
    private final @NotNull Expression index;

    public IndexExpression(@NotNull ReferenceExpression variable, @NotNull Expression index) {
        RapidType variableType = variable.getType();
        this.type = variableType instanceof RapidArrayType arrayType ? arrayType.getUnderlyingType() : variableType;
        this.variable = variable;
        this.index = index;
    }

    public @NotNull ReferenceExpression getVariable() {
        return variable;
    }

    public @NotNull Expression getIndex() {
        return index;
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitIndexExpression(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexExpression that = (IndexExpression) o;
        return Objects.equals(type, that.type) && Objects.equals(variable, that.variable) && Objects.equals(index, that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, variable, index);
    }

    @Override
    public String toString() {
        return "IndexExpression{" +
                "type=" + type +
                ", variable=" + variable +
                ", index=" + index +
                '}';
    }
}
