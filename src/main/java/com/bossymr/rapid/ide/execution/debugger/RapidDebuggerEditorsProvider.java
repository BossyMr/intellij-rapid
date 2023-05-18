package com.bossymr.rapid.ide.execution.debugger;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.psi.RapidElementFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProviderBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidDebuggerEditorsProvider extends XDebuggerEditorsProviderBase {

    @Override
    protected @Nullable PsiFile createExpressionCodeFragment(@NotNull Project project, @NotNull String text, @Nullable PsiElement context, boolean isPhysical) {
        return RapidElementFactory.getInstance(project).createExpressionCodeFragment(text, context, isPhysical);
    }

    @Override
    public @NotNull FileType getFileType() {
        return RapidFileType.getInstance();
    }
}
