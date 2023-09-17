package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.language.type.RapidAliasType;
import com.bossymr.rapid.language.type.RapidRecordType;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.navigation.TargetPresentation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A {@code RapidRecord} is a structure which represents a structured object.
 */
@SuppressWarnings("UnstableApiUsage")
public interface RapidRecord extends RapidStructure {

    /**
     * Returns the components contained in this record.
     *
     * @return the components.
     */
    @NotNull List<RapidComponent> getComponents();

    @Override
    default @NotNull RapidType createType() {
        return new RapidRecordType(this);
    }

    @Override
    @NotNull RapidPointer<? extends RapidRecord> createPointer();

    @Override
    default @NotNull TargetPresentation getTargetPresentation() {
        return TargetPresentation.builder(getPresentableName())
                .icon(RapidIcons.RECORD)
                .presentation();
    }
}
