package com.bossymr.rapid.robot.network;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.xdebugger.XSourcePosition;
import org.jetbrains.annotations.NotNull;

public record LineTextRange(@NotNull String module, int startRow, int endRow, int startColumn, int endColumn) {

    public boolean equals(@NotNull XSourcePosition sourcePosition) {
        return ReadAction.compute(() -> {
            Document document = FileDocumentManager.getInstance().getDocument(sourcePosition.getFile());
            if (document == null) return false;
            String moduleName = sourcePosition.getFile().getNameWithoutExtension();
            int lineNumber = document.getLineNumber(sourcePosition.getOffset());
            return moduleName.equals(module()) && lineNumber >= startRow && lineNumber <= endRow;
        });
    }

}
