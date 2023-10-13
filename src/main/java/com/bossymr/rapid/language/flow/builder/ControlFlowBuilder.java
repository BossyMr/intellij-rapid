package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.builder.RapidBuilder;
import com.bossymr.rapid.language.builder.RapidModuleBuilder;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.BlockDescriptor;
import com.bossymr.rapid.language.flow.ControlFlow;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ControlFlowBuilder implements RapidBuilder {

    private final @NotNull Project project;
    private final @NotNull Map<BlockDescriptor, Block> controlFlow;

    public ControlFlowBuilder(@NotNull Project project) {
        this.project = project;
        this.controlFlow = new HashMap<>();
    }

    @Override
    public @NotNull RapidBuilder withModule(@NotNull String name, @NotNull Consumer<RapidModuleBuilder> consumer) {
        ControlFlowModuleBuilder builder = new ControlFlowModuleBuilder(name, controlFlow);
        consumer.accept(builder);
        return this;
    }

    public @NotNull ControlFlow getControlFlow() {
        return new ControlFlow(project, controlFlow);
    }
}
