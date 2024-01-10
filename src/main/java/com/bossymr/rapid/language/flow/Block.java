package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.flow.expression.Expression;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.psi.BlockType;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public sealed abstract class Block {

    private final @NotNull String moduleName;
    private final @NotNull String name;
    private final @Nullable RapidType returnType;

    private final @NotNull Map<BlockType, EntryInstruction> entryPoints;
    private final @NotNull List<Instruction> instructions;

    private final @NotNull List<Variable> variables;
    private final @NotNull RapidSymbol element;

    public Block(@NotNull RapidSymbol element, @NotNull String moduleName, @NotNull String name, @Nullable RapidType returnType) {
        this.moduleName = moduleName;
        this.name = name;
        this.returnType = returnType;
        this.instructions = new ArrayList<>();
        this.entryPoints = new HashMap<>();
        this.variables = new ArrayList<>();
        this.element = element;
    }

    public @NotNull RapidSymbol getElement() {
        return element;
    }

    public @NotNull String getModuleName() {
        return moduleName;
    }

    public @NotNull String getName() {
        return name;
    }

    public @Nullable RapidType getReturnType() {
        return returnType;
    }

    public @NotNull List<Instruction> getInstructions() {
        return instructions;
    }

    public @Nullable EntryInstruction getEntryInstruction(@NotNull BlockType scopeType) {
        return entryPoints.get(scopeType);
    }

    public @NotNull Collection<EntryInstruction> getEntryInstructions() {
        return entryPoints.values();
    }

    public @NotNull List<ArgumentGroup> getArgumentGroups() {
        return List.of();
    }

    public @NotNull List<Variable> getVariables() {
        return variables;
    }

    public @Nullable Variable findVariable(@NotNull String name) {
        for (Variable variable : getVariables()) {
            if (name.equals(variable.getName())) {
                return variable;
            }
        }
        return null;
    }

    public @Nullable Argument findArgument(@NotNull String name) {
        for (ArgumentGroup argumentGroup : getArgumentGroups()) {
            for (Argument argument : argumentGroup.arguments()) {
                if (name.equals(argument.getName())) {
                    return argument;
                }
            }
        }
        return null;
    }

    public void setEntryInstruction(@NotNull BlockType scopeType, @NotNull Instruction instruction) {
        if (scopeType == BlockType.ERROR_CLAUSE) {
            throw new IllegalArgumentException();
        }
        if (getEntryInstruction(scopeType) != null) {
            throw new IllegalStateException();
        }
        entryPoints.put(scopeType, new EntryInstruction(scopeType, instruction));
    }

    public void setErrorClause(@NotNull List<Expression> exceptions, @NotNull Instruction instruction) {
        if (getEntryInstruction(BlockType.ERROR_CLAUSE) != null) {
            throw new IllegalStateException();
        }
        entryPoints.put(BlockType.ERROR_CLAUSE, new EntryInstruction.ErrorEntryInstruction(instruction, exceptions));
    }

    public @NotNull Variable createVariable(@Nullable String name, @Nullable FieldType fieldType, @NotNull RapidType type) {
        Variable variable = new Variable(getNextVariableIndex(), fieldType, type, name);
        variables.add(variable);
        return variable;
    }

    public @NotNull Argument createArgument(@NotNull String name, @NotNull RapidType type, @NotNull ParameterType parameterType) {
        return new Argument(getNextVariableIndex(), parameterType, type, name);
    }

    protected int getNextVariableIndex() {
        return getArgumentGroups().stream()
                .mapToInt(argumentGroup -> argumentGroup.arguments().size())
                .sum() + getVariables().size();
    }

    public abstract <R> R accept(@NotNull ControlFlowVisitor<R> visitor);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return Objects.equals(element, block.element);
    }

    @Override
    public int hashCode() {
        return Objects.hash(element);
    }

    @Override
    public String toString() {
        return "Block{" +
                "moduleName='" + moduleName + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public static final class FunctionBlock extends Block {

        private final @NotNull RoutineType routineType;
        private final @NotNull List<ArgumentGroup> argumentGroups;

        public FunctionBlock(@NotNull RapidRoutine routine, @NotNull String moduleName) {
            super(routine, moduleName, Objects.requireNonNull(routine.getName()), routine.getType());
            this.routineType = routine.getRoutineType();
            this.argumentGroups = routineType != RoutineType.TRAP ? new ArrayList<>() : List.of();
        }

        @Override
        public @NotNull RapidRoutine getElement() {
            return (RapidRoutine) super.getElement();
        }

        public @NotNull RoutineType getRoutineType() {
            return routineType;
        }

        @Override
        public @NotNull List<ArgumentGroup> getArgumentGroups() {
            return argumentGroups;
        }

        @Override
        public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
            return visitor.visitFunctionBlock(this);
        }

        @Override
        public String toString() {
            return "FunctionBlock{" +
                    "routineType=" + routineType +
                    ", moduleName='" + getModuleName() + '\'' +
                    ", name='" + getName() + '\'' +
                    ", returnType=" + getReturnType() +
                    '}';
        }
    }

    public static final class FieldBlock extends Block {

        private final @NotNull FieldType fieldType;

        public FieldBlock(@NotNull RapidField field, @NotNull String moduleName) {
            super(field, moduleName, Objects.requireNonNull(field.getName()), field.getType());
            this.fieldType = field.getFieldType();
        }

        public @NotNull RapidField getElement() {
            return (RapidField) super.getElement();
        }

        public @NotNull FieldType getFieldType() {
            return fieldType;
        }

        @Override
        public @NotNull RapidType getReturnType() {
            return Objects.requireNonNull(super.getReturnType());
        }

        @Override
        public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
            return
                    visitor.visitFieldBlock(this);
        }

        @Override
        public String toString() {
            return "FieldBlock{" +
                    "fieldType=" + fieldType +
                    ", moduleName='" + getModuleName() + '\'' +
                    ", name='" + getName() + '\'' +
                    ", returnType=" + getReturnType() +
                    '}';
        }
    }
}
