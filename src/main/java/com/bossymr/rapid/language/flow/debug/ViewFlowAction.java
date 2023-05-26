package com.bossymr.rapid.language.flow.debug;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.conditon.Expression;
import com.bossymr.rapid.language.flow.conditon.Operator;
import com.bossymr.rapid.language.flow.conditon.Value;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ViewFlowAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        PsiElement element = e.getRequiredData(CommonDataKeys.PSI_ELEMENT);
        PsiFile containingFile = element.getContainingFile();
        Module module = ModuleUtil.findModuleForFile(containingFile);
        Objects.requireNonNull(module);
        ControlFlow controlFlow = ControlFlowVisitor.createControlFlow(module);
        StringBuilder stringBuilder = new StringBuilder();
        for (Block block : controlFlow.getBlocks()) {
            if (block instanceof Block.FunctionBlock functionBlock) {
                stringBuilder.append(functionBlock.routineType()).append(" ");
            } else if (block instanceof Block.FieldBlock fieldBlock) {
                stringBuilder.append(fieldBlock.fieldType()).append(" ");
            }
            stringBuilder.append(block.moduleName()).append("::").append(block.name());
            if (block instanceof Block.FunctionBlock functionBlock) {
                List<ArgumentGroup> arguments = functionBlock.arguments();
                if (arguments != null) {
                    stringBuilder.append("(");
                    for (ArgumentGroup argumentGroup : arguments) {
                        if (argumentGroup.isOptional()) {
                            stringBuilder.append("\\");
                        }
                        stringBuilder.append(argumentGroup.arguments().stream()
                                .map(argument -> argument.type().getPresentableText() + " " + argument.name() + " (" + argument.index() + ")")
                                .collect(Collectors.joining(", ")));
                    }
                    stringBuilder.append(")").append(" ");
                }
            }
            stringBuilder.append("{").append("\n");
            for (Variable variable : block.variables().values()) {
                stringBuilder.append("\t");
                if (variable.fieldType() != null) {
                    stringBuilder.append(variable.fieldType().getText()).append(" ");
                }
                stringBuilder.append(variable.type().getPresentableText()).append(" ").append(variable.name()).append(" (").append(variable.index()).append(")");
                if (variable.value() != null) {
                    stringBuilder.append(" := ").append(variable.value());
                }
                stringBuilder.append(";").append("\n");
            }
            if (block.variables().values().size() > 0) {
                stringBuilder.append(" ");
            }
            for (Scope scope : block.scopes()) {
                stringBuilder.append("\t");
                stringBuilder.append("scope").append(" ").append(scope.index());
                ScopeType scopeType = scope.scopeType();
                if (scopeType != null) {
                    stringBuilder.append("(").append(scopeType.name().toLowerCase()).append(")");
                }
                stringBuilder.append(" ");
                stringBuilder.append("{");
                stringBuilder.append("\n");
                for (Instruction instruction : scope.instructions()) {
                    stringBuilder.append("\t\t");
                    if (instruction instanceof LinearInstruction.AssignmentInstruction assignmentInstruction) {
                        writeValue(stringBuilder, assignmentInstruction.variable());
                        stringBuilder.append(" := ");
                        writeExpression(stringBuilder, assignmentInstruction.value());
                        stringBuilder.append(";");
                    } else if (instruction instanceof LinearInstruction.ConnectInstruction connectInstruction) {
                        stringBuilder.append("connect ");
                        writeValue(stringBuilder, connectInstruction.variable());
                        stringBuilder.append(" with ");
                        writeValue(stringBuilder, connectInstruction.routine());
                        stringBuilder.append(";");
                    } else if (instruction instanceof BranchingInstruction.ConditionalBranchingInstruction conditionalBranchingInstruction) {
                        stringBuilder.append("if(");
                        writeValue(stringBuilder, conditionalBranchingInstruction.value());
                        stringBuilder.append(") -> ");
                        stringBuilder.append("[");
                        stringBuilder.append("true:").append(conditionalBranchingInstruction.onSuccess().index()).append(", ");
                        stringBuilder.append("false:").append(conditionalBranchingInstruction.onFailure().index());
                        stringBuilder.append("]");
                    } else if (instruction instanceof BranchingInstruction.UnconditionalBranchingInstruction unconditionalBranchingInstruction) {
                        stringBuilder.append("goto ->");
                        stringBuilder.append(unconditionalBranchingInstruction.next().index());
                        stringBuilder.append(";");
                    } else if (instruction instanceof BranchingInstruction.RetryInstruction) {
                        stringBuilder.append("retry;");
                    } else if (instruction instanceof BranchingInstruction.TryNextInstruction) {
                        stringBuilder.append("trynext;");
                    } else if (instruction instanceof BranchingInstruction.ExitInstruction) {
                        stringBuilder.append("exit;");
                    } else if (instruction instanceof BranchingInstruction.ReturnInstruction returnInstruction) {
                        stringBuilder.append("return");
                        if (returnInstruction.value() != null) {
                            stringBuilder.append(" ");
                            writeValue(stringBuilder, returnInstruction.value());
                        }
                        stringBuilder.append(";");
                    } else if (instruction instanceof BranchingInstruction.ThrowInstruction throwInstruction) {
                        stringBuilder.append("throw");
                        if (throwInstruction.exception() != null) {
                            stringBuilder.append(" ");
                            writeValue(stringBuilder, throwInstruction.exception());
                        }
                        stringBuilder.append(";");
                    } else if (instruction instanceof BranchingInstruction.CallInstruction callInstruction) {
                        if (callInstruction.returnValue() != null) {
                            writeValue(stringBuilder, callInstruction.routine());
                            stringBuilder.append(" := ");
                        }
                        writeValue(stringBuilder, callInstruction.routine());
                        stringBuilder.append("(");
                        List<Value> values = new ArrayList<>(callInstruction.arguments().values());
                        for (int i = 0; i < values.size(); i++) {
                            if (i > 0) stringBuilder.append(", ");
                            Value value = values.get(i);
                            writeValue(stringBuilder, value);
                        }
                        stringBuilder.append(")");
                        stringBuilder.append(" -> ");
                        stringBuilder.append(scope.index());
                        stringBuilder.append(";");
                    }
                    stringBuilder.append("\n");
                }
                stringBuilder.append("}").append("\n").append("\n");
            }
            stringBuilder.append("}").append("\n").append("\n");
        }
        LightVirtualFile virtualFile = new LightVirtualFile("ControlFlow.txt", stringBuilder);
        FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, virtualFile), true);
    }

    private void writeExpression(@NotNull StringBuilder stringBuilder, @NotNull Expression expression) {
        if (expression instanceof Expression.Variable variable) {
            writeValue(stringBuilder, variable.value());
        } else if (expression instanceof Expression.Index index) {
            writeValue(stringBuilder, index.variable());
            stringBuilder.append("[");
            writeValue(stringBuilder, index.index());
            stringBuilder.append("]");
        } else if (expression instanceof Expression.Aggregate aggregate) {
            stringBuilder.append("[");
            for (int i = 0; i < aggregate.values().size(); i++) {
                if (i > 0) stringBuilder.append(", ");
                Value value = aggregate.values().get(i);
                writeValue(stringBuilder, value);
            }
            stringBuilder.append("]");
        } else if (expression instanceof Expression.Binary binary) {
            writeValue(stringBuilder, binary.left());
            stringBuilder.append(" ");
            stringBuilder.append(switch (binary.operator()) {
                case ADD -> "+";
                case SUBTRACT -> "-";
                case MULTIPLY -> "*";
                case DIVIDE -> "/";
                case MODULO -> "%";
                case LESS_THAN -> "<";
                case LESS_THAN_OR_EQUAL_TO -> "<=";
                case EQUAL_TO -> "=";
                case GREATER_THAN_OR_EQUAL_TO -> ">=";
                case GREATER_THAN -> ">";
                case NOT_EQUAL_TO -> "!=";
                case AND -> "AND";
                case EXLUSIVE_OR -> "XOR";
                case OR -> "OR";
                case NOT -> "NOT";
            });
            stringBuilder.append(" ");
            writeValue(stringBuilder, binary.right());
        } else if (expression instanceof Expression.Unary unary) {
            stringBuilder.append(switch (unary.operator()) {
                case NOT -> "NOT";
                case NEGATE -> "-";
            });
            if (unary.operator() != Operator.UnaryOperator.NEGATE) {
                stringBuilder.append(" ");
            }
            writeValue(stringBuilder, unary.value());
        }
        throw new IllegalStateException();
    }

    private void writeValue(@NotNull StringBuilder stringBuilder, @NotNull Value value) {
        if (value instanceof Value.Constant constant) {
            stringBuilder.append(constant.value());
        } else if (value instanceof Value.Variable.Local local) {
            stringBuilder.append("_").append(local.index());
        } else if (value instanceof Value.Variable.Field field) {
            stringBuilder.append(field.moduleName()).append("::").append(field.name());
        } else if (value instanceof Value.Variable.Index index) {
            writeValue(stringBuilder, index.variable());
            stringBuilder.append("[");
            writeValue(stringBuilder, index.index());
            stringBuilder.append("]");
        }
        throw new IllegalStateException();
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
        if (element == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        PsiFile containingFile = element.getContainingFile();
        Module module = ModuleUtil.findModuleForFile(containingFile);
        e.getPresentation().setEnabled(module != null);
    }
}
