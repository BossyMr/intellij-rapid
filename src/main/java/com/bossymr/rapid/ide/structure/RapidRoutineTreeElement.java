package com.bossymr.rapid.ide.structure;

import com.bossymr.rapid.language.psi.FormatUtil;
import com.bossymr.rapid.language.psi.FormatUtil.Option;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.intellij.ide.structureView.StructureViewTreeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

public class RapidRoutineTreeElement extends RapidSymbolTreeElement<PhysicalRoutine> {

    public RapidRoutineTreeElement(@NotNull PhysicalRoutine alias) {
        super(alias);
    }

    @Override
    public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
        return Collections.emptyList();
    }

    @Override
    public @Nullable String getPresentableText() {
        if (getValue() == null) return "";
        return FormatUtil.format(getValue(),
                EnumSet.of(Option.SHOW_NAME, Option.SHOW_TYPE, Option.SHOW_TYPE_AFTER, Option.SHOW_PARAMETERS),
                EnumSet.of(Option.SHOW_TYPE));
    }
}
