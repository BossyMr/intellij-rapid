package com.bossymr.rapid.ide.editor.insight.unwrap;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.RapidIfStatement;
import com.bossymr.rapid.language.psi.RapidStatementList;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public class RapidIfUnwrapper extends RapidUnwrapper {

    public RapidIfUnwrapper() {
        super(RapidBundle.message("unwrap.if"));
    }

    @Override
    public boolean isApplicableTo(@NotNull PsiElement e) {
        if (!(e instanceof RapidIfStatement)) {
            return false;
        }
        PsiElement parent = e.getParent();
        return !(parent instanceof RapidIfStatement);
    }

    @Override
    protected void doUnwrap(@NotNull PsiElement element, @NotNull Context context) throws IncorrectOperationException {
        // FIXME:
        RapidIfStatement statement = (RapidIfStatement) element;
        RapidStatementList thenBranch = statement.getThenBranch();
        context.extractStatementList(thenBranch, element);
        context.delete(element);
    }
}
