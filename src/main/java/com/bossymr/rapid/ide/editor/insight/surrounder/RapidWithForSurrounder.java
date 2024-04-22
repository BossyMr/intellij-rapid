package com.bossymr.rapid.ide.editor.insight.surrounder;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.RapidForStatement;
import com.bossymr.rapid.language.psi.RapidIfStatement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class RapidWithForSurrounder extends RapidStatementSurrounder {

    @Override
    public String getTemplateDescription() {
        return RapidBundle.message("surrounder.with.for");
    }

    @Override
    protected @NotNull String getPrefix() {
        return "FOR";
    }

    @Override
    protected @NotNull String getSuffix() {
        return "FROM TO DO\nENDFOR";
    }
}
