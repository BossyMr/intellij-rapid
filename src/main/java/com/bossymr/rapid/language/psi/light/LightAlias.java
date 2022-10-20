package com.bossymr.rapid.language.psi.light;

import com.bossymr.rapid.language.psi.RapidAlias;
import com.bossymr.rapid.language.psi.RapidType;
import com.bossymr.rapid.language.psi.RapidTypeElement;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class LightAlias extends LightSymbol implements RapidAlias {

    private final String name;
    private final RapidType type;

    public LightAlias(@NotNull PsiManager manager, @NotNull String name, @NotNull RapidType type) {
        super(manager);
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
    public boolean isLocal() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LightAlias that = (LightAlias) o;
        return Objects.equals(getName(), that.getName()) && Objects.equals(getType(), that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getType());
    }

    @Override
    public String toString() {
        return "LightAlias:" + getName();
    }
}
