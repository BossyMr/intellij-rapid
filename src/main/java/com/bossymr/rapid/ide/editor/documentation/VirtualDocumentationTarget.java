package com.bossymr.rapid.ide.editor.documentation;

import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.intellij.openapi.project.Project;
import com.intellij.platform.backend.documentation.DocumentationContent;
import com.intellij.platform.backend.documentation.DocumentationResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class VirtualDocumentationTarget extends RapidDocumentationTarget<VirtualSymbol> {

    public VirtualDocumentationTarget(@NotNull Project project, @NotNull VirtualSymbol symbol, @Nullable String anchor) {
        super(project, symbol, anchor);
    }

    @Override
    public @NotNull DocumentationResult computeDocumentation() {
        RapidDocumentationService service = RapidDocumentationService.getInstance();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getSignature());
        String documentation = service.getDocumentation(getProject(), getName());
        if (documentation != null) {
            stringBuilder.append(documentation);
        }
        DocumentationContent content = DocumentationContent.content(stringBuilder.toString(), service.getImages());
        return DocumentationResult.documentation(content)
                                  .anchor(getAnchor());
    }
}
