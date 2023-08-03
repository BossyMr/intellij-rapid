package com.bossymr.rapid.language.flow.debug;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.psi.StatementListType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
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
        blocks.sort(Comparator.comparing(block -> block.getModuleName() + ":" + block.getName()));
        for (int i = 0; i < blocks.size(); i++) {
            if (i > 0) {
                stringBuilder.append("\n");
            }
            Block block = blocks.get(i);
            block.accept(this);
        }
        super.visitControlFlow(controlFlow);
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
        stringBuilder.append("(");
        for (int i = 0; i < argumentGroups.size(); i++) {
            if (i > 0) {
                stringBuilder.append(", ");
            }
            ArgumentGroup argumentGroup = argumentGroups.get(i);
            argumentGroup.accept(this);
        }
        stringBuilder.append(")");
        formatBlock(functionBlock);
        super.visitFunctionBlock(functionBlock);
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
        super.visitFieldBlock(fieldBlock);
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
        if (!(variables.isEmpty())) {
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
        super.visitBasicBlock(basicBlock);
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
        super.visitArgumentGroup(argumentGroup);
    }

    @Override
    public void visitArgument(@NotNull Argument argument) {
        stringBuilder.append(argument.parameterType().getText().toLowerCase());
        stringBuilder.append(" ");
        stringBuilder.append(argument.type().getPresentableText());
        stringBuilder.append(" ");
        stringBuilder.append("_").append(argument.index());
        stringBuilder.append(" [").append(argument.name()).append("]");
        super.visitArgument(argument);
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
        stringBuilder.append(";");
        super.visitVariable(variable);
    }

    @Override
    public void visitAssignmentInstruction(@NotNull LinearInstruction.AssignmentInstruction instruction) {
        instruction.variable().accept(this);
        stringBuilder.append(" := ");
        instruction.value().accept(this);
        stringBuilder.append(";");
        super.visitAssignmentInstruction(instruction);
    }

    @Override
    public void visitConnectInstruction(@NotNull LinearInstruction.ConnectInstruction instruction) {
        stringBuilder.append("connect ");
        instruction.variable().accept(this);
        stringBuilder.append(" with ");
        instruction.routine().accept(this);
        stringBuilder.append(";");
        super.visitConnectInstruction(instruction);
    }

    @Override
    public void visitConditionalBranchingInstruction(@NotNull BranchingInstruction.ConditionalBranchingInstruction instruction) {
        stringBuilder.append("if(");
        instruction.value().accept(this);
        stringBuilder.append(") -> ");
        stringBuilder.append("[true: ").append(instruction.onSuccess().getIndex());
        stringBuilder.append(", false: ").append(instruction.onFailure().getIndex()).append("]");
        super.visitConditionalBranchingInstruction(instruction);
    }

    @Override
    public void visitUnconditionalBranchingInstruction(@NotNull BranchingInstruction.UnconditionalBranchingInstruction instruction) {
        stringBuilder.append("goto -> ").append(instruction.next().getIndex()).append(";");
        super.visitUnconditionalBranchingInstruction(instruction);
    }

    @Override
    public void visitRetryInstruction(@NotNull BranchingInstruction.RetryInstruction instruction) {
        stringBuilder.append("retry;");
        super.visitRetryInstruction(instruction);
    }

    @Override
    public void visitTryNextInstruction(@NotNull BranchingInstruction.TryNextInstruction instruction) {
        stringBuilder.append("trynext;");
        super.visitTryNextInstruction(instruction);
    }

    @Override
    public void visitReturnInstruction(@NotNull BranchingInstruction.ReturnInstruction instruction) {
        stringBuilder.append("return");
        if (instruction.value() != null) {
            stringBuilder.append(" ");
            instruction.value().accept(this);
        }
        stringBuilder.append(";");
        super.visitReturnInstruction(instruction);
    }

    @Override
    public void visitExitInstruction(@NotNull BranchingInstruction.ExitInstruction instruction) {
        stringBuilder.append("exit;");
        super.visitExitInstruction(instruction);
    }

    @Override
    public void visitThrowInstruction(@NotNull BranchingInstruction.ThrowInstruction instruction) {
        stringBuilder.append("throw");
        if (instruction.exception() != null) {
            stringBuilder.append(" ");
            instruction.exception().accept(this);
        }
        stringBuilder.append(";");
        super.visitThrowInstruction(instruction);
    }

    @Override
    public void visitErrorInstruction(@NotNull BranchingInstruction.ErrorInstruction instruction) {
        stringBuilder.append("error");
        if (instruction.next() != null) {
            stringBuilder.append(" -> ");
            stringBuilder.append(instruction.next().getIndex());
        }
        stringBuilder.append(";");
        super.visitErrorInstruction(instruction);
    }

    @Override
    public void visitCallInstruction(@NotNull BranchingInstruction.CallInstruction instruction) {
        if (instruction.returnValue() != null) {
            instruction.returnValue().accept(this);
            stringBuilder.append(" := ");
        }
        Value routine = instruction.routine();
        if (routine instanceof ConstantValue constant && constant.value() instanceof String) {
            stringBuilder.append(constant.value());
        } else {
            routine.accept(this);
        }
        stringBuilder.append("(");
        List<Map.Entry<ArgumentDescriptor, Value>> arguments = new ArrayList<>(instruction.arguments().entrySet());
        arguments.sort(Comparator.comparing(entry -> getDescriptorName(entry.getKey())));
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) {
                stringBuilder.append(", ");
            }
            Map.Entry<ArgumentDescriptor, Value> entry = arguments.get(i);
            String key = getDescriptorName(entry.getKey());
            stringBuilder.append("_").append(key);
            if (entry.getValue() != null) {
                stringBuilder.append(" := ");
                entry.getValue().accept(this);
            }
        }
        stringBuilder.append(")");
        stringBuilder.append(" -> ").append(instruction.next().getIndex()).append(";");
        super.visitCallInstruction(instruction);
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
    public void visitLocalVariableValue(@NotNull VariableValue value) {
        stringBuilder.append("_").append(value.field().index());
        super.visitLocalVariableValue(value);
    }

    @Override
    public void visitFieldVariableValue(@NotNull FieldValue value) {
        if (value.moduleName() != null) {
            stringBuilder.append(value.moduleName()).append(":");
        }
        stringBuilder.append(value.name());
        super.visitFieldVariableValue(value);
    }

    @Override
    public void visitSnapshot(@NotNull ReferenceSnapshot snapshot) {
        stringBuilder.append("=");
        stringBuilder.append(snapshot.hashCode());
    }

    @Override
    public void visitComponentVariableValue(@NotNull ComponentValue value) {
        value.variable().accept(this);
        stringBuilder.append(".");
        stringBuilder.append(value.name());
        super.visitComponentVariableValue(value);
    }

    @Override
    public void visitIndexValue(@NotNull IndexValue value) {
        value.variable().accept(this);
        stringBuilder.append("[");
        value.index().accept(this);
        stringBuilder.append("]");
    }

    @Override
    public void visitConstantValue(@NotNull ConstantValue value) {
        if (value.value() instanceof String) {
            stringBuilder.append("\"").append(value.value()).append("\"");
        } else {
            stringBuilder.append(value.value());
        }
        super.visitConstantValue(value);
    }

    @Override
    public void visitErrorValue(@NotNull ErrorValue value) {
        stringBuilder.append("error");
        super.visitErrorValue(value);
    }

    @Override
    public void visitValueExpression(@NotNull ValueExpression expression) {
        expression.value().accept(this);
        super.visitValueExpression(expression);
    }

    @Override
    public void visitAggregateExpression(@NotNull AggregateExpression expression) {
        stringBuilder.append("[");
        for (int i = 0; i < expression.values().size(); i++) {
            if (i > 0) {
                stringBuilder.append(", ");
            }
            expression.values().get(i).accept(this);
        }
        stringBuilder.append("]");
        super.visitAggregateExpression(expression);
    }

    @Override
    public void visitBinaryExpression(@NotNull BinaryExpression expression) {
        expression.left().accept(this);
        stringBuilder.append(" ");
        stringBuilder.append(switch (expression.operator()) {
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
        });
        stringBuilder.append(" ");
        expression.right().accept(this);
        super.visitBinaryExpression(expression);
    }

    @Override
    public void visitUnaryExpression(@NotNull UnaryExpression expression) {
        stringBuilder.append(switch (expression.operator()) {
            case NOT -> "NOT";
            case NEGATE -> "-";
        });
        if (expression.operator() != UnaryOperator.NEGATE) {
            stringBuilder.append(" ");
        }
        expression.value().accept(this);
        super.visitUnaryExpression(expression);
    }

    @Override
    public void visitCondition(@NotNull Condition condition) {
        condition.getVariable().accept(this);
        stringBuilder.append(" ");
        stringBuilder.append(switch (condition.getConditionType()) {
            case EQUALITY -> "=";
            case INEQUALITY -> "!=";
            case LESS_THAN -> "<";
            case LESS_THAN_OR_EQUAL -> "<=";
            case GREATER_THAN -> ">";
            case GREATER_THAN_OR_EQUAL -> ">=";
        });
        stringBuilder.append(" ");
        condition.getExpression().accept(this);
    }
}
