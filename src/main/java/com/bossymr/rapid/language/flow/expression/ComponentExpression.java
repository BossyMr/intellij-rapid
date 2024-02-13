package com.bossymr.rapid.language.flow.expression;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.psi.RapidReferenceExpression;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ComponentExpression implements ReferenceExpression {

    private final @Nullable SmartPsiElementPointer<RapidReferenceExpression> expression;
    private final @NotNull RapidType type;
    private final @NotNull ReferenceExpression variable;
    private final @NotNull String component;

    public ComponentExpression(@NotNull RapidType type, @NotNull ReferenceExpression variable, @NotNull String component) {
        this(null, type, variable, component);
    }

    public ComponentExpression(@Nullable RapidReferenceExpression expression, @NotNull RapidType type, @NotNull ReferenceExpression variable, @NotNull String component) {
        this.expression = expression != null ? SmartPointerManager.createPointer(expression) : null;
        if (expression != null) {
            if (expression.getQualifier() == null) {
                throw new IllegalArgumentException("Cannot create ComponentExpression with expression: " + expression);
            }
        }
        this.type = type;
        this.variable = variable;
        this.component = component;
    }

    public @NotNull ReferenceExpression getVariable() {
        return variable;
    }

    public @NotNull String getComponent() {
        return component;
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
    }

    @Override
    public @Nullable RapidReferenceExpression getElement() {
        return expression != null ? expression.getElement() : null;
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitComponentExpression(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentExpression that = (ComponentExpression) o;
        return Objects.equals(type, that.type) && Objects.equals(variable, that.variable) && Objects.equals(component, that.component);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, variable, component);
    }

    @Override
    public String toString() {
        return variable + "." + component;
    }
}
