package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class RapidResolveUtil {

    private RapidResolveUtil() {}

    public static ResolveResult @NotNull [] resolve(@NotNull String name, @NotNull Project project) {
        return resolve(name, project, null);
    }

    public static ResolveResult @NotNull [] resolve(@NotNull String name, @NotNull PsiElement context) {
        return resolve(name, context.getProject(), context);
    }

    public static ResolveResult @NotNull [] resolve(@NotNull String name, @NotNull Project project, @Nullable PsiElement context) {
        RapidSymbolBundle bundle = new RapidSymbolBundle(name, context);
        // Check current file
        if (context != null) {
            for (PsiElement level = context.getParent(); level != null; level = level.getParent()) {
                switch (bundle.collect(level)) {
                    case CONTINUE:
                        break;
                    case HALT:
                        return bundle.getResults();
                }
            }
        }
        if (bundle.getResults().length > 0) {
            return bundle.getResults();
        }
        // Check other files
        FileTypeIndex.processFiles(RapidFileType.INSTANCE,
                file -> {
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                    if (psiFile != null) {
                        bundle.collect(psiFile);
                    }
                    return true;
                }, GlobalSearchScope.allScope(project));
        if (bundle.getResults().length > 0) {
            return bundle.getResults();
        }
        // Check remote robot
        RobotService service = RobotService.getInstance(project);
        Optional<Robot> robot = service.getRobot();
        if (robot.isPresent()) {
            Optional<RapidSymbol> symbol = robot.get().getSymbol(name);
            if (symbol.isPresent()) {
                return new ResolveResult[]{new PsiElementResolveResult(symbol.get())};
            }
        }
        return ResolveResult.EMPTY_ARRAY;
    }


    private static final class RapidSymbolBundle {

        private final String name;
        private final PsiElement context;

        private final List<RapidSymbol> symbols = new ArrayList<>();

        public RapidSymbolBundle(@NotNull String name, @Nullable PsiElement context) {
            this.name = name;
            this.context = context;
        }

        public @NotNull ContinueState collect(@NotNull PsiElement element) {
            if (element instanceof RapidFile file) {
                RapidModule module = PsiTreeUtil.getParentOfType(context, RapidModule.class);
                for (RapidModule fileModule : file.getModules()) {
                    if (fileModule.equals(module)) continue;
                    switch (collect(fileModule)) {
                        case CONTINUE:
                            break;
                        case HALT:
                            return ContinueState.HALT;
                    }
                    List<? extends RapidSymbol> topLevel = Stream.of(fileModule.getFields(), fileModule.getRoutines(), fileModule.getStructures())
                            .flatMap(Collection::stream)
                            .toList();
                    for (RapidSymbol symbol : topLevel) {
                        switch (collect(symbol)) {
                            case CONTINUE:
                                break;
                            case HALT:
                                return ContinueState.HALT;
                        }
                    }
                }
            }
            if (element instanceof RapidStatementList) {
                PsiElement next = element.getParent();
                if (next instanceof RapidForStatement statement) {
                    RapidTargetVariable variable = statement.getVariable();
                    if (variable != null) {
                        switch (collect(variable)) {
                            case CONTINUE:
                                break;
                            case HALT:
                                return ContinueState.HALT;
                        }
                    }
                }
                if (next instanceof RapidRoutine routine) {
                    for (RapidLabelStatement label : PsiTreeUtil.findChildrenOfType(routine, RapidLabelStatement.class)) {
                        collect(label);
                    }
                    if (routine.getParameters() != null) {
                        for (RapidParameterGroup group : routine.getParameters()) {
                            for (RapidParameter parameter : group.getParameters()) {
                                switch (collect(parameter)) {
                                    case CONTINUE:
                                        break;
                                    case HALT:
                                        return ContinueState.HALT;
                                }
                            }
                        }
                    }
                }
            }
            return ContinueState.CONTINUE;
        }

        public @NotNull ContinueState collect(@NotNull RapidSymbol symbol) {
            if (name.equals(symbol.getName())) {
                if (context != null && context.getContainingFile().equals(symbol.getContainingFile())) {
                    symbols.add(symbol);
                    return ContinueState.HALT;
                } else {
                    symbols.add(symbol);
                    return ContinueState.CONTINUE;
                }
            }
            return ContinueState.CONTINUE;
        }

        public @NotNull List<RapidSymbol> getSymbols() {
            return symbols;
        }

        public ResolveResult @NotNull [] getResults() {
            return getSymbols().stream()
                    .map(PsiElementResolveResult::new)
                    .toArray(ResolveResult[]::new);
        }

        enum ContinueState {
            CONTINUE, HALT
        }


    }

}
