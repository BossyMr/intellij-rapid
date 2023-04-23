package com.bossymr.rapid.ide.documentation;

import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.documentation.LinkResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code DocumentationLinkProvider} is a {@link DocumentationTarget} which is capable of resolving references.
 */
public interface DocumentationLinkProvider extends DocumentationTarget {

    /**
     * Attempts to resolve the specified link.
     *
     * @param link the link.
     * @return the resolved target, or {@code null} if the link could not be resolved.
     */
    @Nullable LinkResolveResult resolveLink(@NotNull String link);

}
