package com.bossymr.rapid.language.flow.debug;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.conditon.Expression;
import com.bossymr.rapid.language.flow.conditon.Operator;
import com.bossymr.rapid.language.flow.conditon.Value;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.psi.StatementListType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ControlFlowFormatVisitor extends ControlFlowVisitor {

    private final @NotNull StringBuilder stringBuilder;

    public ControlFlowFormatVisitor(@NotNull StringBuilder stringBuilder) {
        this.stringBuilder = stringBuilder;
    }

    public static @NotNull String format(@NotNull ControlFlow controlFlow) {
        StringBuilder stringBuilder = new StringBuilder();
        controlFlow.accept(new ControlFlowFormatVisitor(stringBuilder));
        return stringBuilder.toString();
    }

    @Override
    public void visitControlFlow(@NotNull ControlFlow controlFlow) {
        List<Block> blocks = new ArrayList<>(controlFlow.getBlocks());
        for (int i = 0; i < blocks.size(); i++) {
            if (i > 0) {
                stringBuilder.append("\n");
            }
            Block block = blocks.get(i);
            block.accept(this);
        }
    }

    @Override
    public void visitFunctionBlock(@NotNull Block.FunctionBlock functionBlock) {
        stringBuilder.append(functionBlock.getRoutineType().getText().toLowerCase());
        stringBuilder.append(" ");
        if (functionBlock.getReturnType() != null) {
            stringBuilder.append(functionBlock.getReturnType().getPresentableText());
            stringBuilder.append(" ");
        }
        stringBuilder.append(functionBlock.getModuleName());
        stringBuilder.append(":");
        stringBuilder.append(functionBlock.getName());
        List<ArgumentGroup> argumentGroups = functionBlock.getArgumentGroups();
        if (argumentGroups != null) {
            stringBuilder.append("(");
            for (int i = 0; i < argumentGroups.size(); i++) {
                if (i > 0) {
                    stringBuilder.append(", ");
                }
                ArgumentGroup argumentGroup = argumentGroups.get(i);
                argumentGroup.accept(this);
            }
            stringBuilder.append(")");
        }
        formatBlock(functionBlock);
    }

    @Override
    public void visitFieldBlock(@NotNull Block.FieldBlock fieldBlock) {
        stringBuilder.append(fieldBlock.getFieldType().getText().toLowerCase());
        stringBuilder.append(" ");
        fieldBlock.getReturnType();
        stringBuilder.append(fieldBlock.getReturnType().getPresentableText());
        stringBuilder.append(" ");
        stringBuilder.append(fieldBlock.getModuleName());
        stringBuilder.append(":");
        stringBuilder.append(fieldBlock.getName());
        formatBlock(fieldBlock);
    }

    private void formatBlock(@NotNull Block block) {
        stringBuilder.append(" {");
        stringBuilder.append("\n");
        List<Variable> variables = block.getVariables();
        for (Variable variable : variables) {
            stringBuilder.append("\t");
            variable.accept(this);
            stringBuilder.append("\n");
        }
        if (variables.size() > 0) {
            stringBuilder.append("\n");
        }
        List<BasicBlock> basicBlocks = block.getBasicBlocks();
        for (int i = 0; i < basicBlocks.size(); i++) {
            if (i > 0) {
                stringBuilder.append("\n");
            }
            BasicBlock basicBlock = basicBlocks.get(i);
            stringBuilder.append("\t");
            basicBlock.accept(this);
            stringBuilder.append("\n");
        }
        stringBuilder.append("}");
        stringBuilder.append("\n");
    }

    @Override
    public void visitBasicBlock(@NotNull BasicBlock basicBlock) {
        StatementListType scopeType = basicBlock.getScopeType();
        if (scopeType != null) {
            stringBuilder.append(switch (scopeType) {
                case STATEMENT_LIST -> "entry";
                case ERROR_CLAUSE -> "error";
                case UNDO_CLAUSE -> "undo";
                case BACKWARD_CLAUSE -> "backward";
            });
            stringBuilder.append(" ");
        } else {
            stringBuilder.append("block").append(" ");
        }
        stringBuilder.append(basicBlock.getIndex());
        stringBuilder.append(" {");
        stringBuilder.append("\n");
        for (LinearInstruction instruction : basicBlock.getInstructions()) {
            stringBuilder.append("\t\t");
            instruction.accept(this);
            stringBuilder.append("\n");
        }
        stringBuilder.append("\t\t");
        basicBlock.getTerminator().accept(this);
        stringBuilder.append("\n");
        stringBuilder.append("\t}");
    }

    @Override
    public void visitArgumentGroup(@NotNull ArgumentGroup argumentGroup) {
        if (argumentGroup.isOptional()) {
            stringBuilder.append("\\");
        }
        List<Argument> arguments = argumentGroup.arguments();
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) {
                stringBuilder.append(" | ");
            }
            Argument argument = arguments.get(i);
            argument.accept(this);
        }
    }

    @Override
    public void visitArgument(@NotNull Argument argument) {
        stringBuilder.append(argument.parameterType().getText().toLowerCase());
        stringBuilder.append(" ");
        stringBuilder.append(argument.type().getPresentableText());
        stringBuilder.append(" ");
        stringBuilder.append("_").append(argument.index());
        stringBuilder.append(" [").append(argument.name()).append("]");
    }

    @Override
    public void visitVariable(@NotNull Variable variable) {
        if (variable.fieldType() != null) {
            stringBuilder.append(variable.fieldType().getText().toLowerCase());
            stringBuilder.append(" ");
        }
        stringBuilder.append(variable.type().getPresentableText()).append(" ");
        stringBuilder.append("_").append(variable.index());
        if (variable.name() != null) {
            stringBuilder.append(" [").append(variable.name()).append("]");
        }
        if (variable.value() != null) {
            stringBuilder.append(" := ");
            if (variable.value() instanceof String) {
                stringBuilder.append("\"").append(variable.value()).append("\"");
            } else {
                stringBuilder.append(variable.value());
            }
        }
        stringBuilder.append(";");
    }

    @Override
    public void visitAssignmentInstruction(@NotNull LinearInstruction.AssignmentInstruction instruction) {
        instruction.variable().accept(this);
        stringBuilder.append(" := ");
        instruction.value().accept(this);
        stringBuilder.append(";");
    }

    @Override
    public void visitConnectInstruction(@NotNull LinearInstruction.ConnectInstruction instruction) {
        stringBuilder.append("connect ");
        instruction.variable().accept(this);
        stringBuilder.append(" with ");
        instruction.routine().accept(this);
        stringBuilder.append(";");
    }

    @Override
    public void visitConditionalBranchingInstruction(@NotNull BranchingInstruction.ConditionalBranchingInstruction instruction) {
        stringBuilder.append("if(");
        instruction.value().accept(this);
        stringBuilder.append(") -> ");
        stringBuilder.append("[true: ").append(instruction.onSuccess().getIndex());
        stringBuilder.append(", false: ").append(instruction.onFailure().getIndex()).append("]");
    }

    @Override
    public void visitUnconditionalBranchingInstruction(@NotNull BranchingInstruction.UnconditionalBranchingInstruction instruction) {
        stringBuilder.append("goto -> ").append(instruction.next().getIndex()).append(";");
    }

    @Override
    public void visitRetryInstruction(@NotNull BranchingInstruction.RetryInstruction instruction) {
        stringBuilder.append("retry;");
    }

    @Override
    public void visitTryNextInstruction(@NotNull BranchingInstruction.TryNextInstruction instruction) {
        stringBuilder.append("trynext;");
    }

    @Override
    public void visitReturnInstruction(@NotNull BranchingInstruction.ReturnInstruction instruction) {
        stringBuilder.append("return");
        if (instruction.value() != null) {
            stringBuilder.append(" ");
            instruction.value().accept(this);
        }
        stringBuilder.append(";");
    }

    @Override
    public void visitExitInstruction(@NotNull BranchingInstruction.ExitInstruction instruction) {
        stringBuilder.append("exit;");
    }

    @Override
    public void visitThrowInstruction(@NotNull BranchingInstruction.ThrowInstruction instruction) {
        stringBuilder.append("throw");
        if (instruction.exception() != null) {
            stringBuilder.append(" ");
            instruction.exception().accept(this);
        }
        stringBuilder.append(";");
    }

    @Override
    public void visitErrorInstruction(@NotNull BranchingInstruction.ErrorInstruction instruction) {
        stringBuilder.append("error -> ").append(instruction.next().getIndex()).append(";");
    }

    @Override
    public void visitCallInstruction(@NotNull BranchingInstruction.CallInstruction instruction) {
        if (instruction.returnValue() != null) {
            instruction.returnValue().accept(this);
            stringBuilder.append(" := ");
        }
        Value routine = instruction.routine();
        if (routine instanceof Value.Constant constant && constant.value() instanceof String) {
            stringBuilder.append(constant.value());
        } else {
            routine.accept(this);
        }
        stringBuilder.append("(");
        List<Map.Entry<Integer, Value>> arguments = new ArrayList<>(instruction.arguments().entrySet());
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) {
                stringBuilder.append(", ");
            }
            Map.Entry<Integer, Value> entry = arguments.get(i);
            stringBuilder.append("_").append(entry.getKey());
            if (entry.getValue() != null) {
                stringBuilder.append(" := ");
                entry.getValue().accept(this);
            }
        }
        stringBuilder.append(")");
        stringBuilder.append(" -> ").append(instruction.nextBasicBlock().getIndex()).append(";");
    }

    @Override
    public void visitLocalVariableValue(@NotNull Value.Variable.Local value) {
        stringBuilder.append("_").append(value.index());
    }

    @Override
    public void visitFieldVariableValue(@NotNull Value.Variable.Field value) {
        if (value.moduleName() != null) {
            stringBuilder.append(value.moduleName()).append(":");
        }
        stringBuilder.append(value.name());
    }

    @Override
    public void visitIndexVariableValue(@NotNull Value.Variable.Index value) {
        value.variable().accept(this);
        stringBuilder.append("[");
        value.index().accept(this);
        stringBuilder.append("]");
    }

    @Override
    public void visitComponentVariableValue(@NotNull Value.Variable.Component value) {
        value.variable().accept(this);
        stringBuilder.append(".");
        stringBuilder.append(value.component());
    }

    @Override
    public void visitConstantValue(@NotNull Value.Constant value) {
        if (value.value() instanceof String) {
            stringBuilder.append("\"").append(value.value()).append("\"");
        } else {
            stringBuilder.append(value.value());
        }
    }

    @Override
    public void visitErrorValue(@NotNull Value.Error value) {
        stringBuilder.append("error");
    }

    @Override
    public void visitVariableExpression(@NotNull Expression.Variable expression) {
        expression.value().accept(this);
    }

    @Override
    public void visitIndexExpression(@NotNull Expression.Index expression) {
        expression.variable().accept(this);
        stringBuilder.append("[");
        expression.index().accept(this);
        stringBuilder.append("]");
    }

    @Override
    public void visitAggregateExpression(@NotNull Expression.Aggregate expression) {
        stringBuilder.append("[");
        for (int i = 0; i < expression.values().size(); i++) {
            if (i > 0) {
                stringBuilder.append(", ");
            }
            expression.values().get(i).accept(this);
        }
        stringBuilder.append("]");
    }

    @Override
    public void visitBinaryExpression(@NotNull Expression.Binary expression) {
        expression.left().accept(this);
        stringBuilder.append(" ");
        stringBuilder.append(switch (expression.operator()) {
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
        expression.right().accept(this);
    }

    @Override
    public void visitUnaryExpression(@NotNull Expression.Unary expression) {
        stringBuilder.append(switch (expression.operator()) {
            case NOT -> "NOT";
            case NEGATE -> "-";
        });
        if (expression.operator() != Operator.UnaryOperator.NEGATE) {
            stringBuilder.append(" ");
        }
        expression.value().accept(this);
    }
}
