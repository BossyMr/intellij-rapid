package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.flow.conditon.Value;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.psi.StatementListType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public sealed interface Scope {

    @Nullable StatementListType scopeType();

    int index();

    @NotNull List<Instruction> instructions();

    record EntryScope(@NotNull StatementListType scopeType, int index,
                      @NotNull List<Instruction> instructions) implements Scope {
    }

    record IntermediateScope(int index, @NotNull List<Instruction> instructions) implements Scope {
        @Override
        public @Nullable StatementListType scopeType() {
            return null;
        }
    }

    record ErrorScope(int index, @Nullable List<Value> exceptions,
                      @NotNull List<Instruction> instructions) implements Scope {
        @Override
        public @NotNull StatementListType scopeType() {
            return StatementListType.ERROR_CLAUSE;
        }
    }

}
