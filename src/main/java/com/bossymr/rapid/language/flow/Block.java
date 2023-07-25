package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.psi.RapidElement;
import com.bossymr.rapid.language.psi.StatementListType;
import com.bossymr.rapid.language.symbol.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public sealed abstract class Block {

    private final @Nullable String moduleName;
    private final @NotNull String name;
    private final @Nullable RapidType returnType;

    private final @NotNull List<BasicBlock> basicBlocks;
    private final @NotNull Map<StatementListType, BasicBlock> entryBlocks;
    private final @NotNull List<Variable> variables;

    private final @NotNull RapidSymbol element;

    public Block(@NotNull RapidSymbol element, @Nullable String moduleName, @NotNull String name, @Nullable RapidType returnType) {
        this.moduleName = moduleName;
        this.name = name;
        this.returnType = returnType;
        this.basicBlocks = new ArrayList<>();
        this.entryBlocks = new HashMap<>();
        this.variables = new ArrayList<>();
        this.element = element;
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

    public @NotNull List<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public @NotNull BasicBlock getEntryBlock() {
        return entryBlocks.get(StatementListType.STATEMENT_LIST);
    }

    public @Nullable BasicBlock getEntryBlock(@NotNull StatementListType scopeType) {
        return entryBlocks.get(scopeType);
    }

    public @NotNull List<ArgumentGroup> getArgumentGroups() {
        return List.of();
    }

    public @NotNull List<Variable> getVariables() {
        return variables;
    }

    public @Nullable Variable findVariable(@NotNull String name) {
        for (Variable variable : getVariables()) {
            if (name.equals(variable.name())) {
                return variable;
            }
        }
        return null;
    }

    public @NotNull BasicBlock setEntryBlock(@NotNull StatementListType scopeType) {
        if (scopeType == StatementListType.ERROR_CLAUSE) {
            throw new IllegalArgumentException();
        }
        if (getEntryBlock(scopeType) != null) {
            throw new IllegalStateException();
        }
        BasicBlock basicBlock = new BasicBlock.EntryBasicBlock(this, scopeType);
        basicBlocks.add(basicBlock);
        entryBlocks.put(scopeType, basicBlock);
        return basicBlock;
    }

    public @NotNull BasicBlock setErrorClause(@Nullable List<Integer> exceptions) {
        if (getEntryBlock(StatementListType.ERROR_CLAUSE) != null) {
            throw new IllegalStateException();
        }
        BasicBlock basicBlock = new BasicBlock.ErrorBasicBlock(this, exceptions);
        basicBlocks.add(basicBlock);
        entryBlocks.put(StatementListType.ERROR_CLAUSE, basicBlock);
        return basicBlock;
    }

    public @NotNull BasicBlock createBasicBlock() {
        BasicBlock basicBlock = new BasicBlock.IntermediateBasicBlock(this);
        basicBlocks.add(basicBlock);
        return basicBlock;
    }

    public @NotNull Variable createVariable(@Nullable RapidElement element, @Nullable String name, @Nullable FieldType fieldType, @NotNull RapidType type) {
        Variable variable = new Variable(getNextVariableIndex(), element, fieldType, type, name);
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

    public abstract void accept(@NotNull ControlFlowVisitor visitor);

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

        public @Nullable Argument findArgument(@NotNull String name) {
            for (ArgumentGroup argumentGroup : argumentGroups) {
                for (Argument argument : argumentGroup.arguments()) {
                    if (name.equals(argument.name())) {
                        return argument;
                    }
                }
            }
            return null;
        }

        public @NotNull Argument findArgument(int index) {
            return getArgumentGroups().stream()
                    .flatMap(argumentGroup -> argumentGroup.arguments().stream())
                    .toList().get(index);
        }

        @Override
        public void accept(@NotNull ControlFlowVisitor visitor) {
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
        public void accept(@NotNull ControlFlowVisitor visitor) {
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
