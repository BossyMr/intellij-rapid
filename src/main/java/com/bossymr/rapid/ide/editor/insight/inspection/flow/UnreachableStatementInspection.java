package com.bossymr.rapid.ide.editor.insight.inspection.flow;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.editor.insight.fix.SubstituteRangeFix;
import com.bossymr.rapid.language.flow.ControlFlowBlock;
import com.bossymr.rapid.language.flow.ControlFlowService;
import com.bossymr.rapid.language.flow.data.DataFlowState;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.intellij.codeInspection.*;
import com.intellij.openapi.util.TextRange;
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
                InspectionManager manager = InspectionManager.getInstance(routine.getProject());
                PsiFile containingFile = routine.getContainingFile();
                for (TextRange range : ranges) {
                    SubstituteRangeFix quickFix;
                    TextRange rangeInElement = normalizeTextRange(range, routine);
                    if (isChildOfCompactIfStatement(containingFile, rangeInElement)) {
                        quickFix = SubstituteRangeFix.modify(RapidBundle.message("quick.fix.text.delete.unreachable.code"), routine.getContainingFile(), rangeInElement, "THEN ENDIF");
                    } else {
                        quickFix = SubstituteRangeFix.delete(RapidBundle.message("quick.fix.text.delete.unreachable.code"), routine.getContainingFile(), rangeInElement);
                    }

                    ProblemDescriptor descriptor = manager.createProblemDescriptor(routine, rangeInElement.shiftLeft(routine.getTextRange().getStartOffset()), RapidBundle.message("inspection.message.unreachable.statement"), ProblemHighlightType.LIKE_UNUSED_SYMBOL, isOnTheFly, quickFix);
                    holder.registerProblem(descriptor);
                }
            }
        };
    }

    private boolean isChildOfCompactIfStatement(@NotNull PsiFile file, @NotNull TextRange range) {
        RapidStatement statement = PsiTreeUtil.getParentOfType(file.findElementAt(range.getStartOffset()), RapidStatement.class);
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

    private @NotNull TextRange normalizeTextRange(@NotNull TextRange range, @NotNull PhysicalRoutine routine) {
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
}
