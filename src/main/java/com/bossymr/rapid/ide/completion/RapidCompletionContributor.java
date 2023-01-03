package com.bossymr.rapid.ide.completion;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.physical.*;
import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class RapidCompletionContributor extends CompletionContributor {

    public static PsiElementPattern<? extends PsiElement, ?> INSIDE_ATTRIBUTE_LIST = psiElement()
            .withAncestor(2, psiElement(RapidAttributeList.class).withParent(PhysicalModule.class));

    public static PsiElementPattern<? extends PsiElement, ?> INSIDE_MODULE = psiElement()
            .withSuperParent(2, psiElement(PhysicalModule.class));

    public static PsiElementPattern<? extends PsiElement, ?> INSIDE_RECORD = psiElement()
            .withSuperParent(2, psiElement(RapidTypeElement.class).withParent(psiElement(PhysicalComponent.class).withParent(PhysicalRecord.class)))
            .andOr(psiElement().beforeLeaf("MODULE", "ENDMODULE", "<TDN>", "<DDN>", "<RDN>", "LOCAL", "TASK", "ALIAS", "RECORD", "VAR", "PERS", "CONST", "FUNC", "PROC", "TRAP"),
                    StandardPatterns.not(psiElement().beforeLeaf(psiElement())));

    public static PsiElementPattern<? extends PsiElement, ?> END_OF_ROUTINE = psiElement()
            .withParent(psiElement(RapidStatementList.class).withParent(psiElement(PhysicalRoutine.class)))
            .andOr(psiElement().beforeLeaf("MODULE", "ENDMODULE", "<TDN>", "<DDN>", "<RDN>", "LOCAL", "TASK", "ALIAS", "RECORD", "VAR", "PERS", "CONST", "FUNC", "PROC", "TRAP"),
                    StandardPatterns.not(psiElement().beforeLeaf(psiElement())));

    public static PsiElementPattern<? extends PsiElement, ?> STATEMENT = psiElement()
            .andOr(psiElement().withParent(RapidStatementList.class),
                    psiElement().withParent(psiElement(RapidReferenceExpression.class).andNot(psiElement().afterSibling(psiElement()))));

    public static PsiElementPattern<? extends PsiElement, ?> CONNECT_STATEMENT_WITH = psiElement()
            .withParent(psiElement(RapidReferenceExpression.class)
                    .and(psiElement().withParent(RapidConnectStatement.class))
                    .and(psiElement().afterSiblingSkipping(psiElement().whitespaceCommentEmptyOrError(), psiElement(RapidExpression.class).afterLeaf("CONNECT"))));

    public static PsiElementPattern<? extends PsiElement, ?> PARAMETER = psiElement()
            .withParent(psiElement(RapidReferenceExpression.class).withParent(psiElement(RapidTypeElement.class).withParent(psiElement(PhysicalParameter.class))))
            .andNot(psiElement().afterLeaf("VAR", "PERS", "CONST"));

    public static PsiElementPattern<? extends PsiElement, ?> IF_STATEMENT_THEN = psiElement()
            .withSuperParent(1, RapidReferenceExpression.class)
            .withSuperParent(2, psiElement(RapidProcedureCallStatement.class).andNot(psiElement().afterSibling(psiElement())))
            .withSuperParent(3, psiElement(RapidStatementList.class).afterSiblingSkipping(psiElement().whitespaceCommentEmptyOrError(),
                    psiElement(RapidExpression.class).afterLeaf("IF", "ELSEIF")))
            .withSuperParent(4, RapidIfStatement.class);

    public static PsiElementPattern<? extends PsiElement, ?> WHILE_STATEMENT_DO = psiElement()
            .withSuperParent(1, RapidReferenceExpression.class)
            .withSuperParent(2, psiElement(RapidProcedureCallStatement.class).andNot(psiElement().afterSibling(psiElement())))
            .withSuperParent(3, psiElement(RapidStatementList.class).afterSiblingSkipping(psiElement().whitespaceCommentEmptyOrError(),
                    psiElement(RapidExpression.class).afterLeaf("WHILE")))
            .withSuperParent(4, RapidWhileStatement.class);

    public static PsiElementPattern<? extends PsiElement, ?> WHILE_STATEMENT_ENDWHILE = psiElement()
            .withSuperParent(1, RapidReferenceExpression.class)
            .withSuperParent(2, RapidProcedureCallStatement.class)
            .withSuperParent(3, RapidStatementList.class)
            .withSuperParent(4, RapidWhileStatement.class);

    public static PsiElementPattern<? extends PsiElement, ?> IF_STATEMENT_ELSE_IF = psiElement()
            .withSuperParent(1, RapidReferenceExpression.class)
            .withSuperParent(2, RapidProcedureCallStatement.class)
            .withSuperParent(3, RapidStatementList.class)
            .withSuperParent(4, RapidIfStatement.class);

    public static PsiElementPattern<? extends PsiElement, ?> FOR_STATEMENT_FROM = psiElement()
            .withSuperParent(1, RapidReferenceExpression.class)
            .withSuperParent(2, psiElement(RapidProcedureCallStatement.class)
                    .afterSibling(psiElement(RapidForStatement.class)
                            .withLastChildSkipping(psiElement().whitespaceCommentEmptyOrError(), psiElement(PhysicalTargetVariable.class).afterLeaf("FOR").andNot(psiElement().beforeLeaf("FROM", "TO", "STEP", "DO")))));

    public static PsiElementPattern<? extends PsiElement, ?> FOR_STATEMENT_TO = psiElement()
            .withSuperParent(1, RapidReferenceExpression.class)
            .withSuperParent(2, psiElement(RapidProcedureCallStatement.class)
                    .afterSibling(psiElement(RapidForStatement.class)
                            .withLastChildSkipping(psiElement().whitespaceCommentEmptyOrError(), psiElement(RapidExpression.class).afterLeaf("FROM").andNot(psiElement().beforeLeaf("FROM", "TO", "STEP", "DO")))));

    public static PsiElementPattern<? extends PsiElement, ?> FOR_STATEMENT_STEP = psiElement()
            .withSuperParent(1, RapidReferenceExpression.class)
            .withSuperParent(2, psiElement(RapidProcedureCallStatement.class)
                    .afterSibling(psiElement(RapidForStatement.class)
                            .withLastChildSkipping(psiElement().whitespaceCommentEmptyOrError(), psiElement(RapidExpression.class).afterLeaf("TO").andNot(psiElement().beforeLeaf("FROM", "TO", "STEP", "DO")))));

    public static PsiElementPattern<? extends PsiElement, ?> FOR_STATEMENT_DO = psiElement()
            .withSuperParent(1, RapidReferenceExpression.class)
            .withSuperParent(2, psiElement(RapidProcedureCallStatement.class)
                    .afterSibling(psiElement(RapidForStatement.class)
                            .withLastChildSkipping(psiElement().whitespaceCommentEmptyOrError(), psiElement(RapidExpression.class).afterLeaf("TO", "STEP").andNot(psiElement().beforeLeaf("FROM", "TO", "STEP", "DO")))));

    public static PsiElementPattern<? extends PsiElement, ?> FOR_STATEMENT_ENDFOR = psiElement()
            .withSuperParent(1, RapidReferenceExpression.class)
            .withSuperParent(2, RapidProcedureCallStatement.class)
            .withSuperParent(3, RapidStatementList.class)
            .withSuperParent(4, RapidForStatement.class);

    public static PsiElementPattern<? extends PsiElement, ?> TEST_STATEMENT_CASE = psiElement()
            .withSuperParent(1, RapidReferenceExpression.class)
            .withSuperParent(2, RapidProcedureCallStatement.class)
            .andOr(psiElement().withSuperParent(2, psiElement().afterSibling(psiElement(RapidTestStatement.class)
                            .withLastChildSkipping(psiElement().whitespaceCommentEmptyOrError(), psiElement(RapidExpression.class).afterLeaf("TEST")))),
                    psiElement().withSuperParent(3, psiElement(RapidStatementList.class).withParent(RapidTestCaseStatement.class))
            );

    public RapidCompletionContributor() {
        extend(CompletionType.BASIC, INSIDE_ATTRIBUTE_LIST, new RapidKeywordCompletionProvider("SYSMODULE", "NOVIEW", "NOSTEPIN", "VIEWONLY", "READONLY"));
        extend(CompletionType.BASIC, INSIDE_MODULE.andNot(psiElement().afterLeaf("LOCAL", "TASK")), new RapidKeywordCompletionProvider("<TDN>", "<DDN>", "<RDN>", "LOCAL", "TASK", "ALIAS", "RECORD", "VAR", "PERS", "CONST", "FUNC", "PROC", "TRAP", "ENDMODULE"));

        extend(CompletionType.BASIC, INSIDE_MODULE.afterLeaf("LOCAL"), new RapidKeywordCompletionProvider("ALIAS", "RECORD", "VAR", "PERS", "CONST", "FUNC", "PROC", "TRAP"));
        extend(CompletionType.BASIC, INSIDE_MODULE.afterLeaf("TASK"), new RapidKeywordCompletionProvider("VAR", "PERS"));

        extend(CompletionType.BASIC, INSIDE_RECORD, new RapidKeywordCompletionProvider(TailType.NONE, "ENDRECORD"));

        extend(CompletionType.BASIC, STATEMENT, new RapidKeywordCompletionProvider("GOTO", "RAISE", "CONNECT", "IF", "FOR", "WHILE", "TEST", "VAR", "PERS", "CONST", "ERROR", "BACKWARD", "UNDO"));
        extend(CompletionType.BASIC, STATEMENT, new RapidKeywordCompletionProvider(TailType.SEMICOLON, "EXIT"));

        extend(CompletionType.BASIC, CONNECT_STATEMENT_WITH, new RapidKeywordCompletionProvider("WITH"));

        extend(CompletionType.BASIC, PARAMETER, new RapidKeywordCompletionProvider("VAR", "PERS", "INOUT"));

        extend(CompletionType.BASIC, IF_STATEMENT_THEN, new RapidKeywordCompletionProvider("THEN"));
        extend(CompletionType.BASIC, IF_STATEMENT_ELSE_IF, new RapidKeywordCompletionProvider("ELSE", "ELSEIF", "ENDIF"));

        extend(CompletionType.BASIC, FOR_STATEMENT_FROM, new RapidKeywordCompletionProvider("FROM"));
        extend(CompletionType.BASIC, FOR_STATEMENT_TO, new RapidKeywordCompletionProvider("TO"));
        extend(CompletionType.BASIC, FOR_STATEMENT_STEP, new RapidKeywordCompletionProvider("STEP"));
        extend(CompletionType.BASIC, FOR_STATEMENT_DO, new RapidKeywordCompletionProvider("DO"));
        extend(CompletionType.BASIC, FOR_STATEMENT_ENDFOR, new RapidKeywordCompletionProvider("ENDFOR"));

        extend(CompletionType.BASIC, WHILE_STATEMENT_DO, new RapidKeywordCompletionProvider("DO"));
        extend(CompletionType.BASIC, WHILE_STATEMENT_ENDWHILE, new RapidKeywordCompletionProvider("ENDWHILE"));

        extend(CompletionType.BASIC, TEST_STATEMENT_CASE, new RapidKeywordCompletionProvider("CASE", "ENDTEST"));
        extend(CompletionType.BASIC, TEST_STATEMENT_CASE, new RapidKeywordCompletionProvider(TailType.CASE_COLON, "DEFAULT"));

        extend(CompletionType.BASIC, STATEMENT, new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                PhysicalRoutine routine = PsiTreeUtil.getParentOfType(parameters.getPosition(), PhysicalRoutine.class);
                if (routine == null) return;
                TailType tailType = routine.getAttribute() != RapidRoutine.Attribute.FUNCTION ? TailType.SEMICOLON : TailType.SPACE;
                LookupElement lookupElement = RapidKeywordCompletionProvider.getLookupElement(tailType, "RETURN", null);
                result.caseInsensitive().addElement(lookupElement);
            }
        });

        extend(CompletionType.BASIC, STATEMENT, new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                RapidStatementList statementList = PsiTreeUtil.getParentOfType(parameters.getPosition(), RapidStatementList.class);
                if (statementList == null) return;
                if (statementList.getAttribute() == RapidStatementList.Attribute.ERROR_CLAUSE) {
                    result.caseInsensitive().addElement(RapidKeywordCompletionProvider.getLookupElement(TailType.SEMICOLON, "RETRY", null));
                    result.caseInsensitive().addElement(RapidKeywordCompletionProvider.getLookupElement(TailType.SEMICOLON, "TRYNEXT", null));
                }
            }
        });

        extend(CompletionType.BASIC, END_OF_ROUTINE, new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                PhysicalRoutine routine = PsiTreeUtil.getParentOfType(parameters.getPosition(), PhysicalRoutine.class);
                if (routine == null) return;
                String keyword = switch (routine.getAttribute()) {
                    case FUNCTION -> "ENDFUNC";
                    case PROCEDURE -> "ENDPROC";
                    case TRAP -> "ENDTRAP";
                };
                result.caseInsensitive().addElement(LookupElementBuilder.create(keyword).bold());
            }
        });
    }
}
