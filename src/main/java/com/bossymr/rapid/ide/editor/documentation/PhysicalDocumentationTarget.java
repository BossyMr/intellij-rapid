package com.bossymr.rapid.ide.editor.documentation;

import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.project.Project;
import com.intellij.platform.backend.documentation.DocumentationResult;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhysicalDocumentationTarget extends RapidDocumentationTarget<PhysicalSymbol> {

    public PhysicalDocumentationTarget(@NotNull Project project, @NotNull PhysicalSymbol symbol) {
        super(project, symbol);
    }

    @Override
    public @NotNull Navigatable getNavigatable() {
        return getSymbol();
    }

    @Override
    public @Nullable DocumentationResult computeDocumentation() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getSignature());
        stringBuilder.append(DocumentationMarkup.CONTENT_START);
        for (PsiElement element = getSymbol().getFirstChild(); element != null; element = element.getNextSibling()) {
            IElementType elementType = element.getNode().getElementType();
            if (TokenSet.WHITE_SPACE.contains(elementType)) continue;
            if (elementType != RapidTokenTypes.COMMENT) break;
            String text = element.getText();
            text = text.strip().substring(1).strip() + " ";
            stringBuilder.append(text);
        }
        stringBuilder.append(DocumentationMarkup.CONTENT_END);
        return DocumentationResult.documentation(stringBuilder.toString());
    }
}
