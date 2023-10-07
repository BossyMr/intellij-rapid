package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.psi.RapidReferenceExpression;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FieldExpression implements ReferenceExpression {

    private final @Nullable SmartPsiElementPointer<RapidReferenceExpression> expression;
    private final @NotNull RapidType type;
    private final @NotNull String moduleName;
    private final @NotNull String name;

    public FieldExpression(@NotNull RapidType type, @NotNull String moduleName, @NotNull String name) {
        this(null, type, moduleName, name);
    }

    public FieldExpression(@Nullable RapidReferenceExpression expression, @NotNull RapidType type, @NotNull String moduleName, @NotNull String name) {
        this.expression = expression != null ? SmartPointerManager.createPointer(expression) : null;
        this.type = type;
        this.moduleName = moduleName;
        this.name = name;
    }

    public @NotNull String getModuleName() {
        return moduleName;
    }

    public @NotNull String getName() {
        return name;
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
        return visitor.visitFieldExpression(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldExpression that = (FieldExpression) o;
        return Objects.equals(type, that.type) && Objects.equals(moduleName, that.moduleName) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, moduleName, name);
    }

    @Override
    public String toString() {
        return moduleName + ":" + name;
    }
}
