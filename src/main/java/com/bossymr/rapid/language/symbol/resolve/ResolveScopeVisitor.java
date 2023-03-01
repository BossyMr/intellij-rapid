package com.bossymr.rapid.language.symbol.resolve;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.physical.PhysicalParameterGroup;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.RemoteRobotService;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class ResolveScopeVisitor extends RapidElementVisitor {

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
        if (symbol != null) {
            halt = halt || (!(processor.process(symbol)) && processor.getName() != null);
        }
    }

    @Override
    public void visitFile(@NotNull PsiFile psiFile) {
        if (psiFile instanceof RapidFile file) {
            for (PhysicalModule module : file.getModules()) {
                if (previous.equals(module)) continue;
                process(module);
                for (RapidAccessibleSymbol symbol : module.getSymbols()) {
                    process(symbol);
                }
            }
        }
        super.visitFile(psiFile);
    }

    @Override
    public void visitRoutine(@NotNull PhysicalRoutine routine) {
        if (previous instanceof RapidStatementList) {
            List<PhysicalParameterGroup> groups = routine.getParameters();
            if (groups != null) {
                for (RapidParameterGroup group : groups) {
                    for (RapidParameter parameter : group.getParameters()) {
                        process(parameter);
                    }
                }
            }
            Collection<RapidLabelStatement> labelStatements = PsiTreeUtil.findChildrenOfType(routine, RapidLabelStatement.class);
            for (RapidLabelStatement labelStatement : labelStatements) {
                process(labelStatement);
            }
            for (RapidField field : routine.getFields()) {
                process(field);
            }
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
        for (RapidAccessibleSymbol symbol : module.getSymbols()) {
            process(symbol);
        }
        super.visitModule(module);
    }

    @Override
    public void visitForStatement(@NotNull RapidForStatement statement) {
        if (previous instanceof RapidStatementList) {
            process(statement.getVariable());
        }
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
        RemoteRobotService service = RemoteRobotService.getInstance();
        RapidRobot robot = service.getRobot();
        if (robot != null) {
            for (RapidTask task : robot.getTasks()) {
                for (PhysicalModule module : task.getModules(context.getProject())) {
                    if (!(module.equals(physicalModule))) {
                        if (module.hasAttribute(RapidModule.Attribute.SYSTEM_MODULE) || isRemoteFile) {
                            visitModule(module);
                        }
                    }
                }
            }
        }
        if (!(isRemoteFile)) {
            PsiManager manager = PsiManager.getInstance(context.getProject());
            for (VirtualFile virtualFile : FileTypeIndex.getFiles(RapidFileType.getInstance(), GlobalSearchScope.projectScope(context.getProject()))) {
                PsiFile file = manager.findFile(virtualFile);
                if (file != null) {
                    visitFile(file);
                }
            }
        }
    }

    private boolean isRemoteFile(@NotNull PsiElement element) {
        PsiFile containingFile = element.getContainingFile();
        VirtualFile virtualFile = containingFile.getVirtualFile();
        RemoteRobotService service = RemoteRobotService.getInstance();
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

    private void visitRobot() {
        RemoteRobotService service = RemoteRobotService.getInstance();
        RapidRobot robot = service.getRobot();
        if (robot != null) {
            if (processor.getName() != null) {
                process(robot.getSymbol(processor.getName()));
            } else {
                for (VirtualSymbol symbol : robot.getSymbols()) {
                    process(symbol);
                }
            }
        }
    }
}
