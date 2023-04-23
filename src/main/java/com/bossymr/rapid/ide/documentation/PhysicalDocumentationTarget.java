package com.bossymr.rapid.ide.documentation;

import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.bossymr.rapid.language.symbol.resolve.ResolveService;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.platform.backend.documentation.DocumentationResult;
import com.intellij.platform.backend.documentation.LinkResolveResult;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PhysicalDocumentationTarget extends RapidDocumentationTarget<PhysicalSymbol> {

    public PhysicalDocumentationTarget(@NotNull PhysicalSymbol symbol) {
        super(symbol);
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

    @Override
    public @Nullable LinkResolveResult resolveLink(@NotNull String name) {
        List<RapidSymbol> symbols = ResolveService.getInstance(getSymbol().getProject()).findSymbols(getSymbol(), name);
        if (symbols.isEmpty()) {
            return null;
        }
        return LinkResolveResult.resolvedTarget(symbols.get(0).getDocumentationTarget());
    }
}
