package com.bossymr.rapid.ide.editor.insight.unwrap;

import com.bossymr.rapid.language.psi.RapidStatement;
import com.bossymr.rapid.language.psi.RapidStatementList;
import com.intellij.codeInsight.unwrap.AbstractUnwrapper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class RapidUnwrapper extends AbstractUnwrapper<RapidUnwrapper.Context> {

    public RapidUnwrapper(@NotNull @Nls String description) {
        super(description);
    }

    @Override
    protected Context createContext() {
        return new Context();
    }

    public void unwrap(@NotNull PsiElement element) throws IncorrectOperationException {
        Context context = Context.createContext();
        doUnwrap(element, context);
    }

    public static class Context extends AbstractUnwrapper.AbstractContext {

        private static @NotNull Context createContext() {
            Context context = new Context();
            context.myIsEffective = true;
            return context;
        }

        @Override
        public void delete(PsiElement e) throws IncorrectOperationException {
            super.delete(e);
        }

        public void extractStatementList(@Nullable RapidStatementList statementList, @NotNull PsiElement element) {
            if(statementList != null) {
                List<RapidStatement> statements = statementList.getStatements();
                if(!statements.isEmpty()) {
                    extract(statements.get(0), statements.get(statements.size() - 1), element);
                }
            }
        }

        @Override
        protected boolean isWhiteSpace(PsiElement element) {
            return element instanceof PsiWhiteSpace;
        }
    }
}
