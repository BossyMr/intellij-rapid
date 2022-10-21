package com.bossymr.rapid.language.psi.light;

import com.bossymr.rapid.language.psi.RapidComponent;
import com.bossymr.rapid.language.psi.RapidType;
import com.bossymr.rapid.language.psi.RapidTypeElement;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class LightComponent extends LightSymbol implements RapidComponent {

    private final String name;
    private final RapidType type;

    public LightComponent(@NotNull Project project, @NotNull String name, @NotNull RapidType type) {
        super(project);
        this.name = name;
        this.type = type;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
    }

    @Override
    public @Nullable RapidTypeElement getTypeElement() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LightComponent that = (LightComponent) o;
        return Objects.equals(getName(), that.getName()) && Objects.equals(getType(), that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getType());
    }

    @Override
    public String toString() {
        return "LightComponent:" + getName();
    }
}
