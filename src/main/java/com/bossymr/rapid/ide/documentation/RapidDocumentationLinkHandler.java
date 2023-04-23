package com.bossymr.rapid.ide.documentation;

import com.intellij.codeInsight.documentation.DocumentationManagerProtocol;
import com.intellij.platform.backend.documentation.DocumentationLinkHandler;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.documentation.LinkResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidDocumentationLinkHandler implements DocumentationLinkHandler {

    @Override
    public @Nullable LinkResolveResult resolveLink(@NotNull DocumentationTarget target, @NotNull String url) {
        if (!(target instanceof DocumentationLinkProvider provider)) {
            return null;
        }
        if (url.startsWith(DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL)) {
            String link = url.substring(DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL.length());
            return provider.resolveLink(link);
        }
        return null;
    }
}
