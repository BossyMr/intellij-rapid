package com.bossymr.rapid.language.psi.stubs;

import com.intellij.psi.stubs.NamedStub;
import com.bossymr.rapid.language.psi.RapidAlias;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an alias stub.
 */
public interface RapidAliasStub extends NamedStub<RapidAlias> {

    /**
     * Checks if the alias is local.
     *
     * @return if the alias is local.
     */
    boolean isLocal();

    /**
     * Returns the type of this alias.
     *
     * @return the type of this alias.
     */
    @Nullable String getType();
}
