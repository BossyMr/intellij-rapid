package com.bossymr.rapid.language.flow.debug;

import com.bossymr.rapid.language.builder.ArgumentDescriptor;
import com.bossymr.rapid.language.builder.Label;
import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.data.PathCounter;
import com.bossymr.rapid.language.flow.data.snapshots.ArraySnapshot;
import com.bossymr.rapid.language.flow.data.snapshots.RecordSnapshot;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
import com.bossymr.rapid.language.flow.instruction.*;
import com.bossymr.rapid.language.flow.value.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class ControlFlowFormatVisitor extends ControlFlowVisitor<String> {

    public static @NotNull String format(@NotNull ControlFlow controlFlow) {
        return controlFlow.accept(new ControlFlowFormatVisitor());
    }

    @Override
    public @NotNull String visitControlFlow(@NotNull ControlFlow controlFlow) {
        StringBuilder stringBuilder = new StringBuilder();
        List<Block> blocks = new ArrayList<>(controlFlow.getBlocks());
        blocks.sort(Comparator.comparing(block -> block.getModuleName() + ":" + block.getName()));
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
        formatBlock(functionBlock, stringBuilder);
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
        formatBlock(fieldBlock, stringBuilder);
        return stringBuilder.toString();
    }

    private void formatBlock(@NotNull Block block, @NotNull StringBuilder stringBuilder) {
        stringBuilder.append(" {");
        stringBuilder.append("\n");
        List<Variable> variables = block.getVariables();
        for (Variable variable : variables) {
            stringBuilder.append("\t");
            stringBuilder.append(variable.accept(this));
            stringBuilder.append("\n");
        }
        if (!(variables.isEmpty())) {
            stringBuilder.append("\n");
        }
        List<Instruction> instructions = block.getInstructions();
        for (int i = 0; i < instructions.size(); i++) {
            if (i > 0) {
                stringBuilder.append("\n");
            }
            Instruction instruction = instructions.get(i);
            stringBuilder.append("\t");

            Optional<EntryInstruction> optional = instruction.getBlock().getEntryInstructions().stream()
                    .filter(entry -> entry.getInstruction().equals(instruction))
                    .findFirst();
            if (optional.isPresent()) {
                EntryInstruction entryInstruction = optional.orElseThrow();
                stringBuilder.append(entryInstruction.getEntryType());
                stringBuilder.append(":");
                stringBuilder.append("\n");
                stringBuilder.append("\t");
            }
            stringBuilder.append(instruction.getIndex());
            stringBuilder.append(": ");
            stringBuilder.append(instruction.accept(this));
            if (instruction.getSuccessors().size() != 1 || instruction.getSuccessors().get(0).getIndex() != instruction.getIndex() + 1) {
                if (!(instruction.getSuccessors().isEmpty()) && !(instruction instanceof ConditionalBranchingInstruction)) {
                    stringBuilder.append("\n\t");
                    stringBuilder.append(" ".repeat(String.valueOf(instruction.getIndex()).length() + 2));
                    stringBuilder.append("goto -> ");
                    stringBuilder.append(instruction.getSuccessors().stream()
                            .map(successor -> String.valueOf(successor.getIndex()))
                            .collect(Collectors.joining(", ", "[", "]")));
                    stringBuilder.append(";");
                }
                stringBuilder.append("\n");
            }
        }
        stringBuilder.append("}");
        stringBuilder.append("\n");
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
        String parameterType = argument.getParameterType().getText().toLowerCase();
        String type = argument.getType().getPresentableText();
        return parameterType + " " + type + " " + "_" + argument.getIndex() + " [" + argument.getName() + "]";
    }

    @Override
    public @NotNull String visitVariable(@NotNull Variable variable) {
        StringBuilder stringBuilder = new StringBuilder();
        if (variable.getFieldType() != null) {
            stringBuilder.append(variable.getFieldType().getText().toLowerCase());
            stringBuilder.append(" ");
        }
        stringBuilder.append(variable.getType().getPresentableText()).append(" ");
        stringBuilder.append("_").append(variable.getIndex());
        if (variable.getName() != null) {
            stringBuilder.append(" [").append(variable.getName()).append("]");
        }
        stringBuilder.append(";");
        return stringBuilder.toString();
    }

    @Override
    public @NotNull String visitAssignmentInstruction(@NotNull AssignmentInstruction instruction) {
        return instruction.getVariable().accept(this) + " := " + instruction.getExpression().accept(this) + ";";
    }

    @Override
    public @NotNull String visitConnectInstruction(@NotNull ConnectInstruction instruction) {
        return "connect " + instruction.getVariable().accept(this) + " with " + instruction.getExpression().accept(this) + ";";
    }

    @Override
    public @NotNull String visitConditionalBranchingInstruction(@NotNull ConditionalBranchingInstruction instruction) {
        return "if(" + instruction.getCondition().accept(this) + ") -> [true: " + instruction.getTrue().getIndex() + ", false: " + instruction.getFalse().getIndex() + "]";
    }

    @Override
    public @NotNull String visitRetryInstruction(@NotNull RetryInstruction instruction) {
        return "retry;";
    }

    @Override
    public @NotNull String visitTryNextInstruction(@NotNull TryNextInstruction instruction) {
        return "trynext;";
    }

    @Override
    public @NotNull String visitReturnInstruction(@NotNull ReturnInstruction instruction) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("return");
        if (instruction.getReturnValue() != null) {
            stringBuilder.append(" ").append(instruction.getReturnValue().accept(this));
        }
        stringBuilder.append(";");
        return stringBuilder.toString();
    }

    @Override
    public @NotNull String visitExitInstruction(@NotNull ExitInstruction instruction) {
        return "exit;";
    }

    @Override
    public @NotNull String visitThrowInstruction(@NotNull ThrowInstruction instruction) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("throw");
        if (instruction.getExceptionValue() != null) {
            stringBuilder.append(" ").append(instruction.getExceptionValue().accept(this));
        }
        stringBuilder.append(";");
        return stringBuilder.toString();
    }

    @Override
    public @NotNull String visitErrorInstruction(@NotNull ErrorInstruction instruction) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("error");
        if (instruction.getSuccessor() != null) {
            stringBuilder.append(" ").append(instruction.getSuccessor().getIndex());
        }
        stringBuilder.append(";");
        return stringBuilder.toString();
    }

    @Override
    public @NotNull String visitCallInstruction(@NotNull CallInstruction instruction) {
        StringBuilder stringBuilder = new StringBuilder();
        if (instruction.getReturnValue() != null) {
            stringBuilder.append(instruction.getReturnValue().accept(this));
            stringBuilder.append(" := ");
        }
        Expression routine = instruction.getRoutineName();
        if (routine instanceof ConstantExpression constant && constant.getValue() instanceof String) {
            stringBuilder.append(constant.getValue());
        } else {
            stringBuilder.append(routine.accept(this));
        }
        stringBuilder.append("(");
        List<Map.Entry<ArgumentDescriptor, ReferenceExpression>> arguments = new ArrayList<>(instruction.getArguments().entrySet());
        arguments.sort(Comparator.comparing(entry -> getDescriptorName(entry.getKey())));
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) {
                stringBuilder.append(", ");
            }
            Map.Entry<ArgumentDescriptor, ReferenceExpression> entry = arguments.get(i);
            String key = getDescriptorName(entry.getKey());
            stringBuilder.append("_").append(key);
            if (entry.getValue() != null) {
                stringBuilder.append(" := ");
                stringBuilder.append(entry.getValue().accept(this));
            }
        }
        stringBuilder.append(");");
        return stringBuilder.toString();
    }

    private @NotNull String getDescriptorName(@NotNull ArgumentDescriptor descriptor) {
        String key;
        if (descriptor instanceof ArgumentDescriptor.Optional optional) {
            key = optional.name();
        } else if (descriptor instanceof ArgumentDescriptor.Required required) {
            key = String.valueOf(required.index());
        } else {
            throw new IllegalStateException();
        }
        return key;
    }

    @Override
    public String visitArraySnapshotExpression(@NotNull ArraySnapshot snapshot) {
        return super.visitArraySnapshotExpression(snapshot);
    }

    @Override
    public String visitRecordSnapshotExpression(@NotNull RecordSnapshot snapshot) {
        return super.visitRecordSnapshotExpression(snapshot);
    }

    @Override
    public String visitVariableSnapshotExpression(VariableSnapshot snapshot) {
        return super.visitVariableSnapshotExpression(snapshot);
    }

    @Override
    public String visitPathCounterExpression(@NotNull PathCounter pathCounter) {
        return super.visitPathCounterExpression(pathCounter);
    }

    @Override
    public String visitConstantExpression(@NotNull ConstantExpression expression) {
        if (expression.getValue() instanceof String) {
            return "\"" + expression.getValue() + "\"";
        } else {
            return expression.getValue().toString();
        }
    }

    @Override
    public String visitIndexExpression(@NotNull IndexExpression expression) {
        String variableText = expression.getVariable().accept(this);
        String indexText = expression.getIndex().accept(this);
        return variableText + "[" + indexText + "]";
    }

    @Override
    public String visitComponentExpression(@NotNull ComponentExpression expression) {
        String variableText = expression.getVariable().accept(this);
        return variableText + "." + expression.getComponent();
    }

    @Override
    public String visitVariableExpression(@NotNull VariableExpression expression) {
        return "_" + expression.getField().getIndex();
    }

    @Override
    public String visitFieldExpression(@NotNull FieldExpression expression) {
        return expression.getModuleName() + ":" + expression.getName();
    }

    @Override
    public @NotNull String visitSnapshotExpression(@NotNull SnapshotExpression snapshot) {
        return "~" + snapshot.hashCode();
    }

    @Override
    public String visitLabel(@NotNull Label label) {
        return label.getName() + ":";
    }

    @Override
    public @NotNull String visitAggregateExpression(@NotNull AggregateExpression expression) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (int i = 0; i < expression.getExpressions().size(); i++) {
            if (i > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(expression.getExpressions().get(i).accept(this));
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public @NotNull String visitBinaryExpression(@NotNull BinaryExpression expression) {
        String leftValue = expression.getLeft().accept(this);
        String rightValue = expression.getRight().accept(this);
        return leftValue + " " + switch (expression.getOperator()) {
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
        } + " " + rightValue;
    }

    @Override
    public @NotNull String visitUnaryExpression(@NotNull UnaryExpression expression) {
        return switch (expression.getOperator()) {
            case NOT -> "NOT ";
            case NEGATE -> "-";
        } + expression.getExpression().accept(this);
    }
}
