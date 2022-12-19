package com.bossymr.rapid.language.symbol.resolve;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.robot.RemoteService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class RapidScopeVisitor extends RapidElementVisitor {

    private final RapidScopeProcessor processor;

    private PsiElement previous;
    private State state;

    public RapidScopeVisitor(@NotNull RapidScopeProcessor processor) {
        this.processor = processor;
        this.state = State.CONTINUE;
    }

    private void process(@Nullable RapidSymbol symbol) {
        if (symbol != null) {
            if (state != State.HALT) {
                if (!(processor.process(symbol))) {
                    state = State.HALT;
                }
            } else {
                processor.process(symbol);
            }
        }
    }

    @Override
    public void visitFile(@NotNull PsiFile file) {
        if (file instanceof RapidFile rapidFile) {
            for (PhysicalModule module : rapidFile.getModules()) {
                if (previous.equals(module)) continue;
                process(module);
                for (RapidAccessibleSymbol symbol : module.getSymbols()) {
                    process(symbol);
                }
            }
        }
        super.visitFile(file);
    }

    @Override
    public void visitArgument(@NotNull RapidArgument argument) {
        RapidRoutine routine = getRoutine(argument);
        if (routine != null) {
            List<RapidParameterGroup> parameters = routine.getParameters();
            if (parameters != null) {
                for (RapidParameterGroup group : parameters) {
                    for (RapidParameter parameter : group.getParameters()) {
                        process(parameter);
                    }
                }
            }
        }
        super.visitArgument(argument);
    }

    private @Nullable RapidRoutine getRoutine(@NotNull RapidArgument argument) {
        RapidReferenceExpression reference = getReference(argument);
        if (reference == null) return null;
        RapidSymbol symbol = reference.resolve();
        return symbol instanceof RapidRoutine routine ? routine : null;
    }

    private @Nullable RapidReferenceExpression getReference(@NotNull RapidArgument argument) {
        PsiElement element = PsiTreeUtil.getParentOfType(argument, RapidFunctionCallExpression.class, RapidProcedureCallStatement.class);
        if (element instanceof RapidProcedureCallStatement statement) {
            RapidExpression expression = statement.getReferenceExpression();
            if (!(expression instanceof RapidReferenceExpression reference)) return null;
            return reference;
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
    public void visitStatementList(@NotNull RapidStatementList statementList) {
        PsiElement context = statementList.getParent();
        if (context instanceof RapidForStatement forStatement) {
            process(forStatement.getVariable());
        }
        if (context instanceof RapidRoutine routine) {
            List<RapidParameterGroup> groups = routine.getParameters();
            if (groups != null) {
                for (RapidParameterGroup group : groups) {
                    for (RapidParameter parameter : group.getParameters()) {
                        process(parameter);
                    }
                }
            }
            if (routine instanceof PhysicalRoutine physicalRoutine) {
                Collection<RapidLabelStatement> labels = PsiTreeUtil.findChildrenOfType(physicalRoutine, RapidLabelStatement.class);
                for (RapidLabelStatement label : labels) {
                    process(label);
                }
            }
            for (RapidField field : routine.getFields()) {
                process(field);
            }
        }
        super.visitStatementList(statementList);
    }

    @Override
    public void visitElement(@NotNull PsiElement element) {
        this.previous = element;
        if (state == State.CONTINUE) {
            PsiElement parent = element.getParent();
            if (parent != null) {
                parent.accept(this);
            } else {
                state = State.BREAK;
                Project project = previous.getProject();
                FileTypeIndex.processFiles(RapidFileType.INSTANCE, (virtualFile) -> {
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                    if (psiFile != null) {
                        visitFile(psiFile);
                    }
                    return true;
                }, GlobalSearchScope.projectScope(project));

                if (this.state != State.HALT) {
                    RemoteService service = RemoteService.getInstance();
                    if (service.getRobot() != null) {
                        try {
                            process(service.getRobot().getSymbol(processor.getName()));
                        } catch (IOException | InterruptedException e) {
                            return;
                        }
                    }
                }
            }
        }
        super.visitElement(element);
    }

    enum State {
        HALT, CONTINUE, BREAK
    }
}
