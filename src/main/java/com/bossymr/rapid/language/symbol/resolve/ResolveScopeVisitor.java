package com.bossymr.rapid.language.symbol.resolve;

import com.bossymr.rapid.language.flow.data.HardcodedContract;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.stubs.index.RapidModuleIndex;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.physical.PhysicalParameterGroup;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.language.symbol.virtual.VirtualRoutine;
import com.bossymr.rapid.language.symbol.virtual.VirtualStructure;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ResolveScopeVisitor extends RapidElementVisitor {

    private final @NotNull ResolveScopeProcessor processor;
    private @NotNull PsiElement previous;
    private boolean halt;

    public ResolveScopeVisitor(@NotNull PsiElement element, @NotNull ResolveScopeProcessor processor) {
        this.previous = element;
        this.processor = processor;
    }

    public void process() {
        for (PsiElement element = previous.getParent(); element != null; element = element.getParent()) {
            if (halt) {
                return;
            }
            element.accept(this);
            if (element instanceof PsiFile file) {
                visitProject(file);
                break;
            }
            previous = element;
        }
        if (halt) {
            return;
        }
        visitRobot();
    }

    private void process(@Nullable RapidSymbol symbol) {
        ProgressManager.checkCanceled();
        if (symbol != null) {
            halt = (!(processor.process(symbol)) && processor.getName() != null) || halt;
        }
    }

    @Override
    public void visitFile(@NotNull PsiFile element) {
        if (!(element instanceof RapidFile file)) {
            return;
        }
        for (PhysicalModule module : file.getModules()) {
            if (module.equals(previous)) {
                continue;
            }
            visitModule(module);
        }
    }

    @Override
    public void visitModule(@NotNull PhysicalModule module) {
        process(module);
        for (RapidVisibleSymbol symbol : module.getSymbols()) {
            process(symbol);
        }
    }

    @Override
    public void visitRoutine(@NotNull PhysicalRoutine routine) {
        List<PhysicalParameterGroup> parameterGroups = routine.getParameters();
        if (parameterGroups != null) {
            for (RapidParameterGroup parameterGroup : parameterGroups) {
                for (RapidParameter parameter : parameterGroup.getParameters()) {
                    process(parameter);
                }
            }
        }
        for (RapidLabelStatement label : routine.getLabels()) {
            process(label);
        }
        for (RapidField field : routine.getFields()) {
            process(field);
        }
    }

    @Override
    public void visitForStatement(@NotNull RapidForStatement statement) {
        process(statement.getVariable());
    }

    @Override
    public void visitArgument(@NotNull RapidArgument argument) {
        if (!(previous.equals(argument.getParameter()))) {
            return;
        }
        RapidRoutine routine = getRoutine(argument);
        if (routine != null) {
            List<? extends RapidParameterGroup> parameters = routine.getParameters();
            if (parameters != null) {
                for (RapidParameterGroup group : parameters) {
                    for (RapidParameter parameter : group.getParameters()) {
                        process(parameter);
                    }
                }
            }
        }
    }

    private @Nullable RapidRoutine getRoutine(@NotNull RapidArgument argument) {
        RapidReferenceExpression reference = getReference(argument);
        if (reference == null) {
            return null;
        }
        RapidSymbol symbol = reference.getSymbol();
        if (symbol instanceof RapidRoutine routine) {
            return routine;
        }
        return null;
    }

    private @Nullable RapidReferenceExpression getReference(@NotNull RapidArgument argument) {
        PsiElement element = PsiTreeUtil.getParentOfType(argument, RapidCallExpression.class);
        if (!(element instanceof RapidCallExpression statement)) {
            return null;
        }
        RapidExpression expression = statement.getReferenceExpression();
        return expression instanceof RapidReferenceExpression reference ? reference : null;
    }

    private void visitProject(@NotNull PsiFile file) {
        RapidRobot robot = RobotService.getInstance().getRobot();
        Project project = file.getProject();
        boolean isRemoteFile = isRemoteFile(file);
        if (!(isRemoteFile)) {
            GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
            for (PhysicalModule module : RapidModuleIndex.getInstance().getAllElements(project, scope)) {
                module.accept(this);
            }
        }
        if (robot != null) {
            for (RapidTask task : robot.getTasks()) {
                for (PhysicalModule module : task.getModules(project)) {
                    if (module.getContainingFile().equals(file)) {
                        continue;
                    }
                    if (module.hasAttribute(ModuleType.SYSTEM_MODULE) || isRemoteFile) {
                        visitModule(module);
                    }
                }
            }
        }
    }

    private boolean isRemoteFile(@NotNull PsiFile element) {
        String filePath = element.getViewProvider().getVirtualFile().getPath();
        File file = new File(filePath);
        RobotService service = RobotService.getInstance();
        RapidRobot robot = service.getRobot();
        if (robot != null) {
            for (RapidTask task : robot.getTasks()) {
                Set<File> files = task.getFiles();
                if (files.contains(file)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void visitCustomVariable() {
        if (processor.getName() != null) {
            ResolveService service = ResolveService.getInstance(previous.getProject());
            process(service.getRemoteSymbol("RAPID" + "/" + processor.getName()));
        }
    }

    private void visitRobot() {
        visitCustomVariable();
        RobotService service = RobotService.getInstance();
        RapidRobot robot = service.getRobot();
        if (robot != null) {
            if (processor.getName() != null) {
                try {
                    process(robot.getSymbol(processor.getName()));
                } catch (IOException ignored) {
                } catch (InterruptedException e) {
                    throw new ProcessCanceledException();
                }
            } else {
                for (VirtualSymbol symbol : robot.getSymbols()) {
                    process(symbol);
                }
            }
        } else {
            for (RapidPrimitiveType value : RapidPrimitiveType.values()) {
                VirtualStructure structure = value.getStructure();
                process(structure);
            }
            for (HardcodedContract value : HardcodedContract.values()) {
                VirtualRoutine routine = value.getRoutine();
                process(routine);
            }
        }
    }
}
