package com.bossymr.rapid.language.psi.light;

import com.bossymr.rapid.language.psi.RapidAtomic;
import com.bossymr.rapid.language.psi.RapidType;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class LightAtomic extends LightSymbol implements RapidAtomic {

    private final String name;
    private final RapidType type;

    public LightAtomic(@NotNull PsiManager manager, @NotNull String name) {
        this(manager, name, null);
    }

    public LightAtomic(@NotNull PsiManager manager, @NotNull String name, @Nullable RapidType type) {
        super(manager);
        this.name = name;
        this.type = type;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public @Nullable RapidType getType() {
        return type;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LightAtomic that = (LightAtomic) o;
        return Objects.equals(getName(), that.getName()) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), type);
    }

    @Override
    public String toString() {
        return "LightAtomic:" + getName();
    }
}
