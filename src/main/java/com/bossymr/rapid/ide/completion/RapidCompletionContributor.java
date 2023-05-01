package com.bossymr.rapid.ide.completion;

import com.bossymr.rapid.language.RapidLanguage;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.stubs.index.*;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.bossymr.rapid.language.symbol.resolve.RapidResolveService;
import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.TailTypeDecorator;
import com.intellij.navigation.TargetPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class RapidCompletionContributor extends CompletionContributor {

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement element = parameters.getPosition();
        if (!(element.getLanguage().isKindOf(RapidLanguage.getInstance()))) {
            return;
        }
        Project project = element.getProject();
        RapidResolveService service = RapidResolveService.getInstance(project);
        PsiElement parent = element.getParent();
        if (parent == null) {
            return;
        }
        List<SymbolFilter> filters = getIndexPredicate(parent);
        if (filters.isEmpty()) {
            return;
        }
        List<RapidSymbol> variants = new ArrayList<>();
        service.findSymbols(element).stream()
                .filter(symbol -> filters.stream().anyMatch(filter -> filter.matches(symbol)))
                .forEach(variants::add);
        for (SymbolFilter filter : filters) {
            for (StringStubIndexExtension<? extends PhysicalSymbol> index : filter.getIndexes()) {
                for (String key : index.getAllKeys(project)) {
                    variants.addAll(index.get(key, project, GlobalSearchScope.projectScope(project)));
                }
            }
        }
        variants.stream()
                .filter(symbol -> symbol.getName() != null)
                .map(this::createLookupElement)
                .forEach(lookupElement -> result.caseInsensitive().addElement(lookupElement));
    }

    @SuppressWarnings("UnstableApiUsage")
    private @NotNull LookupElement createLookupElement(@NotNull RapidSymbol symbol) {
        String name = symbol.getPresentableName();
        TargetPresentation presentation = symbol.getTargetPresentation();
        LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(symbol, name)
                .withIcon(presentation.getIcon())
                .withPresentableText(presentation.getPresentableText())
                .withTailText(presentation.getLocationText());
        if (symbol instanceof RapidRoutine routine) {
            List<? extends RapidParameterGroup> parameters = routine.getParameters();
            String tailText = FormatUtil.format(routine, EnumSet.of(FormatUtil.Option.SHOW_PARAMETERS), EnumSet.of(FormatUtil.Option.SHOW_TYPE, FormatUtil.Option.SHOW_NAME));
            return switch (routine.getRoutineType()) {
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


    private @NotNull List<SymbolFilter> getIndexPredicate(@NotNull PsiElement element) {
        if (!(element instanceof RapidReferenceExpression referenceExpression)) {
            return List.of();
        }
        PsiElement parent = referenceExpression.getParent();
        if (parent instanceof RapidTypeElement typeElement) {
            if (typeElement.getParent() instanceof RapidAlias) {
                // Alias cannot be defined on another alias type.
                return List.of(SymbolFilter.ALIAS_TYPE);
            }
            return List.of(SymbolFilter.TYPE);
        }
        if (parent instanceof RapidProcedureCallStatement) {
            return List.of(SymbolFilter.PROCEDURE, SymbolFilter.FUNCTION, SymbolFilter.VARIABLE);
        }
        if (parent instanceof RapidConnectStatement) {
            PsiElement leaf = PsiTreeUtil.prevLeaf(referenceExpression);
            if (leaf != null && leaf.getText().equalsIgnoreCase("WITH")) {
                return List.of(SymbolFilter.TRAP);
            }
        }
        return List.of(SymbolFilter.FUNCTION, SymbolFilter.VARIABLE);
    }

    public enum SymbolFilter {
        TYPE(symbol -> symbol instanceof RapidStructure, RapidAliasIndex.getInstance(), RapidRecordIndex.getInstance()),
        ALIAS_TYPE(symbol -> symbol instanceof RapidStructure && !(symbol instanceof RapidAlias), RapidRecordIndex.getInstance()),
        PROCEDURE(symbol -> symbol instanceof RapidRoutine routine && routine.getRoutineType() == RoutineType.PROCEDURE, RapidProcedureIndex.getInstance()),
        FUNCTION(symbol -> symbol instanceof RapidRoutine routine && routine.getRoutineType() == RoutineType.FUNCTION, RapidFunctionIndex.getInstance()),
        TRAP(symbol -> symbol instanceof RapidRoutine routine && routine.getRoutineType() == RoutineType.TRAP, RapidTrapIndex.getInstance()),
        VARIABLE(symbol -> symbol instanceof RapidVariable, RapidFieldIndex.getInstance());


        private final @NotNull Predicate<RapidSymbol> predicate;
        private final @NotNull StringStubIndexExtension<? extends PhysicalSymbol>[] indexes;

        @SafeVarargs
        SymbolFilter(@NotNull Predicate<RapidSymbol> predicate, @NotNull StringStubIndexExtension<? extends PhysicalSymbol>... indexes) {
            this.predicate = predicate;
            this.indexes = indexes;
        }

        public boolean matches(@NotNull RapidSymbol symbol) {
            return predicate.test(symbol);
        }

        @Contract(pure = true)
        public @Unmodifiable List<StringStubIndexExtension<? extends PhysicalSymbol>> getIndexes() {
            return List.of(indexes);
        }
    }
}
