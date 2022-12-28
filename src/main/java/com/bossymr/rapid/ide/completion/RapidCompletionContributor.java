package com.bossymr.rapid.ide.completion;

import com.bossymr.rapid.language.psi.RapidAttributeList;
import com.bossymr.rapid.language.psi.RapidStatement;
import com.bossymr.rapid.language.psi.RapidStatementList;
import com.bossymr.rapid.language.psi.RapidTypeElement;
import com.bossymr.rapid.language.symbol.physical.PhysicalComponent;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.physical.PhysicalRecord;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class RapidCompletionContributor extends CompletionContributor {

    public static PsiElementPattern<? extends PsiElement, ?> INSIDE_ATTRIBUTE_LIST = psiElement()
            .withAncestor(2, psiElement(RapidAttributeList.class).withParent(PhysicalModule.class));

    public static PsiElementPattern<? extends PsiElement, ?> INSIDE_MODULE = psiElement()
            .withAncestor(2, psiElement(PhysicalModule.class))
            .andNot(psiElement().afterLeaf("LOCAL", "TASK"));

    public static PsiElementPattern<? extends PsiElement, ?> INSIDE_RECORD = psiElement()
            .withSuperParent(2, psiElement(RapidTypeElement.class).andNot(psiElement().afterSibling(psiElement())))
            .withSuperParent(3, PhysicalComponent.class)
            .withSuperParent(4, PhysicalRecord.class);

    public static PsiElementPattern<? extends PsiElement, ?> INSIDE_ROUTINE = psiElement()
            .withSuperParent(2, psiElement(RapidStatement.class).andNot(psiElement().afterSibling(psiElement())))
            .withSuperParent(3, psiElement(RapidStatementList.class).andNot(psiElement().afterSibling(psiElement())))
            .withSuperParent(4, PhysicalRoutine.class);

    public static PsiElementPattern<? extends PsiElement, ?> STATEMENT = psiElement()
            .withSuperParent(2, RapidStatement.class)
            .andNot(psiElement().afterSibling(psiElement()));


    public RapidCompletionContributor() {
        extend(CompletionType.BASIC, INSIDE_ATTRIBUTE_LIST, new RapidKeywordCompletionProvider("SYSMODULE", "NOVIEW", "NOSTEPIN", "VIEWONLY", "READONLY"));
        extend(CompletionType.BASIC, INSIDE_MODULE, new RapidKeywordCompletionProvider("<TDN>", "<DDN>", "<RDN>", "LOCAL", "TASK", "ALIAS", "RECORD", "VAR", "PERS", "CONST", "FUNC", "PROC", "TRAP", "ENDMODULE"));
        extend(CompletionType.BASIC, INSIDE_MODULE.afterLeaf("LOCAL"), new RapidKeywordCompletionProvider("ALIAS", "RECORD", "VAR", "PERS", "CONST", "FUNC", "PROC", "TRAP"));
        extend(CompletionType.BASIC, INSIDE_MODULE.afterLeaf("TASK"), new RapidKeywordCompletionProvider("VAR", "PERS"));
        extend(CompletionType.BASIC, INSIDE_RECORD, new RapidKeywordCompletionProvider("ENDRECORD"));
        extend(CompletionType.BASIC, STATEMENT, new RapidKeywordCompletionProvider("GOTO", "RETURN", "RAISE", "EXIT", "RETRY", "TRYNEXT", "CONNECT", "IF", "FOR", "WHILE", "TEST"));

        extend(CompletionType.BASIC, INSIDE_ROUTINE, new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                PsiElement element = parameters.getPosition();
                PhysicalRoutine routine = PsiTreeUtil.getParentOfType(element, PhysicalRoutine.class);
                if (routine != null) {
                    String keyword = switch (routine.getAttribute()) {
                        case FUNCTION -> "ENDFUNC";
                        case PROCEDURE -> "ENDPROC";
                        case TRAP -> "ENDTRAP";
                    };
                    result.caseInsensitive().addElement(LookupElementBuilder.create(keyword));
                }
            }
        });
    }

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        super.fillCompletionVariants(parameters, result);
    }
}
