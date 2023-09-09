package com.bossymr.rapid.language.symbol.resolve;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.data.hardcode.HardcodedContract;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.psi.RapidReferenceExpression;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service(Service.Level.PROJECT)
public final class RapidResolveService {

    private final @NotNull Project project;

    public RapidResolveService(@NotNull Project project) {
        this.project = project;
    }

    public static @NotNull RapidResolveService getInstance(@NotNull Project project) {
        return project.getService(RapidResolveService.class);
    }

    public static @Nullable RapidSymbol findChild(@NotNull RapidSymbol symbol, @NotNull String name) {
        if (symbol instanceof RapidRoutine routine) {
            List<? extends RapidParameterGroup> parameters = routine.getParameters();
            if (parameters == null) {
                return null;
            }
            for (RapidParameterGroup group : parameters) {
                for (RapidParameter parameter : group.getParameters()) {
                    if (name.equalsIgnoreCase(parameter.getName())) {
                        return parameter;
                    }
                }
            }
        }
        if (symbol instanceof RapidRecord record) {
            List<? extends RapidComponent> components = record.getComponents();
            for (RapidComponent component : components) {
                if (name.equalsIgnoreCase(component.getName())) {
                    return component;
                }
            }
        }
        return null;
    }

    public @NotNull List<RapidSymbol> findSymbols(@NotNull RapidReferenceExpression expression) {
        PsiElement identifier = expression.getIdentifier();
        if (identifier == null) {
            return List.of();
        }
        String name = expression.getText();
        List<RapidSymbol> symbols = findQualifier(expression, name);
        if (symbols != null) {
            return symbols;
        }
        return findSymbols(expression, name);
    }

    private @Nullable List<RapidSymbol> findQualifier(@NotNull RapidReferenceExpression expression, @NotNull String name) {
        RapidExpression qualifier = expression.getQualifier();
        if (qualifier == null) {
            return null;
        }
        RapidType dataType = qualifier.getType();
        if (dataType == null) {
            return null;
        }
        RapidStructure structure = dataType.getActualStructure();
        if (!(structure instanceof RapidRecord record)) {
            return null;
        }
        List<RapidSymbol> symbols = new ArrayList<>();
        for (RapidComponent component : record.getComponents()) {
            if (name.equalsIgnoreCase(component.getName())) {
                symbols.add(component);
            }
        }
        return List.copyOf(symbols);
    }

    public @NotNull List<RapidSymbol> findSymbols(@NotNull PsiElement context) {
        ResolveScopeProcessor processor = new ResolveScopeProcessor(context, null);
        ResolveScopeVisitor visitor = new ResolveScopeVisitor(context, processor);
        visitor.process();
        return processor.getSymbols();
    }

    /**
     * Attempts to find a symbol with the specified name, which can be accessed from the specified context.
     *
     * @param context the context.
     * @param name the name.
     * @return the symbols.
     */
    public @NotNull List<RapidSymbol> findSymbols(@NotNull PsiElement context, @NotNull String name) {
        ResolveScopeProcessor processor = new ResolveScopeProcessor(context, name);
        ResolveScopeVisitor visitor = new ResolveScopeVisitor(context, processor);
        visitor.process();
        return processor.getSymbols();
    }

    public @NotNull List<RapidSymbol> findSymbols(@NotNull PsiElement context, @Nullable String moduleName, @NotNull String name) {
        if (moduleName == null) {
            return findSymbols(context, name);
        }
        if (moduleName.isBlank()) {
            RapidSymbol symbol = findSymbol("RAPID/" + name);
            return symbol != null ? List.of(symbol) : List.of();
        }
        RapidModule module = findModule(moduleName);
        if (module == null) {
            return List.of();
        }
        return module.getSymbols().stream()
                .filter(symbol -> name.equalsIgnoreCase(symbol.getName()))
                .map(symbol -> (RapidSymbol) symbol)
                .toList();
    }

    public @Nullable RapidSymbol findCustomSymbol(@NotNull String @NotNull [] sections) {
        try {
            HardcodedContract contract = HardcodedContract.valueOf(sections[1].toUpperCase());
            RapidRoutine element = contract.getFunction().getBlock().getElement();
            if (sections.length == 3) {
                return findChild(element, sections[2]);
            }
            return element;
        } catch (IllegalArgumentException ignored) {}
        return switch (sections[1]) {
            case "num" -> RapidPrimitiveType.NUMBER.getStructure();
            case "dnum" -> RapidPrimitiveType.DOUBLE.getStructure();
            case "string" -> RapidPrimitiveType.STRING.getStructure();
            case "bool" -> RapidPrimitiveType.BOOLEAN.getStructure();
            case "pose" -> RapidPrimitiveType.POSE.getStructure();
            case "orient" -> RapidPrimitiveType.ORIENTATION.getStructure();
            case "pos" -> RapidPrimitiveType.POSITION.getStructure();
            default -> null;
        };
    }

    private @Nullable RapidModule findModule(@NotNull String moduleName) {
        Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(RapidFileType.getInstance(), GlobalSearchScope.allScope(project));
        PsiManager manager = PsiManager.getInstance(project);
        return virtualFiles.stream()
                .map(manager::findFile)
                .filter(file -> file instanceof RapidFile)
                .map(file -> (RapidFile) file)
                .flatMap(file -> file.getModules().stream())
                .filter(module -> moduleName.equalsIgnoreCase(module.getName()))
                .findFirst().orElse(null);
    }

    /**
     * Attempts to resolve the symbol with the specified {@link RapidSymbol#getCanonicalName() canonical name} on a
     * connected robot.
     *
     * @param canonicalName the canonical name of the symbol.
     * @return the symbol, or {@code null} if a symbol with the specified canonical name was not found, or if a robot is
     * not currently connected.
     */
    public @Nullable RapidSymbol findSymbol(@NotNull String canonicalName) {
        RobotService service = RobotService.getInstance();
        RapidRobot robot = service.getRobot();
        if (robot == null) {
            return null;
        }
        String[] sections = canonicalName.split("/");
        if (sections.length <= 1) {
            throw new IllegalArgumentException("Malformed name: " + canonicalName);
        }
        RapidSymbol customSymbol = findCustomSymbol(sections);
        if (customSymbol != null) {
            return customSymbol;
        }
        RapidSymbol symbol = findSymbol(robot, sections);
        if (symbol != null) {
            return symbol;
        }
        try {
            VirtualSymbol result = robot.getSymbol(sections[1]);
            if (result == null) {
                return null;
            }
            if (sections.length == 2) {
                return result;
            }
            return findChild(result, sections[2]);
        } catch (IOException e) {
            return null;
        } catch (InterruptedException e) {
            throw new ProcessCanceledException();
        }
    }

    private @Nullable RapidSymbol findSymbol(@NotNull RapidRobot robot, @NotNull String[] sections) {
        Optional<RapidTask> task = robot.getTasks().stream()
                .filter(element -> element.getName().equalsIgnoreCase(sections[1]))
                .findFirst();
        if (task.isEmpty()) {
            return null;
        }
        if (sections.length == 2) {
            return task.orElseThrow();
        }
        Optional<? extends RapidModule> module = task.orElseThrow().getModules(project).stream()
                .filter(element -> sections[2].equalsIgnoreCase(element.getName()))
                .findFirst();
        if (module.isEmpty()) {
            return null;
        }
        if (sections.length == 3) {
            return module.orElseThrow();
        }
        Optional<? extends RapidSymbol> symbol = module.orElseThrow().getSymbols().stream()
                .filter(element -> sections[3].equalsIgnoreCase(element.getName()))
                .findFirst();
        if (symbol.isEmpty()) {
            return null;
        }
        if (sections.length == 4) {
            return symbol.orElseThrow();
        }
        return findChild(symbol.orElseThrow(), sections[5]);
    }
}
