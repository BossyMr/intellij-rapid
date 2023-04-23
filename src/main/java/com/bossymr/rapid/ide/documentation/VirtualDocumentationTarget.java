package com.bossymr.rapid.ide.documentation;

import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.platform.backend.documentation.DocumentationResult;
import com.intellij.platform.backend.documentation.LinkResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;


public class VirtualDocumentationTarget extends RapidDocumentationTarget<VirtualSymbol> {

    public VirtualDocumentationTarget(@NotNull VirtualSymbol symbol) {
        super(symbol);
    }

    @Override
    public @NotNull DocumentationResult computeDocumentation() {
        return DocumentationResult.documentation(getSignature());
    }

    @Override
    public @Nullable LinkResolveResult resolveLink(@NotNull String link) {
        RobotService service = RobotService.getInstance();
        RapidRobot robot = service.getRobot();
        if (robot == null) {
            return null;
        }
        String[] sections = link.split("/");
        try {
            VirtualSymbol symbol = robot.getSymbol(sections[1]);
            if (symbol == null) {
                return null;
            }
            return LinkResolveResult.resolvedTarget(symbol.getDocumentationTarget());
        } catch (IOException e) {
            return null;
        } catch (InterruptedException e) {
            throw new ProcessCanceledException();
        }
    }
}
