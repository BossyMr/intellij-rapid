package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.ide.documentation.VirtualDocumentationTarget;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.intellij.navigation.NavigationTarget;
import com.intellij.openapi.project.Project;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

/**
 * A {@code VirtualSymbol} is a symbol which is represented by a connected robot, and is not represented by an element
 * in a source code file.
 */
@SuppressWarnings("UnstableApiUsage")
public interface VirtualSymbol extends RapidSymbol {

    @Override
    default @NotNull String getCanonicalName() {
        return getName();
    }

    @Override
    @NotNull String getName();

    @Override
    default @NotNull Collection<? extends NavigationTarget> getNavigationTargets(@NotNull Project project) {
        // TODO: 2023-04-23 Allow navigation, to select corresponding element in Robot tool window.
        return Set.of();
    }

    @Override
    @NotNull VirtualPointer<? extends VirtualSymbol> createPointer();

    @Override
    default @NotNull DocumentationTarget getDocumentationTarget() {
        return new VirtualDocumentationTarget(this);
    }
}
