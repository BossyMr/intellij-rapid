package com.bossymr.rapid.ide.editor.insight.surrounder;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.RapidIfStatement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class RapidWithIfSurrounder extends RapidStatementSurrounder {

    @Override
    public String getTemplateDescription() {
        return RapidBundle.message("surrounder.with.if");
    }

    @Override
    public @NotNull RapidIfStatement surroundElements(@NotNull Project project, PsiElement @NotNull [] elements, @NotNull String condition) {
        return (RapidIfStatement) super.surroundElements(project, elements, condition);
    }

    @Override
    protected @NotNull String getPrefix() {
        return "IF";
    }

    @Override
    protected @NotNull String getSuffix() {
        return "THEN\nENDIF";
    }
}
