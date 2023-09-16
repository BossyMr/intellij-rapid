package com.bossymr.rapid.ide.editor.insight.inspection.flow;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.data.DataFlow;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.psi.RapidBinaryExpression;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidReferenceExpression;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ConstantValueInspection extends LocalInspectionTool {

    private static @Nullable Instruction getInstruction(@NotNull RapidExpression expression, @NotNull BasicBlock basicBlock) {
        for (LinearInstruction instruction : basicBlock.getInstructions()) {
            PsiElement element = instruction.element();
            if (element != null && element.isEquivalentTo(expression)) {
                return instruction;
            }
        }
        BranchingInstruction terminator = basicBlock.getTerminator();
        PsiElement element = terminator.element();
        if (element != null && element.isEquivalentTo(expression)) {
            return terminator;
        }
        return null;
    }

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new RapidElementVisitor() {
            @Override
            public void visitExpression(@NotNull RapidExpression expression) {
                if (!(expression instanceof RapidBinaryExpression || expression instanceof RapidReferenceExpression)) {
                    return;
                }
                PhysicalRoutine routine = PhysicalRoutine.getRoutine(expression);
                if (routine == null) {
                    return;
                }
                DataFlow dataFlow = ControlFlowService.getInstance().getDataFlow(expression);
                Block.FunctionBlock functionBlock = getBlock(routine, dataFlow.getControlFlow());
                if (functionBlock == null) {
                    return;
                }
                BasicBlock basicBlock = getBasicBlock(functionBlock, expression);
                if (basicBlock == null) {
                    return;
                }
                Instruction instruction = Objects.requireNonNull(getInstruction(expression, basicBlock));
                if (instruction instanceof LinearInstruction.AssignmentInstruction assignmentInstruction) {
                    ReferenceExpression variable = assignmentInstruction.variable();
                    DataFlowBlock block = dataFlow.getBlock(basicBlock);
                    if (block == null) {
                        return;
                    }
                    Expression operation = assignmentInstruction.value();
                    for (Expression component : operation.getComponents()) {
                        if (component instanceof ReferenceExpression referenceExpression) {
                            RapidExpression element = referenceExpression.getElement();
                            if (!(element instanceof RapidReferenceExpression expr)) {
                                continue;
                            }
                            Optionality optionality = block.getOptionality(referenceExpression);
                            if (optionality == Optionality.MISSING) {
                                holder.registerProblem(instruction.element(), RapidBundle.message("inspection.message.missing.variable", expr.getCanonicalText()));
                            }
                            if (optionality == Optionality.UNKNOWN) {
                                holder.registerProblem(instruction.element(), RapidBundle.message("inspection.message.unknown.variable", expr.getCanonicalText()));
                            }
                        }
                    }
                    if (!(variable.getType().isAssignable(RapidPrimitiveType.BOOLEAN))) {
                        return;
                    }
                    BooleanValue constraint = block.getConstraint(variable);
                    if (constraint == BooleanValue.ALWAYS_TRUE || constraint == BooleanValue.ALWAYS_FALSE) {
                        String string = constraint == BooleanValue.ALWAYS_TRUE ? "true" : "false";
                        holder.registerProblem(expression, RapidBundle.message("inspection.message.constant.expression", string));
                    }
                }
            }
        };
    }

    private @Nullable BasicBlock getBasicBlock(@NotNull Block block, @NotNull RapidExpression expression) {
        for (BasicBlock basicBlock : block.getBasicBlocks()) {
            Instruction instruction = getInstruction(expression, basicBlock);
            if (instruction != null) {
                return basicBlock;
            }
        }
        return null;
    }

    private @Nullable Block.FunctionBlock getBlock(@NotNull PhysicalRoutine routine, @NotNull ControlFlow controlFlow) {
        for (Block block : controlFlow.getBlocks()) {
            if (!(block instanceof Block.FunctionBlock functionBlock)) {
                continue;
            }
            if (Objects.equals(block.getElement().getCanonicalName(), routine.getCanonicalName())) {
                return functionBlock;
            }
        }
        return null;
    }
}
