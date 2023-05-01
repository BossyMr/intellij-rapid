package com.bossymr.rapid.ide.completion;

import com.bossymr.rapid.language.psi.RapidAttributeList;
import com.bossymr.rapid.language.symbol.ModuleType;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

public class RapidAttributeListCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        PsiElement element = parameters.getPosition();
        RapidAttributeList attributeList = PsiTreeUtil.getParentOfType(element, RapidAttributeList.class);
        if (attributeList == null) {
            throw new IllegalStateException();
        }
        Set<ModuleType> attributes = getVariants(attributeList);
        for (ModuleType attribute : attributes) {
            LookupElement lookupElement = LookupElementBuilder.create(attribute.getText()).bold()
                    .withInsertHandler((insertionContext, item) -> {
                        attributeList.setAttribute(attribute, true);
                    });
            result.caseInsensitive().addElement(lookupElement);
        }
    }

    private @NotNull Set<ModuleType> getVariants(@NotNull RapidAttributeList attributeList) {
        Set<ModuleType> attributes = EnumSet.allOf(ModuleType.class);
        for (ModuleType attribute : attributeList.getAttributes()) {
            attributes.remove(attribute);
            ModuleType.MUTUALLY_EXCLUSIVE.get(attribute).forEach(attributes::remove);
        }
        return attributes;
    }
}
