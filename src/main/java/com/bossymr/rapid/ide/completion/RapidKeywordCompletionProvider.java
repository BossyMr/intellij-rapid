package com.bossymr.rapid.ide.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class RapidKeywordCompletionProvider extends CompletionProvider<CompletionParameters> {

    private final String[] keywords;

    public RapidKeywordCompletionProvider(@NotNull String... keywords) {
        this.keywords = keywords;
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        for (String keyword : keywords) {
            LookupElement lookupElement = LookupElementBuilder.create(keyword).bold();
            result.caseInsensitive().addElement(lookupElement);
        }
    }
}
