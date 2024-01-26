package com.bossymr.rapid.ide.editor.insight.inspection.flow;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.flow.ControlFlowBlock;
import com.bossymr.rapid.language.flow.ControlFlowService;
import com.bossymr.rapid.language.flow.data.DataFlowState;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.modcommand.ModPsiUpdater;
import com.intellij.modcommand.PsiUpdateModCommandQuickFix;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class UnreachableStatementInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new RapidElementVisitor() {
            @Override
            public void visitRoutine(@NotNull PhysicalRoutine routine) {
                ControlFlowBlock block = ControlFlowService.getInstance().getDataFlow(routine);
                List<RapidStatement> statements = getUnreachableCode(routine, block);
                if (statements.isEmpty()) {
                    return;
                }
                List<TextRange> ranges = getTextRanges(statements);
                for (TextRange range : ranges) {
                    TextRange rangeInFile = getActualTextRange(range, routine);
                    RemoveUnreachableStatementFix quickFix = new RemoveUnreachableStatementFix(rangeInFile);
                    int startOffset = routine.getTextRange().getStartOffset();
                    TextRange rangeInElement = rangeInFile.shiftLeft(startOffset);
                    holder.registerProblem(routine, RapidBundle.message("inspection.message.unreachable.statement"), ProblemHighlightType.LIKE_UNUSED_SYMBOL, rangeInElement, quickFix);
                }
            }
        };
    }

    private @NotNull TextRange getActualTextRange(@NotNull TextRange range, @NotNull PhysicalRoutine routine) {
        PsiElement element = routine.getContainingFile().findElementAt(range.getStartOffset());
        if (element == null) {
            return range;
        }
        PsiElement psiElement = PsiTreeUtil.nextVisibleLeaf(element);
        if (psiElement == null) {
            return range;
        }
        range = TextRange.create(psiElement.getTextRange().getStartOffset(), range.getEndOffset());
        return range;
    }

    private @NotNull List<RapidStatement> getUnreachableCode(@NotNull PhysicalRoutine routine, @NotNull ControlFlowBlock block) {
        List<RapidStatement> statements = getStatements(routine);
        List<Instruction> instructions = block.getControlFlow().getInstructions();
        for (Instruction instruction : instructions) {
            PsiElement element = instruction.getElement();
            if (!(element instanceof RapidStatement statement)) {
                continue;
            }
            Set<DataFlowState> states = block.getDataFlow(instruction);
            if (states.isEmpty()) {
                continue;
            }
            statements.remove(statement);
        }
        return statements;
    }

    private @NotNull List<RapidStatement> getStatements(@NotNull PhysicalRoutine routine) {
        List<RapidStatement> statements = new ArrayList<>();
        Deque<RapidStatement> queue = new ArrayDeque<>();
        for (RapidStatementList statementList : routine.getStatementLists()) {
            getStatements(statements, queue, statementList);
        }
        while (!(queue.isEmpty())) {
            RapidStatement statement = queue.removeLast();
            if (statement instanceof RapidIfStatement ifStatement) {
                getStatements(statements, queue, ifStatement.getThenBranch());
                getStatements(statements, queue, ifStatement.getElseBranch());
            } else if (statement instanceof RapidForStatement forStatement) {
                getStatements(statements, queue, forStatement.getStatementList());
            } else if (statement instanceof RapidWhileStatement whileStatement) {
                getStatements(statements, queue, whileStatement.getStatementList());
            } else if (statement instanceof RapidTestStatement testStatement) {
                for (RapidTestCaseStatement caseStatement : testStatement.getTestCaseStatements()) {
                    getStatements(statements, queue, caseStatement.getStatements());
                }
            }
        }
        return statements;
    }

    private void getStatements(@NotNull List<RapidStatement> statements, @NotNull Deque<RapidStatement> queue, @Nullable RapidStatementList statementList) {
        if (statementList == null) {
            return;
        }
        for (RapidStatement statement : statementList.getStatements()) {
            if (statement instanceof RapidGotoStatement) {
                continue;
            }
            statements.add(statement);
            if (statement instanceof RapidIfStatement || statement instanceof RapidForStatement || statement instanceof RapidWhileStatement || statement instanceof RapidTestStatement) {
                queue.add(statement);
            }
        }
    }

    private @NotNull List<TextRange> getTextRanges(@NotNull List<RapidStatement> statements) {
        List<TextRange> ranges = new ArrayList<>();
        ranges.add(getTextRange(statements.get(0)));
        for (RapidStatement statement : statements.subList(1, statements.size())) {
            TextRange textRange = getTextRange(statement);
            TextRange leftNeighbour = ranges.get(ranges.size() - 1);
            if (leftNeighbour.intersects(textRange)) {
                ranges.remove(ranges.size() - 1);
                ranges.add(leftNeighbour.union(textRange));
            } else {
                ranges.add(textRange);
            }
        }
        return ranges;
    }

    private @NotNull TextRange getTextRange(@NotNull RapidStatement statement) {
        TextRange range = statement.getTextRange();
        PsiElement nextSibling = PsiTreeUtil.getNextSiblingOfType(statement, RapidStatement.class);
        if (nextSibling instanceof RapidGotoStatement) {
            range = range.union(nextSibling.getTextRange());
        }
        PsiElement sibling = PsiTreeUtil.prevVisibleLeaf(statement);
        if (sibling == null) {
            return range;
        }
        return TextRange.create(sibling.getTextRange().getEndOffset(), range.getEndOffset());
    }

    @SuppressWarnings("UnstableApiUsage")
    private static class RemoveUnreachableStatementFix extends PsiUpdateModCommandQuickFix {

        private final @NotNull TextRange range;

        private RemoveUnreachableStatementFix(@NotNull TextRange range) {
            this.range = range;
        }

        @Override
        protected void applyFix(@NotNull Project project, @NotNull PsiElement element, @NotNull ModPsiUpdater updater) {
            PsiFile file = element.getContainingFile();
            PsiDocumentManager manager = PsiDocumentManager.getInstance(project);
            Document document = manager.getDocument(file);
            if (document == null) {
                return;
            }
            document.deleteString(range.getStartOffset(), range.getEndOffset());
            if (isInCompactIfStatement(file)) {
                document.replaceString(range.getStartOffset(), range.getEndOffset(), "THEN ENDIF");
            }
        }

        private boolean isInCompactIfStatement(@NotNull PsiFile file) {
            PsiElement startElement = file.findElementAt(range.getStartOffset());
            RapidStatement statement = PsiTreeUtil.getParentOfType(startElement, RapidStatement.class);
            if (statement == null) {
                return false;
            }
            if (!(statement.getParent() instanceof RapidStatementList statementList)) {
                return false;
            }
            if (!(statementList.getParent() instanceof RapidIfStatement ifStatement)) {
                return false;
            }
            return ifStatement.isCompact();
        }

        @Override
        public @IntentionFamilyName @NotNull String getFamilyName() {
            return RapidBundle.message("quick.fix.text.delete.unreachable.code");
        }
    }
}
