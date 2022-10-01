package com.bossymr.rapid.network.controller.rapid;

import com.bossymr.rapid.network.NetworkQuery;
import com.bossymr.rapid.network.controller.Controller;
import com.bossymr.rapid.network.controller.Node;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Rapid extends Node {

    public Rapid(@NotNull Controller controller) {
        super(controller);
    }

    public @NotNull NetworkQuery<List<SymbolEntity>> getSymbols(@NotNull SymbolSearchQuery query) {
        return null;
    }

}
