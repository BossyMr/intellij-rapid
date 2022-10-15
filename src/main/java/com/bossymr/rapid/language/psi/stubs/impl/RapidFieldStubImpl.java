package com.bossymr.rapid.language.psi.stubs.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.bossymr.rapid.language.psi.RapidField;
import com.bossymr.rapid.language.psi.RapidField.Attribute;
import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.psi.stubs.RapidFieldStub;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidFieldStubImpl extends StubBase<RapidField> implements RapidFieldStub {

    private final Attribute attribute;
    private final String name, type, initializer;
    private final boolean isLocal, isTask;

    public RapidFieldStubImpl(@Nullable StubElement<?> parent, @NotNull Attribute attribute, @Nullable String name, @Nullable String type, @Nullable String initializer, boolean isLocal, boolean isTask) {
        super(parent, RapidStubElementTypes.FIELD);
        this.attribute = attribute;
        this.name = name;
        this.type = type;
        this.initializer = initializer;
        this.isLocal = isLocal;
        this.isTask = isTask;
    }

    @Override
    public @NotNull Attribute getAttribute() {
        return attribute;
    }

    @Override
    public boolean isLocal() {
        return isLocal;
    }

    @Override
    public boolean isTask() {
        return isTask;
    }

    @Override
    public @NonNls @Nullable String getName() {
        return name;
    }

    @Override
    public @Nullable String getType() {
        return type;
    }

    @Override
    public @Nullable String getInitializer() {
        return initializer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidFieldStubImpl that = (RapidFieldStubImpl) o;
        return isLocal() == that.isLocal() && isTask() == that.isTask() && Objects.equals(getName(), that.getName()) && Objects.equals(getType(), that.getType()) && Objects.equals(getInitializer(), that.getInitializer());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getType(), getInitializer(), isLocal(), isTask());
    }

    @Override
    public String toString() {
        return "RapidFieldStub{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", initializer='" + initializer + '\'' +
                ", isLocal=" + isLocal +
                ", isTask=" + isTask +
                '}';
    }
}
