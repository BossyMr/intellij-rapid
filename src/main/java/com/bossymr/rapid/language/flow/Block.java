package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.psi.StatementListType;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public sealed abstract class Block {

    private final @Nullable String moduleName;
    private final @NotNull String name;
    private final @Nullable RapidType returnType;

    private final @NotNull List<Instruction> instructions;
    private final @NotNull Map<StatementListType, Instruction> entryBlocks;
    private final @NotNull List<Variable> variables;

    private final @NotNull RapidSymbol element;

    public Block(@NotNull RapidSymbol element, @Nullable String moduleName, @NotNull String name, @Nullable RapidType returnType) {
        this.moduleName = moduleName;
        this.name = name;
        this.returnType = returnType;
        this.instructions = new ArrayList<>();
        this.entryBlocks = new HashMap<>();
        this.variables = new ArrayList<>();
        this.element = element;
    }

    private boolean isEmpty() {
        return instructions.isEmpty();
    }

    private boolean isComplete() {
        if (isEmpty()) {
            return false;
        }
        Instruction last = instructions.get(instructions.size() - 1);
        // TODO: 2023-10-13 Check if last has successor which isn't declared
    }

    public @NotNull RapidSymbol getElement() {
        return element;
    }

    public @Nullable String getModuleName() {
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

    public @NotNull Instruction getEntryInstruction() {
        return entryBlocks.get(StatementListType.STATEMENT_LIST);
    }

    public @Nullable Instruction getEntryInstruction(@NotNull StatementListType scopeType) {
        return entryBlocks.get(scopeType);
    }

    public @NotNull Collection<Instruction> getEntryBlocks() {
        return entryBlocks.values();
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

    public void setEntryInstruction(@NotNull StatementListType scopeType, @NotNull Instruction instruction) {
        if (scopeType == StatementListType.ERROR_CLAUSE) {
            throw new IllegalArgumentException();
        }
        if (getEntryInstruction(scopeType) != null) {
            throw new IllegalStateException();
        }
        entryBlocks.put(scopeType, instruction);
    }

    public @NotNull BasicBlock setErrorClause(@Nullable List<Integer> exceptions, @NotNull Instruction instruction) {
        if (getEntryInstruction(StatementListType.ERROR_CLAUSE) != null) {
            throw new IllegalStateException();
        }
        entryBlocks.put(StatementListType.ERROR_CLAUSE, )
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

        public FunctionBlock(@NotNull RapidRoutine routine, @Nullable String moduleName) {
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

        public @NotNull Argument findArgument(int index) {
            return getArgumentGroups().stream()
                    .flatMap(argumentGroup -> argumentGroup.arguments().stream())
                    .toList().get(index);
        }

        @Override
        public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
            return
            visitor.visitFunctionBlock(this);
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
