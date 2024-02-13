package com.bossymr.rapid.ide.editor.documentation;

import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.bossymr.rapid.language.symbol.resolve.ResolveService;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.codeInsight.documentation.DocumentationManagerProtocol;
import com.intellij.lang.ASTNode;
import com.intellij.model.Symbol;
import com.intellij.model.psi.PsiSymbolReference;
import com.intellij.model.psi.PsiSymbolReferenceService;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.platform.backend.documentation.DocumentationLinkHandler;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.documentation.DocumentationTargetProvider;
import com.intellij.platform.backend.documentation.LinkResolveResult;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class RapidDocumentationTargetProvider implements DocumentationTargetProvider, DocumentationLinkHandler {

    @Override
    public @NotNull List<? extends @NotNull DocumentationTarget> documentationTargets(@NotNull PsiFile file, int offset) {
        PsiElement element = file.findElementAt(offset);
        if (element == null) {
            return List.of();
        }
        ASTNode elementNode = element.getNode();
        IElementType elementType = elementNode.getElementType();
        if (elementType == RapidTokenTypes.IDENTIFIER) {
            element = element.getParent();
        }
        List<Symbol> symbols = new ArrayList<>();
        if (element instanceof PhysicalSymbol symbol && symbol.getNameIdentifier() != null) {
            symbols.add(symbol);
        }
        for (PsiSymbolReference reference : PsiSymbolReferenceService.getService().getReferences(element)) {
            symbols.addAll(reference.resolveReference());
        }
        Project project = file.getProject();
        return symbols.stream()
                      .filter(symbol -> symbol instanceof RapidSymbol)
                      .map(symbol -> (RapidSymbol) symbol)
                      .map(symbol -> symbol.getDocumentationTarget(project))
                      .toList();
    }

    @Override
    public @Nullable LinkResolveResult resolveLink(@NotNull DocumentationTarget target, @NotNull String url) {
        if (!(url.startsWith(DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL))) {
            return null;
        }
        if (!(target instanceof RapidDocumentationTarget<?> documentationTarget)) {
            return null;
        }
        String link = url.substring(DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL.length());
        RapidSymbol symbol = documentationTarget.getSymbol();
        Project project = documentationTarget.getProject();
        if (symbol instanceof PhysicalSymbol physicalSymbol) {
            List<RapidSymbol> symbols = ResolveService.getInstance(project).findSymbols(physicalSymbol, link);
            if (symbols.isEmpty()) {
                return null;
            }
            return LinkResolveResult.resolvedTarget(symbols.get(0).getDocumentationTarget(project));
        } else {
            RobotService service = RobotService.getInstance();
            RapidRobot robot = service.getRobot();
            if (robot == null) {
                return null;
            }
            String[] sections = link.split("/");
            if (sections.length == 0) {
                throw new IllegalArgumentException("Cannot resolve symbol " + link);
            }
            try {
                VirtualSymbol result = robot.getSymbol(sections[0]);
                if (result == null) {
                    return null;
                }
                if (sections.length == 1) {
                    return LinkResolveResult.resolvedTarget(result.getDocumentationTarget(project));
                }
                RapidSymbol child = ResolveService.findChild(result, sections[1]);
                if (child == null) {
                    return null;
                }
                return LinkResolveResult.resolvedTarget(child.getDocumentationTarget(project));
            } catch (IOException e) {
                return null;
            } catch (InterruptedException e) {
                throw new ProcessCanceledException();
            }
        }

    }
}
