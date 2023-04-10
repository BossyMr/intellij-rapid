package com.bossymr.rapid.language.symbol.resolve;

import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidReferenceExpression;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RemoteRobotService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ResolveUtil {

    private static final Logger logger = Logger.getInstance(ResolveUtil.class);

    private ResolveUtil() {
        throw new UnsupportedOperationException();
    }

    public static @Nullable RapidSymbol findSymbol(@NotNull Project project, @NotNull String address) {
        RemoteRobotService service = RemoteRobotService.getInstance();
        RapidRobot robot = service.getRobot();
        if (robot == null) return null;
        String[] sections = address.split("/");
        for (RapidTask task : robot.getTasks()) {
            if (sections[1].equals(task.getName())) {
                if (sections.length == 2) return task;
                for (PhysicalModule module : task.getModules(project)) {
                    if (sections[2].equals(module.getName())) {
                        if (sections.length == 3) return module;
                        for (RapidAccessibleSymbol accessibleSymbol : module.getSymbols()) {
                            if (sections[3].equals(accessibleSymbol.getName())) {
                                if (sections.length == 4) {
                                    return accessibleSymbol;
                                } else {
                                    return findChild(accessibleSymbol, sections[4]);
                                }
                            }
                        }
                    }
                }
            }
        }
        try {
            return robot.getSymbol(sections[1]);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (IOException e) {
            logger.error(e);
            throw new AssertionError();
        }
    }

    private static @Nullable RapidSymbol findChild(@NotNull RapidSymbol symbol, @NotNull String name) {
        if (symbol instanceof RapidRoutine routine && routine.getParameters() != null) {
            for (RapidParameterGroup group : routine.getParameters()) {
                for (RapidParameter parameter : group.getParameters()) {
                    if (name.equals(parameter.getName())) {
                        return parameter;
                    }
                }
            }
        }
        return null;
    }

    public static @NotNull List<RapidSymbol> getSymbols(@NotNull RapidReferenceExpression expression) {
        RapidExpression qualifier = expression.getQualifier();
        if (qualifier != null) {
            RapidType dataType = qualifier.getType();
            if (dataType != null) {
                RapidStructure structure = dataType.getTargetStructure();
                if (structure instanceof RapidRecord record) {
                    return new ArrayList<>(record.getComponents());
                }
            }
            return Collections.emptyList();
        }
        ResolveScopeProcessor processor = new ResolveScopeProcessor(expression, null);
        ResolveScopeVisitor visitor = new ResolveScopeVisitor(expression, processor);
        visitor.process();
        return processor.getSymbols();
    }

    public static @Nullable RapidStructure getStructure(@NotNull PsiElement element, @NotNull String name) {
        List<RapidSymbol> results = getSymbols(element, name);
        if (results.size() == 0) return null;
        RapidSymbol symbol = results.iterator().next();
        return symbol instanceof RapidStructure structure ? structure : null;
    }

    public static @NotNull List<RapidSymbol> getSymbols(@NotNull RapidReferenceExpression expression, @NotNull String name) {
        RapidExpression qualifier = expression.getQualifier();
        if (qualifier != null) {
            RapidType dataType = qualifier.getType();
            if (dataType != null) {
                RapidStructure structure = dataType.getTargetStructure();
                if (structure instanceof RapidRecord record) {
                    List<RapidSymbol> symbols = new ArrayList<>();
                    for (RapidComponent component : record.getComponents()) {
                        if (name.equalsIgnoreCase(component.getName())) {
                            symbols.add(component);
                        }
                    }
                    return symbols;
                }
            }
            return Collections.emptyList();
        }
        return getSymbols((PsiElement) expression, name);
    }


    public static @NotNull List<RapidSymbol> getSymbols(@NotNull PsiElement element, @NotNull String name) {
        return getSymbols(element, new ResolveScopeProcessor(element, name));
    }

    public static @NotNull List<RapidSymbol> getSymbols(@NotNull PsiElement element, @NotNull ResolveScopeProcessor processor) {
        ResolveScopeVisitor visitor = new ResolveScopeVisitor(element, processor);
        visitor.process();
        return processor.getSymbols();
    }

}
