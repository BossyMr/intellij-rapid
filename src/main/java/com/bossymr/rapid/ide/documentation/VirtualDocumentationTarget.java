package com.bossymr.rapid.ide.documentation;

import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.intellij.openapi.project.Project;
import com.intellij.platform.backend.documentation.DocumentationResult;
import org.jetbrains.annotations.NotNull;


public class VirtualDocumentationTarget extends RapidDocumentationTarget<VirtualSymbol> {

    public VirtualDocumentationTarget(@NotNull Project project, @NotNull VirtualSymbol symbol) {
        super(project, symbol);
    }

    @Override
    public @NotNull DocumentationResult computeDocumentation() {
        return DocumentationResult.documentation(getSignature());
    }
}
