package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.flow.conditon.Value;
import com.bossymr.rapid.language.psi.StatementListType;
import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.RoutineType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public sealed abstract class Block {

    private final @NotNull String moduleName;
    private final @NotNull String name;
    private final @Nullable RapidType returnType;

    private final @NotNull List<BasicBlock> basicBlocks;
    private final @NotNull Map<StatementListType, BasicBlock> entryBlocks;
    private final @NotNull List<Variable> variables;

    public Block(@NotNull String moduleName, @NotNull String name, @Nullable RapidType returnType) {
        this.moduleName = moduleName;
        this.name = name;
        this.returnType = returnType;
        this.basicBlocks = new ArrayList<>();
        this.entryBlocks = new HashMap<>();
        this.variables = new ArrayList<>();
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

    public @NotNull List<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public @NotNull BasicBlock getEntryBlock() {
        return entryBlocks.get(StatementListType.STATEMENT_LIST);
    }

    public @Nullable BasicBlock getEntryBlock(@NotNull StatementListType scopeType) {
        return entryBlocks.get(scopeType);
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
        BasicBlock basicBlock = new BasicBlock.EntryBasicBlock(getBasicBlocks().size(), scopeType);
        basicBlocks.add(basicBlock);
        entryBlocks.put(scopeType, basicBlock);
        return basicBlock;
    }

    public @NotNull BasicBlock setErrorClause(@Nullable List<Value> exceptions) {
        if (getEntryBlock(StatementListType.ERROR_CLAUSE) != null) {
            throw new IllegalStateException();
        }
        BasicBlock basicBlock = new BasicBlock.ErrorBasicBlock(getBasicBlocks().size(), exceptions);
        basicBlocks.add(basicBlock);
        entryBlocks.put(StatementListType.ERROR_CLAUSE, basicBlock);
        return basicBlock;
    }

    public @NotNull BasicBlock createBasicBlock() {
        BasicBlock basicBlock = new BasicBlock.IntermediateBasicBlock(getBasicBlocks().size());
        basicBlocks.add(basicBlock);
        return basicBlock;
    }

    public @NotNull Variable createVariable(@Nullable String name, @Nullable FieldType fieldType, @NotNull RapidType type, @Nullable Object initialValue) {
        Variable variable = new Variable(getNextVariableIndex(), initialValue, fieldType, type, name);
        variables.add(variable);
        return variable;
    }

    public @NotNull Argument createArgument(@NotNull String name, @NotNull RapidType type, @NotNull ParameterType parameterType) {
        return new Argument(getNextVariableIndex(), parameterType, type, name);
    }

    protected int getNextVariableIndex() {
        return getVariables().size();
    }

    public abstract void accept(@NotNull ControlFlowVisitor visitor);

    @Override
    public String toString() {
        return "Block{" +
                "moduleName='" + moduleName + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return Objects.equals(moduleName, block.moduleName) && Objects.equals(name, block.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(moduleName, name);
    }

    public static final class FunctionBlock extends Block {

        private final @NotNull RoutineType routineType;
        private final @Nullable List<ArgumentGroup> argumentGroups;

        public FunctionBlock(@NotNull String moduleName, @NotNull String name, @Nullable RapidType returnType, @NotNull RoutineType routineType, boolean hasArguments) {
            super(moduleName, name, returnType);
            this.argumentGroups = hasArguments ? new ArrayList<>() : null;
            this.routineType = routineType;
        }

        public @NotNull RoutineType getRoutineType() {
            return routineType;
        }

        public @Nullable List<ArgumentGroup> getArgumentGroups() {
            return argumentGroups;
        }

        public @Nullable Argument findArgument(@NotNull String name) {
            if (argumentGroups == null) {
                return null;
            }
            for (ArgumentGroup argumentGroup : argumentGroups) {
                for (Argument argument : argumentGroup.arguments()) {
                    if (name.equals(argument.name())) {
                        return argument;
                    }
                }
            }
            return null;
        }

        @Override
        protected int getNextVariableIndex() {
            int size = getVariables().size();
            if (getArgumentGroups() != null) {
                size += getArgumentGroups().size();
            }
            return size;
        }

        @Override
        public void accept(@NotNull ControlFlowVisitor visitor) {
            visitor.visitFunctionBlock(this);
        }
    }

    public static final class FieldBlock extends Block {

        private final @NotNull FieldType fieldType;

        public FieldBlock(@NotNull String moduleName, @NotNull String name, @NotNull RapidType returnType, @NotNull FieldType fieldType) {
            super(moduleName, name, returnType);
            this.fieldType = fieldType;
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
    }
}
