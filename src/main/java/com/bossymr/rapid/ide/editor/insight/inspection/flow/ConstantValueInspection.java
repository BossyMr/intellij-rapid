package com.bossymr.rapid.ide.editor.insight.inspection.flow;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.flow.BasicBlock;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.ControlFlow;
import com.bossymr.rapid.language.flow.ControlFlowService;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.constraint.Optionality;
import com.bossymr.rapid.language.flow.data.DataFlow;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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
                    ReferenceValue variable = assignmentInstruction.variable();
                    DataFlowBlock block = dataFlow.getBlock(basicBlock);
                    Constraint constraint = block.getConstraint(variable);
                    Expression operation = assignmentInstruction.value();
                    Set<VariableSymbol> values = new HashSet<>(2, 1);
                    if (expression instanceof RapidUnaryExpression element && operation instanceof UnaryExpression unaryExpression) {
                        VariableSymbol.add(values, element.getExpression(), unaryExpression.value());
                    } else if (expression instanceof RapidBinaryExpression element && operation instanceof BinaryExpression binaryExpression) {
                        VariableSymbol.add(values, element.getLeft(), binaryExpression.left());
                        VariableSymbol.add(values, element.getRight(), binaryExpression.right());
                    }
                    for (VariableSymbol value : values) {
                        Constraint variableConstraint = block.getHistoricConstraint(value.referenceValue(), assignmentInstruction);
                        String name = value.name();
                        if (variableConstraint.getOptionality() == Optionality.MISSING) {
                            holder.registerProblem(value.element(), RapidBundle.message("inspection.message.missing.variable", name));
                        }
                        if (variableConstraint.getOptionality() == Optionality.UNKNOWN) {
                            holder.registerProblem(value.element(), RapidBundle.message("inspection.message.unknown.variable", name));
                        }
                    }
                    Optional<?> value = constraint.getValue();
                    if (value.isPresent()) {
                        Object object = value.orElseThrow();
                        String string = object instanceof Double ? BigDecimal.valueOf((Double) object).stripTrailingZeros().toPlainString() : String.valueOf(object);
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

    private record VariableSymbol(@NotNull PsiElement element, @NotNull String name, @NotNull ReferenceValue referenceValue) {

        public static void add(@NotNull Set<VariableSymbol> symbols, @Nullable RapidExpression expression, @NotNull Value value) {
            if (!(expression instanceof RapidReferenceExpression referenceExpression)) {
                return;
            }
            if (!(value instanceof ReferenceValue referenceValue)) {
                return;
            }
            RapidSymbol symbol = referenceExpression.getSymbol();
            if (symbol == null) {
                return;
            }
            String name = symbol.getName();
            if (name == null) {
                return;
            }
            symbols.add(new VariableSymbol(expression, name, referenceValue));
        }

    }
}
