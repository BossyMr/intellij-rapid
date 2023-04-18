package com.bossymr.rapid.ide.debugger.breakpoints;

import com.bossymr.rapid.language.psi.RapidFile;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.DocumentUtil;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.intellij.xdebugger.breakpoints.ui.XBreakpointCustomPropertiesPanel;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public abstract class AbstractRapidLineBreakpointType<T extends RapidBreakpointProperties<T>> extends XLineBreakpointType<T> {

    protected AbstractRapidLineBreakpointType(@NonNls @NotNull String id, @Nls @NotNull String title) {
        super(id, title);
    }

    @Override
    public boolean isSuspendThreadSupported() {
        return false;
    }

    @Override
    public @Nullable XDebuggerEditorsProvider getEditorsProvider(@NotNull XLineBreakpoint<T> breakpoint, @NotNull Project project) {
        return super.getEditorsProvider(breakpoint, project);
    }

    @Override
    public @Nullable XBreakpointCustomPropertiesPanel<XLineBreakpoint<T>> createCustomRightPropertiesPanel(@NotNull Project project) {
        return super.createCustomRightPropertiesPanel(project);
    }

    @Override
    public List<? extends AnAction> getAdditionalPopupMenuActions(@NotNull XLineBreakpoint<T> breakpoint, @Nullable XDebugSession currentSession) {
        return super.getAdditionalPopupMenuActions(breakpoint, currentSession);
    }

    protected boolean canPutAtElement(@NotNull VirtualFile virtualFile, int line, @NotNull Project project, @NotNull Predicate<PsiElement> predicate) {
        PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
        if (!(file instanceof RapidFile)) {
            return false;
        }
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        if (document == null) return false;
        AtomicBoolean atomicBoolean = new AtomicBoolean();
        XDebuggerUtil.getInstance().iterateLine(project, document, line, element -> {
            if (element instanceof PsiWhiteSpace || element instanceof PsiComment) {
                return true;
            }
            PsiElement parent;
            while ((parent = element.getParent()) != null) {
                int offset = parent.getTextOffset();
                if (DocumentUtil.isValidOffset(offset, document) && document.getLineNumber(offset) == line) {
                    if (predicate.test(parent)) {
                        atomicBoolean.set(true);
                        return false;
                    }
                    element = parent;
                } else {
                    return false;
                }
            }
            return true;
        });
        return atomicBoolean.get();
    }
}
