package com.bossymr.rapid.ide.completion;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.physical.PhysicalParameter;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.*;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.StandardPatterns.or;
import static com.intellij.patterns.StandardPatterns.string;

public class RapidKeywordCompletionContributor extends CompletionContributor {

    public static ElementPattern<PsiElement> BEFORE_MODULE = psiElement().andNot(psiElement().afterLeaf(psiElement()));

    public static ElementPattern<PsiElement> ATTRIBUTE_LIST = psiElement().withSuperParent(2, RapidAttributeList.class);

    public static PsiElementPattern<PsiElement, ?> INSIDE_MODULE = psiElement().withSuperParent(2, PhysicalModule.class);

    public static ElementPattern<PsiElement> PARAMETER_TYPE = psiElement()
            .withParent(RapidReferenceExpression.class)
            .withSuperParent(2, psiElement(RapidTypeElement.class))
            .withSuperParent(3, psiElement(PhysicalParameter.class))
            .andNot(psiElement().afterLeaf("VAR", "PERS", "INOUT"));

    public static ElementPattern<PsiElement> STATEMENT = or(
            psiElement().withParent(RapidStatementList.class),
            // The start of a new statement.
            psiElement()
                    .withParent(RapidReferenceExpression.class)
                    .withSuperParent(2, RapidProcedureCallStatement.class)
                    .andNot(psiElement().afterSibling(psiElement())));

    public static ElementPattern<PsiElement> CONNECT_STATEMENT_WITH = psiElement()
            .withParent(RapidReferenceExpression.class)
            .withSuperParent(2, RapidConnectStatement.class)
            .afterLeafSkipping(psiElement().withParent(RapidExpression.class), psiElement().withText("CONNECT"));

    public static ElementPattern<PsiElement> IF_STATEMENT_THEN = psiElement()
            .withParent(RapidReferenceExpression.class)
            .afterLeafSkipping(psiElement().withParent(RapidExpression.class), psiElement().withText(string().oneOf("IF", "ELSEIF")));

    public RapidKeywordCompletionContributor() {
        extend(CompletionType.BASIC, BEFORE_MODULE, new RapidKeywordCompletionProvider("MODULE"));
        extend(CompletionType.BASIC, ATTRIBUTE_LIST, new RapidAttributeListCompletionProvider());
        extend(CompletionType.BASIC, INSIDE_MODULE.andNot(psiElement().afterLeaf("LOCAL", "TASK")), new RapidKeywordCompletionProvider("LOCAL", "TASK", "ALIAS", "RECORD", "VAR", "PERS", "CONST", "FUNC", "PROC", "TRAP"));
        extend(CompletionType.BASIC, INSIDE_MODULE.and(psiElement().afterLeaf("LOCAL")), new RapidKeywordCompletionProvider("VAR", "PERS", "CONST", "RECORD, ALIAS", "FUNC", "PROC", "TRAP"));
        extend(CompletionType.BASIC, INSIDE_MODULE.and(psiElement().afterLeaf("TASK")), new RapidKeywordCompletionProvider("VAR", "PERS"));
        extend(CompletionType.BASIC, PARAMETER_TYPE, new RapidKeywordCompletionProvider("VAR", "PERS", "INOUT"));

        // TODO: 2023-05-01 Technically UNDO, ERROR, and BACKWARD should only be displayed if they don't already exist, and should be inserted in the correct order.
        extend(CompletionType.BASIC, STATEMENT, new RapidKeywordCompletionProvider("GOTO", "RAISE", "CONNECT", "IF", "FOR", "WHILE", "TEST", "VAR", "PERS", "CONST", "ERROR", "BACKWARD", "UNDO"));
        extend(CompletionType.BASIC, STATEMENT, new RapidKeywordCompletionProvider(TailType.SEMICOLON, "EXIT"));

        // Use the correct tail type depending on if the routine has a return value.
        extend(CompletionType.BASIC, STATEMENT, new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                PhysicalRoutine routine = PsiTreeUtil.getParentOfType(parameters.getPosition(), PhysicalRoutine.class);
                if (routine == null) {
                    return;
                }
                TailType tailType = routine.getRoutineType() == RoutineType.FUNCTION ? TailType.SPACE : TailType.SEMICOLON;
                result.caseInsensitive().addElement(RapidKeywordCompletionProvider.getLookupElement(tailType, "RETURN", null));
            }
        });

        // Add statements only available in an error clause.
        extend(CompletionType.BASIC, STATEMENT, new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                RapidStatementList statementList = PsiTreeUtil.getParentOfType(parameters.getPosition(), RapidStatementList.class);
                if (statementList == null) {
                    return;
                }
                if (statementList.getAttribute() == StatementListType.ERROR_CLAUSE) {
                    result.caseInsensitive().addElement(RapidKeywordCompletionProvider.getLookupElement(TailType.SEMICOLON, "RETRY", null));
                    result.caseInsensitive().addElement(RapidKeywordCompletionProvider.getLookupElement(TailType.SEMICOLON, "TRYNEXT", null));
                }
            }
        });

        extend(CompletionType.BASIC, CONNECT_STATEMENT_WITH, new RapidKeywordCompletionProvider("WITH"));
        extend(CompletionType.BASIC, IF_STATEMENT_THEN, new RapidKeywordCompletionProvider("THEN"));

        // TODO: 2023-05-01 Add alternatives for ENDMODULE and ENDRECORD if at the end of a record or module.

        // TODO: 2023-05-01 Add alternatives for THEN, FROM, TO, STEP, ENDFOR, ENDIF and CASE
    }
}
