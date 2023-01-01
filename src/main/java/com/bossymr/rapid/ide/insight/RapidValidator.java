package com.bossymr.rapid.ide.insight;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.insight.quickfix.*;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalField;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.bossymr.rapid.language.symbol.resolve.ResolveUtil;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.ColorUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A validator which validates individual elements.
 * <p>
 * After inquiry, quick fix should implement {@link IntentionAction}, not {@link LocalQuickFix}, as elements which
 * implement it will show an options' menu.
 */
public class RapidValidator {

    private final AnnotationHolder annotationHolder;

    public RapidValidator(@NotNull AnnotationHolder annotationHolder) {
        this.annotationHolder = annotationHolder;
    }

    /**
     * Checks if the specified module is declared in a file which has the same name as the module. If a module is not
     * declared in the correct file, it can be moved (if the file contains multiple modules), or the file or module can
     * be renamed to match.
     *
     * @param module the module.
     */
    public void checkModuleFile(@NotNull PhysicalModule module) {
        String name = module.getName();
        PsiElement nameIdentifier = module.getNameIdentifier();
        if (name == null || nameIdentifier == null) return;
        if (!(module.getContainingFile() instanceof RapidFile containingFile)) return;
        VirtualFile virtualFile = containingFile.getVirtualFile();
        String fileName = virtualFile.getNameWithoutExtension();
        if (!(name.equals(fileName))) {
            /*
             * The annotation is shown in the module declaration, from the start of the "module" keyword to the end of the
             * identifier.
             */
            int startOffset = module.getNode().getStartOffset();
            int endOffset = nameIdentifier.getNode().getStartOffset() + nameIdentifier.getTextLength();
            TextRange textRange = new TextRange(startOffset, endOffset);
            String message = RapidBundle.message("annotation.module.multiple", name);
            AnnotationBuilder annotationBuilder = annotationHolder.newAnnotation(HighlightSeverity.ERROR, message)
                    .range(textRange);
            if (containingFile.getModules().size() > 1) {
                /*
                 * If the module is not alone in its containing file, it can be moved to a separate file with the correct
                 * name.
                 */
                annotationBuilder = annotationBuilder.withFix(new MoveModuleToSeparateFileFix(module));
            }
            boolean existsModuleForFile = containingFile.getModules().stream().anyMatch(otherModule -> !(otherModule.getManager().areElementsEquivalent(otherModule, module)) && fileName.equals(otherModule.getName()));
            if (!(existsModuleForFile)) {
                /*
                 * If no module in the containing file is correct (the same name as the file), the file can be renamed to
                 * the name of the module, or the module can be renamed to the name of the file.
                 *
                 * This cannot be done if another module with the same name as the file name already exists. As that module
                 * would no longer match the name of the file, or multiple modules would exist with the same name.
                 */
                annotationBuilder = annotationBuilder.withFix(new RenameFileFix(name + RapidFileType.DEFAULT_DOT_EXTENSION));
                annotationBuilder = annotationBuilder.withFix(new RenameElementFix(module, fileName));
            }
            annotationBuilder.create();
        }
    }

    public void checkDuplicateSymbol(@NotNull PhysicalSymbol symbol) {
        String name = symbol.getName();
        PsiElement nameIdentifier = symbol.getNameIdentifier();
        if (name == null || nameIdentifier == null) return;
        List<RapidSymbol> symbols = ResolveUtil.getSymbols(symbol, name);
        if (symbols.size() > 1 && symbols.indexOf(symbol) != 0) {
            /*
             * Multiple symbols are declared with the same name as this symbol, in the same context.
             */
            AnnotationBuilder annotationBuilder = annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.declaration.duplicate.symbol", name))
                    .range(nameIdentifier);
            if (symbols.get(0) instanceof PhysicalSymbol otherSymbol) {
                annotationBuilder = annotationBuilder.withFix(new NavigateToAlreadyDeclaredSymbolFix(otherSymbol));
            }
            annotationBuilder.create();
        }
    }

    public void checkAttributeList(@NotNull RapidAttributeList attributeList) {
        List<PsiElement> elements = new ArrayList<>();
        for (PsiElement element : PsiTreeUtil.getChildrenOfTypeAsList(attributeList, PsiElement.class)) {
            IElementType elementType = element.getNode().getElementType();
            if (RapidModule.Attribute.TOKEN_SET.contains(elementType)) {
                elements.add(element);
            }
        }
        List<RapidModule.Attribute> attributes = elements.stream()
                .map(element -> RapidModule.Attribute.getAttribute(element.getNode().getElementType()))
                .toList();
        attributes:
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) {
                if (attributes.indexOf(attributes.get(i)) < i) {
                    annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.module.attribute.duplicate"))
                            .range(elements.get(i))
                            .withFix(new RemoveModuleAttributeFix(elements.get(i)))
                            .create();
                    continue;
                }
            }
            if (RapidModule.Attribute.MUTUALLY_EXCLUSIVE.containsKey(attributes.get(i))) {
                for (RapidModule.Attribute attribute : RapidModule.Attribute.MUTUALLY_EXCLUSIVE.get(attributes.get(i))) {
                    if (attributes.contains(attribute)) {
                        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.module.attribute.mutually.exclusive", attributes.get(i).getText(), attribute.getText()))
                                .range(elements.get(i))
                                .withFix(new RemoveModuleAttributeFix(elements.get(i)))
                                .withFix(new RemoveModuleAttributeFix(elements.get(attributes.indexOf(attribute))))
                                .create();
                        continue attributes;
                    }
                }
            }
            if (i > 0) {
                if (attributes.get(i).ordinal() < attributes.get(i - 1).ordinal()) {
                    annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.module.attribute.order"))
                            .range(elements.get(i))
                            .withFix(new ReorderModuleAttributeFix(attributeList))
                            .create();
                }
            }
        }
    }

    public void checkReferenceExpression(@NotNull RapidReferenceExpression expression) {
        RapidSymbol symbol = expression.getSymbol();
        if (symbol == null) {
            PsiElement identifier = expression.getIdentifier();
            if (identifier != null) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.reference.cannot.resolve.symbol", expression.getCanonicalText()))
                        .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                        .range(identifier)
                        .create();
            }
        }
    }

    public void checkDimensions(@NotNull PhysicalField field) {
        RapidArray array = field.getArray();
        if (array != null) {
            List<RapidExpression> dimensions = array.getDimensions();
            if (dimensions.size() > 3) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.array.too.many.dimensions"))
                        .range(array)
                        .create();
            }
            for (RapidExpression dimension : dimensions) {
                if (!(dimension.isConstant())) {
                    annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.expression.not.constant"))
                            .range(dimension)
                            .create();
                    continue;
                }
                checkCompatibleType(RapidType.NUMBER, dimension.getType(), dimension.getTextRange());
            }
        }
    }

    public void checkStatementType(@NotNull RapidAssignmentStatement statement) {
        RapidExpression left = statement.getLeft();
        RapidExpression right = statement.getRight();
        if (left != null && right != null) {
            if (left instanceof RapidReferenceExpression reference) {
                RapidSymbol symbol = reference.getSymbol();
                if (symbol instanceof RapidVariable variable) {
                    checkAssignmentType(variable, right);
                }
            }
        }
    }

    private void checkAssignmentType(@NotNull RapidVariable variable, @NotNull RapidExpression expression) {
        RapidType left = variable.getType();
        if (expression instanceof RapidAggregateExpression aggregate) {
            if (left != null) {
                checkAggregateType(left, aggregate);
            }
        }
        checkCompatibleType(left, expression.getType(), expression.getTextRange());
    }

    private void checkAggregateType(@NotNull RapidType type, @NotNull RapidAggregateExpression aggregate) {
        RapidStructure structure = type.getStructure();
        RapidType arrayType = type.createArrayType(type.getDimensions() - 1);
        if (type.getDimensions() > 0) {
            for (RapidExpression expression : aggregate.getExpressions()) {
                if (expression instanceof RapidAggregateExpression) {
                    checkAggregateType(arrayType, (RapidAggregateExpression) expression);
                } else {
                    checkCompatibleType(arrayType, expression.getType(), expression.getTextRange());
                }
            }
        } else if (structure instanceof RapidRecord record) {
            List<RapidExpression> expressions = aggregate.getExpressions();
            List<RapidComponent> components = record.getComponents();
            if (expressions.size() != components.size()) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.aggregate.number.of.components", record.getName(), components.size()))
                        .range(aggregate)
                        .create();
            } else {
                for (int i = 0; i < expressions.size(); i++) {
                    RapidExpression expression = expressions.get(i);
                    RapidComponent component = components.get(i);
                    if (component.getType() != null) {
                        if (expression instanceof RapidAggregateExpression) {
                            checkAggregateType(component.getType(), (RapidAggregateExpression) expression);
                        } else {
                            checkCompatibleType(component.getType(), expression.getType(), expression.getTextRange());
                        }
                    }
                }
            }
        } else {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.aggregate.invalid.type", type.getPresentableText()))
                    .range(aggregate)
                    .create();
        }
    }

    public void checkInitializer(@NotNull PhysicalField field) {
        RapidExpression initializer = field.getInitializer();
        if (initializer != null) {
            checkAssignmentType(field, initializer);
            switch (field.getAttribute()) {
                case VARIABLE, CONSTANT -> {
                    if (!(initializer.isConstant())) {
                        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.expression.not.constant"))
                                .range(initializer)
                                .create();
                    }
                }
                case PERSISTENT -> {
                    if (!(initializer.isLiteral())) {
                        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.expression.not.literal"))
                                .range(initializer)
                                .create();
                    }
                }
            }
        } else {
            switch (field.getAttribute()) {
                case CONSTANT ->
                        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.constant.not.initialized", field.getName()))
                                .create();
                case PERSISTENT -> {
                    if (field.getVisibility() != Visibility.GLOBAL) {
                        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.persistent.not.initialized", field.getName()))
                                .withFix(new VisibilityFix(field, Visibility.GLOBAL))
                                .create();
                    }
                }
            }
        }
    }

    private void checkCompatibleType(@Nullable RapidType left, @Nullable RapidType right, @NotNull TextRange range) {
        if (left == null || right == null) return;
        if (!(RapidType.isAssignable(left, right))) {
            annotateIncompatibleType(left, right, range);
        }
    }

    private void annotateIncompatibleType(@NotNull RapidType left, @NotNull RapidType right, @NotNull TextRange range) {
        String leftPresentableText = left.getPresentableText();
        String rightPresentableText = right.getPresentableText();
        String description = RapidBundle.message("annotation.description.incompatible.types", leftPresentableText, rightPresentableText);
        String tooltip = RapidBundle.message("annotation.tooltip.incompatible.types", leftPresentableText, rightPresentableText, "#" + ColorUtil.toHex(UIUtil.getContextHelpForeground()));
        annotationHolder.newAnnotation(HighlightSeverity.ERROR, description)
                .tooltip(tooltip)
                .range(range)
                .create();
    }

    public void checkAggregateExpression(@NotNull RapidAggregateExpression expression) {
        RapidStatement statement = PsiTreeUtil.getParentOfType(expression, RapidStatement.class);
        PhysicalField field = PsiTreeUtil.getParentOfType(expression, PhysicalField.class);
        if (statement == null && field == null) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.aggregate.unable.to.calculate.type"))
                    .create();
        }
    }
}