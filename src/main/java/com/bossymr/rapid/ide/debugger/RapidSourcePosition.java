package com.bossymr.rapid.ide.debugger;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.xdebugger.XSourcePosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidSourcePosition implements XSourcePosition {

    private final VirtualFile virtualFile;
    private final int line;
    private final int offset;

    public RapidSourcePosition(@NotNull VirtualFile virtualFile, int line, int offset) {
        this.virtualFile = virtualFile;
        this.line = line;
        this.offset = offset;
    }

    public static @Nullable RapidSourcePosition create(@NotNull PsiElement element) {
        VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
        if (virtualFile == null) return null;
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        if (document == null) return null;
        int textOffset = element.getTextOffset();
        int lineNumber = document.getLineNumber(textOffset);
        return new RapidSourcePosition(virtualFile, lineNumber, textOffset);
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public @NotNull VirtualFile getFile() {
        return virtualFile;
    }

    @Override
    public @NotNull Navigatable createNavigatable(@NotNull Project project) {
        return new OpenFileDescriptor(project, getFile(), getOffset());
    }
}
