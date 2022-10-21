package com.bossymr.rapid.language.psi.light;

import com.bossymr.rapid.language.RapidLanguage;
import com.bossymr.rapid.language.psi.RapidParameter;
import com.bossymr.rapid.language.psi.RapidParameterGroup;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LightParameterGroup extends LightElement implements RapidParameterGroup {

    private final boolean isOptional;
    private final List<RapidParameter> parameters;

    public LightParameterGroup(@NotNull Project project, boolean isOptional, @NotNull List<RapidParameter> parameters) {
        super(PsiManager.getInstance(project), RapidLanguage.INSTANCE);
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

    @Override
    public String toString() {
        return "RapidParameterGroup";
    }
}
