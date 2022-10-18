package com.bossymr.rapid.language.psi.light;

import com.bossymr.rapid.language.psi.RapidParameter;
import com.bossymr.rapid.language.psi.RapidParameterGroup;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.FakePsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LightParameterGroup extends FakePsiElement implements RapidParameterGroup {

    private final boolean isOptional;
    private final List<RapidParameter> parameters;

    public LightParameterGroup(boolean isOptional, @NotNull List<RapidParameter> parameters) {
        this.isOptional = isOptional;
        this.parameters = parameters;
    }

    @Override
    public boolean isOptional() {
        return isOptional;
    }

    @Override
    public @NotNull List<RapidParameter> getParameters() {
        return parameters;
    }

    @Override
    public PsiElement getParent() {
        return null;
    }
}
