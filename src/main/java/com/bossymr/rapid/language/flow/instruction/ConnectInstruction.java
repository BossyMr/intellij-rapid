package com.bossymr.rapid.language.flow.instruction;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.expression.Expression;
import com.bossymr.rapid.language.flow.expression.ReferenceExpression;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ConnectInstruction extends Instruction {

    private final @NotNull ReferenceExpression variable;
    private final @NotNull Expression expression;

    public ConnectInstruction(@NotNull Block block, @Nullable PsiElement element, @NotNull ReferenceExpression variable, @NotNull Expression expression) {
        super(block, element);
        if (!(RapidPrimitiveType.NUMBER.isAssignable(expression.getType()))) {
            throw new IllegalArgumentException("Invalid connect target type: " + expression.getType());
        }
        this.variable = variable;
        this.expression = expression;
    }

    public @NotNull ReferenceExpression getVariable() {
        return variable;
    }

    public @NotNull Expression getExpression() {
        return expression;
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitConnectInstruction(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ConnectInstruction that = (ConnectInstruction) o;
        return Objects.equals(variable, that.variable) && Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), variable, expression);
    }

    @Override
    public String toString() {
        return "connect " + variable + " with " + expression;
    }
}
