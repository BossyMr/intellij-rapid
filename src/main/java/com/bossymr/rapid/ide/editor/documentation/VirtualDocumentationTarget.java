package com.bossymr.rapid.ide.editor.documentation;

import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.project.Project;
import com.intellij.platform.backend.documentation.DocumentationContent;
import com.intellij.platform.backend.documentation.DocumentationResult;
import org.jetbrains.annotations.NotNull;


public class VirtualDocumentationTarget extends RapidDocumentationTarget<VirtualSymbol> {

    public VirtualDocumentationTarget(@NotNull Project project, @NotNull VirtualSymbol symbol) {
        super(project, symbol);
    }

    @Override
    public @NotNull DocumentationResult computeDocumentation() {
        RapidDocumentationService service = RapidDocumentationService.getInstance();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getSignature());
        stringBuilder.append(DocumentationMarkup.CONTENT_START);
        String documentation = service.getDocumentation(getProject(), getName());
        if (documentation != null) {
            stringBuilder.append(documentation);
        }
        stringBuilder.append(DocumentationMarkup.CONTENT_START);
        return DocumentationResult.documentation(DocumentationContent.content(stringBuilder.toString(), service.getImages()));
    }
}
