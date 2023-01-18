package com.bossymr.rapid.ide.completion;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.bossymr.rapid.language.symbol.resolve.ResolveUtil;
import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.TailTypeDecorator;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class RapidSymbolCompletionContributor extends CompletionContributor {

    public static PsiElementPattern<?, ?> REFERENCE_EXPRESSION = psiElement()
            .withParent(RapidReferenceExpression.class);

    public static PsiElementPattern<?, ?> TYPE = REFERENCE_EXPRESSION
            .withSuperParent(2, RapidTypeElement.class);

    public static PsiElementPattern<?, ?> PROCEDURE = REFERENCE_EXPRESSION
            .withSuperParent(2, RapidProcedureCallStatement.class);

    public static PsiElementPattern<?, ?> TRAP = REFERENCE_EXPRESSION
            .withSuperParent(2, RapidConnectStatement.class)
            .afterLeaf("WITH");

    public static PsiElementPattern<?, ?> FIELD = REFERENCE_EXPRESSION
            .andNot(TRAP);

    public RapidSymbolCompletionContributor() {
        extend(CompletionType.BASIC, REFERENCE_EXPRESSION, new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                List<RapidSymbol> symbols = getSymbols(parameters);
                if (PROCEDURE.accepts(parameters.getPosition(), context)) {
                    result.caseInsensitive().addAllElements(getLookupElements(symbols, (symbol) -> symbol instanceof RapidRoutine routine && routine.getAttribute().equals(RapidRoutine.Attribute.PROCEDURE)));
                }
                if (FIELD.accepts(parameters.getPosition(), context)) {
                    result.caseInsensitive().addAllElements(getLookupElements(symbols, (symbol) -> symbol instanceof RapidVariable));
                    result.caseInsensitive().addAllElements(getLookupElements(symbols, (symbol) -> symbol instanceof RapidRoutine routine && routine.getAttribute().equals(RapidRoutine.Attribute.FUNCTION)));
                }
                if (TRAP.accepts(parameters.getPosition(), context)) {
                    result.caseInsensitive().addAllElements(getLookupElements(symbols, (symbol) -> symbol instanceof RapidRoutine routine && routine.getAttribute().equals(RapidRoutine.Attribute.TRAP)));
                }
                if (TYPE.accepts(parameters.getPosition(), context)) {
                    result.caseInsensitive().addAllElements(getLookupElements(symbols, (symbol) -> symbol instanceof RapidStructure));
                }
            }
        });
    }

    private @NotNull List<LookupElement> getLookupElements(@NotNull List<RapidSymbol> symbols, @NotNull Predicate<RapidSymbol> predicate) {
        return symbols.stream().filter(predicate)
                .map(this::createLookupElement)
                .toList();
    }

    private @NotNull LookupElement createLookupElement(@NotNull RapidSymbol symbol) {
        String name = symbol.getName() != null ? symbol.getName() : "";
        LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(symbol, name)
                .withIcon(symbol.getIcon());
        if (symbol instanceof RapidRoutine routine) {
            List<? extends RapidParameterGroup> parameters = routine.getParameters();
            String tailText = FormatUtil.format(routine, EnumSet.of(FormatUtil.Option.SHOW_PARAMETERS), EnumSet.of(FormatUtil.Option.SHOW_TYPE, FormatUtil.Option.SHOW_NAME));
            return switch (routine.getAttribute()) {
                case FUNCTION -> lookupElementBuilder
                        .withInsertHandler(ParenthesesInsertHandler.getInstance(parameters != null && parameters.size() > 0))
                        .withTailText(tailText, true)
                        .withTypeText(routine.getType() != null ? routine.getType().getPresentableText() : "", true);
                case PROCEDURE ->
                        TailTypeDecorator.withTail(lookupElementBuilder.withTailText(tailText, true), TailType.SPACE);
                case TRAP -> TailTypeDecorator.withTail(lookupElementBuilder, TailType.NONE);
            };
        }
        if (symbol instanceof RapidVariable variable) {
            return lookupElementBuilder
                    .withTypeText(variable.getType() != null ? variable.getType().getPresentableText() : "", true);
        }
        return lookupElementBuilder;
    }

    private @NotNull List<RapidSymbol> getSymbols(@NotNull CompletionParameters parameters) {
        PsiElement parent = parameters.getPosition().getParent();
        if (!(parent instanceof RapidReferenceExpression referenceExpression)) {
            throw new IllegalStateException();
        }
        List<RapidSymbol> symbols = ResolveUtil.getSymbols(referenceExpression);
        symbols.removeIf(symbol -> {
            if (symbol instanceof PhysicalSymbol physicalSymbol) {
                return physicalSymbol.getContainingFile().equals(parameters.getOriginalFile());
            }
            return false;
        });
        return symbols;
    }
}
