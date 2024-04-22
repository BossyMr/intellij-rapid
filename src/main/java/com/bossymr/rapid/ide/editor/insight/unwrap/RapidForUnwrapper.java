package com.bossymr.rapid.ide.editor.insight.unwrap;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.RapidForStatement;
import com.bossymr.rapid.language.psi.RapidIfStatement;
import com.bossymr.rapid.language.psi.RapidStatementList;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public class RapidForUnwrapper extends RapidUnwrapper {

    public RapidForUnwrapper() {
        super(RapidBundle.message("unwrap.for"));
    }

    @Override
    public boolean isApplicableTo(@NotNull PsiElement e) {
        return e instanceof RapidForStatement;
    }

    @Override
    protected void doUnwrap(@NotNull PsiElement element, @NotNull Context context) throws IncorrectOperationException {
        RapidForStatement statement = ((RapidForStatement) element);
        RapidStatementList thenBranch = statement.getStatementList();
        context.extractStatementList(thenBranch, element);
        context.delete(element);
    }
}
