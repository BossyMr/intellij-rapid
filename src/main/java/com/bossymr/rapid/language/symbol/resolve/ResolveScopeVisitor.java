package com.bossymr.rapid.language.symbol.resolve;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.stubs.index.RapidModuleIndex;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.physical.PhysicalParameterGroup;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ResolveScopeVisitor extends RapidElementVisitor {

    private static final Logger logger = Logger.getInstance(ResolveScopeVisitor.class);

    private final ResolveScopeProcessor processor;
    private @NotNull PsiElement previous;
    private boolean halt;

    public ResolveScopeVisitor(@NotNull PsiElement element, @NotNull ResolveScopeProcessor processor) {
        this.previous = element;
        this.processor = processor;
    }

    public void process() {
        for (PsiElement element = previous.getParent(); element != null; element = element.getParent()) {
            if (halt) return;
            element.accept(this);
            previous = element;
        }
        visitProject(processor.getContext());
        if (halt) return;
        visitRobot();
    }

    private void process(@Nullable RapidSymbol symbol) {
        ProgressManager.checkCanceled();
        if (symbol != null) {
            halt = (!(processor.process(symbol)) && processor.getName() != null) || halt;
        }
    }

    @Override
    public void visitFile(@NotNull PsiFile psiFile) {
        if (psiFile instanceof RapidFile file) {
            for (PhysicalModule module : file.getModules()) {
                processModule(module);
            }
        }
        super.visitFile(psiFile);
    }

    private void processModule(@NotNull PhysicalModule module) {
        if (previous.equals(module)) return;
        process(module);
        for (RapidVisibleSymbol symbol : module.getSymbols()) {
            process(symbol);
        }
    }

    @Override
    public void visitRoutine(@NotNull PhysicalRoutine routine) {
        List<PhysicalParameterGroup> groups = routine.getParameters();
        if (groups != null) {
            for (RapidParameterGroup group : groups) {
                for (RapidParameter parameter : group.getParameters()) {
                    process(parameter);
                }
            }
        }
        for (RapidLabelStatement labelStatement : routine.getLabels()) {
            process(labelStatement);
        }
        for (RapidField field : routine.getFields()) {
            process(field);
        }
        super.visitRoutine(routine);
    }

    private @Nullable RapidRoutine getRoutine(@NotNull RapidArgument argument) {
        RapidReferenceExpression reference = getReference(argument);
        if (reference == null) return null;
        RapidSymbol symbol = reference.getSymbol();
        return symbol instanceof RapidRoutine routine ? routine : null;
    }

    private @Nullable RapidReferenceExpression getReference(@NotNull RapidArgument argument) {
        PsiElement element = PsiTreeUtil.getParentOfType(argument, RapidFunctionCallExpression.class, RapidProcedureCallStatement.class);
        if (element instanceof RapidProcedureCallStatement statement) {
            RapidExpression expression = statement.getReferenceExpression();
            return expression instanceof RapidReferenceExpression reference ? reference : null;
        } else if (element instanceof RapidFunctionCallExpression expression) {
            return expression.getReferenceExpression();
        } else {
            return null;
        }
    }

    @Override
    public void visitModule(@NotNull PhysicalModule module) {
        process(module);
        for (RapidVisibleSymbol symbol : module.getSymbols()) {
            process(symbol);
        }
        super.visitModule(module);
    }

    @Override
    public void visitForStatement(@NotNull RapidForStatement statement) {
        process(statement.getVariable());
        super.visitForStatement(statement);
    }

    @Override
    public void visitArgument(@NotNull RapidArgument argument) {
        if (previous.equals(argument.getParameter())) {
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
        super.visitArgument(argument);
    }

    private void visitProject(@NotNull PsiElement context) {
        PhysicalModule physicalModule = PsiTreeUtil.getParentOfType(context, PhysicalModule.class);
        boolean isRemoteFile = isRemoteFile(context);
        RobotService service = RobotService.getInstance();
        RapidRobot robot = service.getRobot();
        Project project = context.getProject();
        if (robot != null) {
            for (RapidTask task : robot.getTasks()) {
                for (PhysicalModule module : task.getModules(project)) {
                    if (!(module.equals(physicalModule))) {
                        if (module.hasAttribute(ModuleType.SYSTEM_MODULE) || isRemoteFile) {
                            visitModule(module);
                        }
                    }
                }
            }
        }
        if (!(isRemoteFile)) {
            GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
            for (PhysicalModule module : RapidModuleIndex.getInstance().getAllElements(project, scope)) {
                processModule(module);
            }
        }
    }

    private boolean isRemoteFile(@NotNull PsiElement element) {
        PsiFile containingFile = element.getContainingFile();
        VirtualFile virtualFile = containingFile.getVirtualFile();
        RobotService service = RobotService.getInstance();
        RapidRobot robot = service.getRobot();
        if (robot != null && virtualFile != null) {
            for (RapidTask task : robot.getTasks()) {
                if (task.getFiles().contains(new File(virtualFile.getPath()))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void visitCustomVariable() {
        if (processor.getName() != null) {
            ResolveService service = ResolveService.getInstance(previous.getProject());
            process(service.findCustomSymbol(new String[]{"RAPID", processor.getName()}));
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
                } catch (IOException e) {
                    logger.error(e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                for (VirtualSymbol symbol : robot.getSymbols()) {
                    process(symbol);
                }
            }
        }
    }
}
