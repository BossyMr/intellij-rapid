package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.ide.search.RapidSymbolSearchTarget;
import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.RapidRobot;
import com.intellij.find.usages.api.SearchTarget;
import com.intellij.find.usages.symbol.SearchTargetSymbol;
import com.intellij.model.Pointer;
import com.intellij.model.Symbol;
import com.intellij.navigation.NavigatableSymbol;
import com.intellij.navigation.TargetPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A {@code RapidSymbol} is a symbol representing an element in the {@code RAPID} programming language. A
 * {@code RapidSymbol} might be either a {@link PhysicalSymbol}, which is represented by an element in a
 * {@link RapidFile file},  or a {@link VirtualSymbol}, which exists only on a connected {@link RapidRobot robot}.
 * <p>
 * A {@code RapidSymbol} is valid in a single read-action. However, it should not be stored in between read-actions, as
 * it might be removed in a write-action; if the source code file is modified or if a robot is disconnected. To store a
 * symbol between read-actions, use {@link #createPointer()} to create a pointer which can be used to restore this
 * symbol.
 */
@SuppressWarnings("UnstableApiUsage")
public interface RapidSymbol extends Symbol, NavigatableSymbol, SearchTargetSymbol {

    /**
     * Returns a pointer which can be used to restore this symbol between read-actions.
     *
     * @return a pointer which can be used to restore this symbol between read-actions.
     */
    @Override
    @NotNull Pointer<? extends RapidSymbol> createPointer();

    /**
     * Returns the canonical name of this symbol, used to identify a specific symbol.
     * <p>
     * The structure of a conical name is {@code RAPID/[Task]/[Module]/[Symbol]/[Argument|Record]}. However, the
     * {@code RAPID/[Task]} section is not included, as the task depends on where the program is uploaded.
     *
     * @return the canonical name of this symbol, or {@code null} if this symbol is incomplete.
     */
    @Nullable String getCanonicalName();

    /**
     * Returns the name of this symbol.
     *
     * @return the name of this symbol, or {@code null} if this symbol is incomplete.
     */
    @Nullable String getName();

    /**
     * Returns the presentable name of this symbol.
     *
     * @return the presentable name of this symbol.
     */
    @NotNull
    default String getPresentableName() {
        return Objects.requireNonNullElse(getName(), "<ID>");
    }

    /**
     * Compute the presentation which is used to display this symbol.
     *
     * @return the presentation used to display this symbol.
     */
    @RequiresReadLock
    @RequiresBackgroundThread
    @NotNull TargetPresentation getTargetPresentation();

    @NotNull DocumentationTarget getDocumentationTarget(@NotNull Project project);

    @Override
    default @NotNull SearchTarget getSearchTarget() {
        return new RapidSymbolSearchTarget(this);
    }

    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();
}
