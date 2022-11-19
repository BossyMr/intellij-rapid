package com.bossymr.rapid.language.symbol;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface RapidRecord extends RapidStructure {

    @NotNull List<RapidComponent> getComponents();

}
