package com.bossymr.rapid.ide.editor.insight.inspection.flow;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.data.DataFlow;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
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

import java.util.Map;
import java.util.Objects;

public class ConstantValueInspection extends LocalInspectionTool {

    private static @Nullable Expression getExpression(@NotNull RapidExpression expression, @NotNull BasicBlock basicBlock) {
        ExpressionControlFlowVisitor visitor = new ExpressionControlFlowVisitor(expression);
        for (LinearInstruction instruction : basicBlock.getInstructions()) {
            Expression output = instruction.accept(visitor);
            if(output != null) {
                return output;
            }
        }
        BranchingInstruction terminator = basicBlock.getTerminator();
        return terminator.accept(visitor);
    }

    private static class ExpressionControlFlowVisitor extends ControlFlowVisitor<Expression> {

        private final @NotNull PsiElement element;

        public ExpressionControlFlowVisitor(@NotNull PsiElement element) {
            this.element = element;
        }

        private boolean isEquivalent(@Nullable Expression expression) {
            if(expression == null) {
                return false;
            }
            RapidExpression expressionElement = expression.getElement();
            return expressionElement != null && expressionElement.isEquivalentTo(element);
        }

        private Expression getEquivalent(@Nullable Expression @Nullable... expressions) {
            if(expressions == null) {
                return null;
            }
            for (Expression expression : expressions) {
                if(isEquivalent(expression)) {
                    return expression;
                }
            }
            return null;
        }

        @Override
        public Expression visitAssignmentInstruction(@NotNull LinearInstruction.AssignmentInstruction instruction) {
            if(isEquivalent(instruction.variable())) {
                return instruction.variable();
            }
            Expression[] expressions = instruction.value().getComponents().toArray(Expression[]::new);
            return getEquivalent(expressions);
        }

        @Override
        public Expression visitConnectInstruction(@NotNull LinearInstruction.ConnectInstruction instruction) {
            return getEquivalent(instruction.variable(), instruction.routine());
        }

        @Override
        public Expression visitConditionalBranchingInstruction(@NotNull BranchingInstruction.ConditionalBranchingInstruction instruction) {
            return getEquivalent(instruction.value());
        }

        @Override
        public Expression visitReturnInstruction(@NotNull BranchingInstruction.ReturnInstruction instruction) {
            return getEquivalent(instruction.value());
        }

        @Override
        public Expression visitThrowInstruction(@NotNull BranchingInstruction.ThrowInstruction instruction) {
            return getEquivalent(instruction.exception());
        }

        @Override
        public Expression visitCallInstruction(@NotNull BranchingInstruction.CallInstruction instruction) {
            Expression equivalent = getEquivalent(instruction.routine(), instruction.returnValue());
            if(equivalent != null) {
                return equivalent;
            }
            Expression[] expressions = instruction.arguments().values().toArray(Expression[]::new);
            return getEquivalent(expressions);
        }
    }

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new RapidElementVisitor() {
            @Override
            public void visitExpression(@NotNull RapidExpression expression) {
                PhysicalRoutine routine = PhysicalRoutine.getRoutine(expression);
                if (routine == null) {
                    return;
                }
                DataFlow dataFlow = ControlFlowService.getInstance().getDataFlow(expression);
                Block.FunctionBlock functionBlock = getBlock(routine, dataFlow.getControlFlow());
                if (functionBlock == null) {
                    return;
                }
                Map.Entry<BasicBlock, Expression> entry = getBasicBlock(functionBlock, expression);
                if (entry == null) {
                    return;
                }
                Expression instruction = entry.getValue();
                DataFlowBlock block = dataFlow.getBlock(entry.getKey());
                if(block == null) {
                    return;
                }
                if (instruction instanceof ReferenceExpression referenceExpression) {
                    if (expression instanceof RapidReferenceExpression object) {
                        PsiElement parent = expression.getParent();
                        if (parent != null) {
                            Optionality optionality = block.getOptionality(referenceExpression, parent);
                            if (optionality == Optionality.MISSING) {
                                holder.registerProblem(object, RapidBundle.message("inspection.message.missing.variable", object.getCanonicalText()));
                            }
                            if (optionality == Optionality.UNKNOWN) {
                                holder.registerProblem(object, RapidBundle.message("inspection.message.unknown.variable", object.getCanonicalText()));
                            }
                        }
                    }
                }
                if (!(instruction.getType().isAssignable(RapidPrimitiveType.BOOLEAN))) {
                    return;
                }
                BooleanValue constraint = block.getConstraint(instruction);
                if (constraint == BooleanValue.ALWAYS_TRUE || constraint == BooleanValue.ALWAYS_FALSE) {
                    String string = constraint == BooleanValue.ALWAYS_TRUE ? "true" : "false";
                    holder.registerProblem(expression, RapidBundle.message("inspection.message.constant.expression", string));
                }
            }
        };
    }

    private @Nullable Map.Entry<BasicBlock, Expression> getBasicBlock(@NotNull Block block, @NotNull RapidExpression expression) {
        for (BasicBlock basicBlock : block.getBasicBlocks()) {
            Expression instruction = getExpression(expression, basicBlock);
            if (instruction != null) {
                return Map.entry(basicBlock, instruction);
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
