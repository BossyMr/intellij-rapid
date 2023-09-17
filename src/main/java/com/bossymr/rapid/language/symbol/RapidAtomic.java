package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.language.type.RapidAliasType;
import com.bossymr.rapid.language.type.RapidAtomicType;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.navigation.TargetPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code RapidAtomic} is a structure which represents a primitive type.
 */
@SuppressWarnings("UnstableApiUsage")
public interface RapidAtomic extends RapidStructure {

    @Override
    @NotNull String getName();

    @Override
    default @NotNull RapidType createType() {
        return new RapidAtomicType(this);
    }

    /**
     * If this type is a {@link ValueType#SEMI_VALUE_TYPE semi-value} type, returns the associated type which should be
     * used instead of this type.
     *
     * @return the associated type of this type.
     */
    @Nullable RapidType getAssociatedType();

    @Override
    @NotNull RapidPointer<? extends RapidAtomic> createPointer();

    @Override
    default @NotNull TargetPresentation getTargetPresentation() {
        return TargetPresentation.builder(getPresentableName())
                .icon(RapidIcons.ATOMIC)
                .presentation();
    }
}
