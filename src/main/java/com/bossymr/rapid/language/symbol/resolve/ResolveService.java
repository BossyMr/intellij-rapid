package com.bossymr.rapid.language.symbol.resolve;

import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidReferenceExpression;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.language.type.RapidType;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public final class ResolveService {

    private final @NotNull Project project;

    public ResolveService(@NotNull Project project) {
        this.project = project;
    }

    public static @NotNull ResolveService getInstance(@NotNull Project project) {
        return project.getService(ResolveService.class);
    }

    /**
     * Returns the "children" of the specified symbol, with the specified name. A child is either a parameter to a
     * routine,
     * or a component to a record.
     *
     * @param symbol the name.
     * @param name the name of the child.
     * @return the children to the specified symbol with the specified name.
     */
    public static @NotNull List<RapidSymbol> getChildSymbol(@NotNull RapidSymbol symbol, @NotNull String name) {
        List<RapidSymbol> children = new ArrayList<>();
        if (symbol instanceof RapidRoutine routine) {
            List<? extends RapidParameterGroup> parameters = routine.getParameters();
            if (parameters != null) {
                for (RapidParameterGroup parameterGroup : parameters) {
                    for (RapidParameter parameter : parameterGroup.getParameters()) {
                        if (name.equalsIgnoreCase(parameter.getName())) {
                            children.add(parameter);
                        }
                    }
                }
            }
        } else if (symbol instanceof RapidRecord record) {
            for (RapidComponent component : record.getComponents()) {
                if (name.equalsIgnoreCase(component.getName())) {
                    children.add(component);
                }
            }
        }
        return List.copyOf(children);
    }

    /**
     * Resolves the specified reference expression. If the reference expression might resolve to one of multiple
     * symbols, multiple symbols might be returned.
     *
     * @param expression the reference expression.
     * @return the symbols which the expression might resolve to.
     */
    public @NotNull List<RapidSymbol> getSymbols(@NotNull RapidReferenceExpression expression) {
        PsiElement identifier = expression.getIdentifier();
        if (identifier == null) {
            return List.of();
        }
        String name = identifier.getText();
        List<RapidSymbol> symbols = getQualifier(expression, name);
        if (symbols != null) {
            return symbols;
        }
        return getSymbols(expression, name);
    }

    /**
     * Resolves all symbols reachable from the specified element.
     *
     * @param context the element.
     * @return all symbols reachable from the specified element.
     */
    public @NotNull List<RapidSymbol> getAllSymbols(@NotNull PsiElement context) {
        ResolveScopeProcessor processor = new ResolveScopeProcessor(context, null);
        ResolveScopeVisitor visitor = new ResolveScopeVisitor(context, processor);
        visitor.process();
        return processor.getSymbols();
    }

    /**
     * Resolves all symbols reachable from the specified element, with the specified name.
     *
     * @param context the element.
     * @param name the name of the symbol.
     * @return all symbols reachable from the specified element, with the specified name.
     */
    public @NotNull List<RapidSymbol> getSymbols(@NotNull PsiElement context, @NotNull String name) {
        ResolveScopeProcessor processor = new ResolveScopeProcessor(context, name);
        ResolveScopeVisitor visitor = new ResolveScopeVisitor(context, processor);
        visitor.process();
        return processor.getSymbols();
    }

    /**
     * Attempts to resolve the symbol with the specified {@link RapidSymbol#getCanonicalName() canonical name} on a
     * connected robot.
     *
     * @param canonicalName the canonical name of the symbol.
     * @return the symbol or {@code null} if either a symbol with the specified canonical name was not found or a robot is
     * not currently connected.
     */
    public @Nullable RapidSymbol getRemoteSymbol(@NotNull String canonicalName) {
        RobotService service = RobotService.getInstance();
        RapidRobot robot = service.getRobot();
        if (robot == null) {
            return null;
        }
        String[] sections = canonicalName.split("/");
        if (sections.length < 2) {
            throw new IllegalArgumentException("Malformed name: " + canonicalName);
        }
        RapidSymbol symbol = getRemotePhysicalSymbol(robot, sections);
        if (symbol != null) {
            return symbol;
        }
        return getRemoteVirtualSymbol(robot, sections);
    }

    private @Nullable List<RapidSymbol> getQualifier(@NotNull RapidReferenceExpression expression, @NotNull String name) {
        RapidExpression qualifier = expression.getQualifier();
        if (qualifier == null) {
            return null;
        }
        RapidType dataType = qualifier.getType();
        if (dataType == null) {
            return null;
        }
        RapidStructure structure = dataType.getRootStructure();
        if (!(structure instanceof RapidRecord record)) {
            return null;
        }
        return record.getComponents().stream()
                     .filter(component -> name.equalsIgnoreCase(component.getName()))
                     .collect(Collectors.toUnmodifiableList());
    }

    private @Nullable RapidSymbol getRemoteVirtualSymbol(@NotNull RapidRobot robot, @NotNull String[] sections) {
        try {
            String name = sections[1];
            VirtualSymbol result = robot.getSymbol(name);
            if (result == null) {
                return null;
            }
            if (sections.length == 2) {
                return result;
            }
            List<RapidSymbol> children = getChildSymbol(result, sections[2]);
            return children.isEmpty() ? null : children.get(0);
        } catch (IOException e) {
            return null;
        } catch (InterruptedException e) {
            throw new ProcessCanceledException();
        }
    }

    private @Nullable RapidSymbol getRemotePhysicalSymbol(@NotNull RapidRobot robot, @NotNull String[] sections) {
        RapidTask task = getSymbol(robot.getTasks(), sections[1]);
        if (task == null || sections.length == 2) {
            return task;
        }
        RapidModule module = getSymbol(task.getModules(project), sections[2]);
        if (module == null || sections.length == 3) {
            return module;
        }
        RapidSymbol symbol = getSymbol(module.getSymbols(), sections[3]);
        if (symbol == null || sections.length == 4) {
            return symbol;
        }
        List<RapidSymbol> symbols = getChildSymbol(symbol, sections[5]);
        return symbols.isEmpty() ? null : symbols.get(0);
    }

    private <T extends RapidSymbol> @Nullable T getSymbol(@NotNull Collection<T> symbols, @NotNull String name) {
        for (T symbol : symbols) {
            if (name.equalsIgnoreCase(symbol.getName())) {
                return symbol;
            }
        }
        return null;
    }
}
