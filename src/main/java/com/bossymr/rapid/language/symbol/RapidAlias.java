package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.language.type.RapidAliasType;
import com.bossymr.rapid.language.type.RapidAtomicType;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.navigation.TargetPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code RapidAlias} is a structure which represents an alias for another type.
 */
@SuppressWarnings("UnstableApiUsage")
public interface RapidAlias extends RapidStructure {

    /**
     * Returns the type of this alias.
     *
     * @return the type of this alias, or {@code null} if this symbol is incomplete.
     */
    @Nullable RapidType getType();

    @Override
    default @NotNull RapidType createType() {
        return new RapidAliasType(this);
    }

    @Override
    @NotNull RapidPointer<? extends RapidAlias> createPointer();

    @Override
    default @NotNull TargetPresentation getTargetPresentation() {
        return TargetPresentation.builder(getPresentableName())
                .icon(RapidIcons.ALIAS)
                .presentation();
    }
}
