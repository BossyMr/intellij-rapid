package com.bossymr.rapid.language.flow.data.block;

import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.microsoft.z3.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DataFlowBlock {

    private final @NotNull AtomicReference<Context> context;
    private final @NotNull Instruction instruction;
    private final @NotNull List<DataFlowState> states = new ArrayList<>();

    public DataFlowBlock(@NotNull AtomicReference<Context> context, @NotNull Instruction instruction) {
        this.context = context;
        this.instruction = instruction;
    }

    public @Nullable Context getContext() {
        return context.get();
    }

    public void setContext(@Nullable Context context) {
        this.context.set(context);
    }

    public @NotNull Instruction getInstruction() {
        return instruction;
    }

    public @NotNull List<DataFlowState> getStates() {
        return states;
    }

    @Override
    public String toString() {
        return "DataFlowBlock{" +
                "index=" + instruction.getIndex() +
                ", basicBlock=" + instruction +
                ", states=" + states +
                '}';
    }
}
