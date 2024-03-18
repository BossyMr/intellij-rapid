package com.bossymr.rapid.ide.editor.documentation;

import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.bossymr.rapid.language.symbol.resolve.ResolveService;
import com.intellij.codeInsight.documentation.DocumentationManagerProtocol;
import com.intellij.model.Symbol;
import com.intellij.openapi.project.Project;
import com.intellij.platform.backend.documentation.DocumentationLinkHandler;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.documentation.LinkResolveResult;
import com.intellij.platform.backend.documentation.SymbolDocumentationTargetProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class RapidDocumentationTargetProvider implements SymbolDocumentationTargetProvider, DocumentationLinkHandler {

    @Override
    public @Nullable DocumentationTarget documentationTarget(@NotNull Project project, @NotNull Symbol symbol) {
        if (!(symbol instanceof RapidSymbol)) {
            return null;
        }
        return RapidDocumentationTarget.create(project, (RapidSymbol) symbol, null);
    }

    @Override
    public @Nullable LinkResolveResult resolveLink(@NotNull DocumentationTarget target, @NotNull String url) {
        if (!(url.startsWith(DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL))) {
            return null;
        }
        if (!(target instanceof RapidDocumentationTarget<?> documentationTarget)) {
            return null;
        }
        Project project = documentationTarget.getProject();
        RapidSymbol symbol = getSymbol(project, documentationTarget.getSymbol(), url);
        if (symbol == null) {
            return null;
        }
        String anchor = getAnchor(url);
        return LinkResolveResult.resolvedTarget(RapidDocumentationTarget.create(project, symbol, anchor));
    }

    private @Nullable RapidSymbol getSymbol(@NotNull Project project, @NotNull RapidSymbol symbol, @NotNull String url) {
        if (url.startsWith(DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL)) {
            url = url.substring(DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL.length());
            int index = url.indexOf(DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL_REF_SEPARATOR);
            if (index >= 0) {
                url = url.substring(0, index);
            }
            ResolveService service = ResolveService.getInstance(project);
            if (symbol instanceof PhysicalSymbol physicalSymbol) {
                List<RapidSymbol> symbols = service.getSymbols(physicalSymbol, url);
                return symbols.isEmpty() ? null : symbols.get(0);
            } else {
                return service.getRemoteSymbol("RAPID" + "/" + url);
            }
        }
        return null;
    }

    private @Nullable String getAnchor(@NotNull String url) {
        int index = url.indexOf(DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL_REF_SEPARATOR);
        if (index >= 0) {
            return url.substring(0, index + DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL_REF_SEPARATOR.length());
        }
        return null;
    }
}
