package com.bossymr.rapid.ide.editor.insight.surrounder;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.RapidIfStatement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class RapidWithWhileSurrounder extends RapidStatementSurrounder {

    @Override
    public String getTemplateDescription() {
        return RapidBundle.message("surrounder.with.while");
    }

    @Override
    protected @NotNull String getPrefix() {
        return "WHILE";
    }

    @Override
    protected @NotNull String getSuffix() {
        return "DO\nENDWHILE";
    }
}
