package com.bossymr.rapid.ide.completion;

import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.TailTypeDecorator;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidKeywordCompletionProvider extends CompletionProvider<CompletionParameters> {

    private final String[] keywords;
    private final TailType tailType;
    private final InsertHandler<LookupElement> insertHandler;

    public RapidKeywordCompletionProvider(@NotNull String... keywords) {
        this(TailType.SPACE, keywords);
    }

    public RapidKeywordCompletionProvider(@NotNull TailType tailType, @NotNull String... keywords) {
        this(tailType, null, keywords);
    }

    public RapidKeywordCompletionProvider(@NotNull TailType tailType, @Nullable InsertHandler<LookupElement> insertHandler, @NotNull String... keywords) {
        this.keywords = keywords;
        this.tailType = tailType;
        this.insertHandler = insertHandler;
    }

    public static @NotNull LookupElement getLookupElement(@NotNull TailType tailType, @NotNull String keyword, @Nullable InsertHandler<LookupElement> insertHandler) {
        LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(keyword).bold();
        if (insertHandler != null) {
            lookupElementBuilder = lookupElementBuilder.withInsertHandler(insertHandler);
        }
        return TailTypeDecorator.withTail(lookupElementBuilder, tailType);
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        for (String keyword : keywords) {
            result.caseInsensitive().addElement(getLookupElement(tailType, keyword, insertHandler));
        }
    }
}
