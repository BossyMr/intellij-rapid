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
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getSignature());
        RapidDocumentationService service = RapidDocumentationService.getInstance();
        RapidDocumentationService.Result documentation = service.getDocumentation(getSymbol());
        DocumentationContent content;
        if (documentation != null) {
            stringBuilder.append(DocumentationMarkup.CONTENT_START);
            stringBuilder.append(documentation.content());
            stringBuilder.append(DocumentationMarkup.CONTENT_END);
            content = DocumentationContent.content(stringBuilder.toString(), documentation.images());
        } else {
            content = DocumentationContent.content(stringBuilder.toString());
        }
        return DocumentationResult.documentation(content);
    }
}
