package com.bossymr.rapid.language.flow.debug;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.psi.StatementListType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ControlFlowFormatVisitor extends ControlFlowVisitor<String> {

    public static @NotNull String format(@NotNull ControlFlow controlFlow) {
        return controlFlow.accept(new ControlFlowFormatVisitor());
    }

    @Override
    public @NotNull String visitControlFlow(@NotNull ControlFlow controlFlow) {
        List<Block> blocks = new ArrayList<>(controlFlow.getBlocks());
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < blocks.size(); i++) {
            if (i > 0) {
                stringBuilder.append("\n");
            }
            Block block = blocks.get(i);
            stringBuilder.append(block.accept(this));
        }
        return stringBuilder.toString();
    }

    @Override
    public @NotNull String visitFunctionBlock(@NotNull Block.FunctionBlock functionBlock) {
        StringBuilder stringBuilder = new StringBuilder();
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
        stringBuilder.append("(");
        for (int i = 0; i < argumentGroups.size(); i++) {
            if (i > 0) {
                stringBuilder.append(", ");
            }
            ArgumentGroup argumentGroup = argumentGroups.get(i);
            stringBuilder.append(argumentGroup.accept(this));
        }
        stringBuilder.append(")");
        formatBlock(stringBuilder, functionBlock);
        return stringBuilder.toString();
    }

    @Override
    public @NotNull String visitFieldBlock(@NotNull Block.FieldBlock fieldBlock) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(fieldBlock.getFieldType().getText().toLowerCase());
        stringBuilder.append(" ");
        fieldBlock.getReturnType();
        stringBuilder.append(fieldBlock.getReturnType().getPresentableText());
        stringBuilder.append(" ");
        stringBuilder.append(fieldBlock.getModuleName());
        stringBuilder.append(":");
        stringBuilder.append(fieldBlock.getName());
        formatBlock(stringBuilder, fieldBlock);
        return stringBuilder.toString();
    }

    private void formatBlock(@NotNull StringBuilder stringBuilder, @NotNull Block block) {
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
    public @NotNull String visitBasicBlock(@NotNull BasicBlock basicBlock) {
        StringBuilder stringBuilder = new StringBuilder();
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
            stringBuilder.append(instruction.accept(this));
            stringBuilder.append("\n");
        }
        stringBuilder.append("\t\t");
        basicBlock.getTerminator().accept(this);
        stringBuilder.append("\n");
        stringBuilder.append("\t}");
        return stringBuilder.toString();
    }

    @Override
    public @NotNull String visitArgumentGroup(@NotNull ArgumentGroup argumentGroup) {
        StringBuilder stringBuilder = new StringBuilder();
        if (argumentGroup.isOptional()) {
            stringBuilder.append("\\");
        }
        List<Argument> arguments = argumentGroup.arguments();
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) {
                stringBuilder.append(" | ");
            }
            Argument argument = arguments.get(i);
            stringBuilder.append(argument.accept(this));
        }
        return stringBuilder.toString();
    }

    @Override
    public @NotNull String visitArgument(@NotNull Argument argument) {
        return argument.parameterType().getText().toLowerCase() + " " + argument.type().getPresentableText() + " " + "_" + argument.index() + " [" + argument.name() + "]";
    }

    @Override
    public @NotNull String visitVariable(@NotNull Variable variable) {
        StringBuilder stringBuilder = new StringBuilder();
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
        return stringBuilder.toString();
    }

    @Override
    public @NotNull String visitAssignmentInstruction(@NotNull LinearInstruction.AssignmentInstruction instruction) {
        return instruction.variable().accept(this) + " := " + instruction.value().accept(this) + ";";
    }

    @Override
    public @NotNull String visitConnectInstruction(@NotNull LinearInstruction.ConnectInstruction instruction) {
        return "connect " + instruction.variable().accept(this) + " with " + instruction.routine().accept(this) + ";";
    }

    @Override
    public @NotNull String visitConditionalBranchingInstruction(@NotNull BranchingInstruction.ConditionalBranchingInstruction instruction) {
        return "if(" + instruction.value().accept(this) + ") -> " +
                "[true: " + instruction.onSuccess().getIndex() +
                ", false: " + instruction.onFailure().getIndex() + "]";
    }

    @Override
    public @NotNull String visitUnconditionalBranchingInstruction(@NotNull BranchingInstruction.UnconditionalBranchingInstruction instruction) {
        return "goto -> " + instruction.next().getIndex() + ";";
    }

    @Override
    public @NotNull String visitRetryInstruction(@NotNull BranchingInstruction.RetryInstruction instruction) {
        return "retry;";
    }

    @Override
    public @NotNull String visitTryNextInstruction(@NotNull BranchingInstruction.TryNextInstruction instruction) {
        return "trynext;";
    }

    @Override
    public @NotNull String visitReturnInstruction(@NotNull BranchingInstruction.ReturnInstruction instruction) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("return");
        if (instruction.value() != null) {
            stringBuilder.append(" ");
            stringBuilder.append(instruction.value().accept(this));
        }
        stringBuilder.append(";");
        return stringBuilder.toString();
    }

    @Override
    public @NotNull String visitExitInstruction(@NotNull BranchingInstruction.ExitInstruction instruction) {
        return "exit;";
    }

    @Override
    public @NotNull String visitThrowInstruction(@NotNull BranchingInstruction.ThrowInstruction instruction) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("throw");
        if (instruction.exception() != null) {
            stringBuilder.append(" ");
            stringBuilder.append(instruction.exception().accept(this));
        }
        stringBuilder.append(";");
        return stringBuilder.toString();
    }

    @Override
    public @NotNull String visitErrorInstruction(@NotNull BranchingInstruction.ErrorInstruction instruction) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("error");
        if (instruction.next() != null) {
            stringBuilder.append(" -> ");
            stringBuilder.append(instruction.next().getIndex());
        }
        stringBuilder.append(";");
        return stringBuilder.toString();
    }

    @Override
    public @NotNull String visitCallInstruction(@NotNull BranchingInstruction.CallInstruction instruction) {
        StringBuilder stringBuilder = new StringBuilder();
        if (instruction.returnValue() != null) {
            stringBuilder.append(instruction.returnValue().accept(this));
            stringBuilder.append(" := ");
        }
        Value routine = instruction.routine();
        if (routine instanceof ConstantValue constant && constant.value() instanceof String) {
            stringBuilder.append(constant.value());
        } else {
            stringBuilder.append(routine.accept(this));
        }
        stringBuilder.append("(");
        List<Map.Entry<ArgumentDescriptor, Value>> arguments = new ArrayList<>(instruction.arguments().entrySet());
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) {
                stringBuilder.append(", ");
            }
            Map.Entry<ArgumentDescriptor, Value> entry = arguments.get(i);
            stringBuilder.append("_").append(entry.getKey());
            if (entry.getValue() != null) {
                stringBuilder.append(" := ");
                stringBuilder.append(entry.getValue().accept(this));
            }
        }
        stringBuilder.append(")");
        stringBuilder.append(" -> ").append(instruction.nextBasicBlock().getIndex()).append(";");
        return stringBuilder.toString();
    }

    @Override
    public @NotNull String visitLocalVariableValue(@NotNull VariableReference value) {
        return "_" + value.field().index();
    }

    @Override
    public @NotNull String visitFieldVariableValue(@NotNull FieldReference value) {
        StringBuilder stringBuilder = new StringBuilder();
        if (value.moduleName() != null) {
            stringBuilder.append(value.moduleName()).append(":");
        }
        stringBuilder.append(value.name());
        return stringBuilder.toString();
    }

    @Override
    public @NotNull String visitIndexVariableValue(@NotNull IndexReference value) {
        return value.variable().accept(this) + "[" + value.index().accept(this) + "]";
    }

    @Override
    public @NotNull String visitComponentVariableValue(@NotNull ComponentReference value) {
        return value.variable().accept(this) + "." + value.component();
    }

    @Override
    public @NotNull String visitConstantValue(@NotNull ConstantValue value) {
        StringBuilder stringBuilder = new StringBuilder();
        if (value.value() instanceof String) {
            stringBuilder.append("\"").append(value.value()).append("\"");
        } else {
            stringBuilder.append(value.value());
        }
        return stringBuilder.toString();
    }

    @Override
    public @NotNull String visitErrorValue(@NotNull ErrorValue value) {
        return "error";
    }

    @Override
    public @NotNull String visitVariableExpression(@NotNull VariableExpression expression) {
        return expression.value().accept(this);
    }

    @Override
    public @NotNull String visitAggregateExpression(@NotNull AggregateExpression expression) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (int i = 0; i < expression.values().size(); i++) {
            if (i > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(expression.values().get(i).accept(this));
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public @NotNull String visitBinaryExpression(@NotNull BinaryExpression expression) {
        return expression.left().accept(this) + " " +
                switch (expression.operator()) {
                    case ADD -> "+";
                    case SUBTRACT -> "-";
                    case MULTIPLY -> "*";
                    case DIVIDE -> "/";
                    case INTEGER_DIVIDE -> "DIV";
                    case MODULO -> "%";
                    case LESS_THAN -> "<";
                    case LESS_THAN_OR_EQUAL -> "<=";
                    case EQUAL_TO -> "=";
                    case NOT_EQUAL_TO -> "!=";
                    case GREATER_THAN -> ">";
                    case GREATER_THAN_OR_EQUAL -> ">=";
                    case AND -> "AND";
                    case XOR -> "XOR";
                    case OR -> "OR";
                } + " " + expression.right().accept(this);
    }

    @Override
    public @NotNull String visitUnaryExpression(@NotNull UnaryExpression expression) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(switch (expression.operator()) {
            case NOT -> "NOT";
            case NEGATE -> "-";
        });
        if (expression.operator() != UnaryOperator.NEGATE) {
            stringBuilder.append(" ");
        }
        stringBuilder.append(expression.value().accept(this));
        return stringBuilder.toString();
    }
}
