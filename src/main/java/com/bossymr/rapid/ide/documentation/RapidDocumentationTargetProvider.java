package com.bossymr.rapid.ide.documentation;

import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.bossymr.rapid.language.symbol.resolve.RapidResolveService;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.codeInsight.documentation.DocumentationManagerProtocol;
import com.intellij.model.Symbol;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.platform.backend.documentation.DocumentationLinkHandler;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.documentation.LinkResolveResult;
import com.intellij.platform.backend.documentation.SymbolDocumentationTargetProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class RapidDocumentationTargetProvider implements SymbolDocumentationTargetProvider, DocumentationLinkHandler {

    @Override
    public @Nullable DocumentationTarget documentationTarget(@NotNull Project project, @NotNull Symbol symbol) {
        if (symbol instanceof RapidSymbol target) {
            return target.getDocumentationTarget(project);
        }
        return null;
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
            List<RapidSymbol> symbols = RapidResolveService.getInstance(project).findSymbols(physicalSymbol, link);
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
                RapidSymbol child = RapidResolveService.findChild(result, sections[1]);
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
