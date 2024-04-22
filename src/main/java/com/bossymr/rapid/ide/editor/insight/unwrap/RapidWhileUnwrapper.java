package com.bossymr.rapid.ide.editor.insight.unwrap;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.RapidStatementList;
import com.bossymr.rapid.language.psi.RapidWhileStatement;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public class RapidWhileUnwrapper extends RapidUnwrapper {

    public RapidWhileUnwrapper() {
        super(RapidBundle.message("unwrap.while"));
    }

    @Override
    public boolean isApplicableTo(@NotNull PsiElement e) {
        return e instanceof RapidWhileStatement;
    }

    @Override
    protected void doUnwrap(@NotNull PsiElement element, @NotNull Context context) throws IncorrectOperationException {
        RapidWhileStatement statement = ((RapidWhileStatement) element);
        RapidStatementList thenBranch = statement.getStatementList();
        context.extractStatementList(thenBranch, element);
        context.delete(element);
    }
}
