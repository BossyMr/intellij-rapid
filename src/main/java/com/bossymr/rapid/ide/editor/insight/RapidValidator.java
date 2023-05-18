package com.bossymr.rapid.ide.editor.insight;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.editor.insight.quickfix.*;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.*;
import com.bossymr.rapid.language.symbol.resolve.RapidResolveService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

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
            boolean existsModuleForFile =
                    containingFile.getModules().stream().anyMatch(otherModule -> !(otherModule.getManager().areElementsEquivalent(otherModule, module))
                            && fileName.equals(otherModule.getName()));
            if (!(existsModuleForFile)) {
                /*
                 * If no module in the containing file is correct (the same name as the file), the file can be renamed to
                 * the name of the module, or the module can be renamed to the name of the file.
                 *
                 * This cannot be done if another module with the same name as the file name already exists. As that module
                 * would no longer match the name of the file, or multiple modules would exist with the same name.
                 */
                annotationBuilder =
                        annotationBuilder.withFix(new RenameFileFix(name + RapidFileType.DEFAULT_DOT_EXTENSION));
                annotationBuilder = annotationBuilder.withFix(new RenameElementFix(module, fileName));
            }
            annotationBuilder.create();
        }
    }

    public void checkDuplicateSymbol(@NotNull PhysicalSymbol symbol) {
        String name = symbol.getName();
        PsiElement nameIdentifier = symbol.getNameIdentifier();
        if (name == null || nameIdentifier == null) return;
        List<RapidSymbol> symbols = RapidResolveService.getInstance(symbol.getProject()).findSymbols(symbol, name);
        if (symbols.size() > 1 && symbols.indexOf(symbol) != 0) {
            /*
             * Multiple symbols are declared with the same name as this symbol, in the same context.
             */
            annotateDuplicateSymbol(symbol, symbols.get(0));
        }
    }

    private void annotateDuplicateSymbol(@NotNull PhysicalSymbol original, @NotNull RapidSymbol duplicate) {
        PsiElement nameIdentifier = original.getNameIdentifier();
        if (nameIdentifier == null) return;
        AnnotationBuilder annotationBuilder =
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.declaration.duplicate.symbol", original.getName()))
                        .range(nameIdentifier);
        if (duplicate instanceof PhysicalSymbol otherSymbol) {
            annotationBuilder = annotationBuilder.withFix(new NavigateToAlreadyDeclaredSymbolFix(otherSymbol));
        }
        annotationBuilder.create();
    }

    public void checkAttributeList(@NotNull RapidAttributeList attributeList) {
        List<PsiElement> elements = new ArrayList<>();
        for (PsiElement element : PsiTreeUtil.getChildrenOfTypeAsList(attributeList, PsiElement.class)) {
            IElementType elementType = element.getNode().getElementType();
            if (ModuleType.TOKEN_SET.contains(elementType)) {
                elements.add(element);
            }
        }
        List<ModuleType> moduleTypes = elements.stream()
                .map(element -> ModuleType.getAttribute(element.getNode().getElementType()))
                .toList();
        attributes:
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) {
                if (moduleTypes.indexOf(moduleTypes.get(i)) < i) {
                    annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.module.attribute.duplicate"))
                            .range(elements.get(i))
                            .withFix(new RemoveModuleAttributeFix(elements.get(i)))
                            .create();
                    continue;
                }
            }
            if (ModuleType.MUTUALLY_EXCLUSIVE.containsKey(moduleTypes.get(i))) {
                for (ModuleType moduleType : ModuleType.MUTUALLY_EXCLUSIVE.get(moduleTypes.get(i))) {
                    if (moduleTypes.contains(moduleType)) {
                        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.module.attribute.mutually.exclusive", moduleTypes.get(i).getText(), moduleType.getText()))
                                .range(elements.get(i))
                                .withFix(new RemoveModuleAttributeFix(elements.get(i)))
                                .withFix(new RemoveModuleAttributeFix(elements.get(moduleTypes.indexOf(moduleType))))
                                .create();
                        continue attributes;
                    }
                }
            }
            if (i > 0) {
                if (moduleTypes.get(i).ordinal() < moduleTypes.get(i - 1).ordinal()) {
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
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.reference.cannot.resolve.symbol", expression.getText()))
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
                checkCompatibleType(RapidType.NUMBER, dimension);
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
                    if (!(variable.isModifiable())) {
                        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.variable.read.only", variable.getName()))
                                .range(reference)
                                .create();
                    }
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
        checkCompatibleType(left, expression);
    }

    private void checkAggregateType(@NotNull RapidType type, @NotNull RapidAggregateExpression aggregate) {
        RapidStructure structure = type.getStructure();
        RapidType arrayType = type.createArrayType(type.getDimensions() - 1);
        if (type.getDimensions() > 0) {
            for (RapidExpression expression : aggregate.getExpressions()) {
                if (expression instanceof RapidAggregateExpression) {
                    checkAggregateType(arrayType, (RapidAggregateExpression) expression);
                } else {
                    checkCompatibleType(arrayType, expression);
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
                            checkCompatibleType(component.getType(), expression);
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

    public void checkConnectLeft(@NotNull RapidExpression expression) {
        checkCompatibleType(RapidType.NUMBER, expression);
        if (expression instanceof RapidIndexExpression indexExpression) {
            expression = indexExpression.getExpression();
        }
        if (!(expression instanceof RapidReferenceExpression referenceExpression)) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.connect.target.invalid"))
                    .range(expression)
                    .create();
            return;
        }
        RapidSymbol symbol = referenceExpression.getSymbol();
        if (symbol == null) return;
        if (symbol instanceof PhysicalField field) {
            if (field.getFieldType() != FieldType.VARIABLE) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.connect.target.not.variable", field.getName()))
                        .range(expression)
                        .create();
                return;
            }
            if (PsiTreeUtil.getParentOfType(field, PhysicalRoutine.class) != null) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.connect.target.not.module", field.getName()))
                        .range(expression)
                        .create();
                return;
            }
        }
        if (symbol instanceof PhysicalParameter parameter) {
            if (parameter.getParameterType() != ParameterType.VARIABLE
                    && parameter.getParameterType() != ParameterType.INOUT) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.connect.target.wrong.parameter", parameter.getName()))
                        .range(expression)
                        .create();
            }
        }
    }

    public void checkConnectRight(@NotNull RapidExpression expression) {
        if (!(expression instanceof RapidReferenceExpression referenceExpression)) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.expression.not.trap"))
                    .range(expression)
                    .create();
            return;
        }
        RapidSymbol symbol = referenceExpression.getSymbol();
        if (symbol == null) return;
        if (!(symbol instanceof RapidRoutine routine) || routine.getRoutineType() != RoutineType.TRAP) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.reference.not.trap", symbol.getName()))
                    .range(expression)
                    .create();
        }
    }

    public void checkOutsideErrorHandler(@NotNull RapidStatement statement, @NotNull String message) {
        RapidStatementList statementList = PsiTreeUtil.getParentOfType(statement, RapidStatementList.class);
        if (statementList == null) return;
        if (statementList.getAttribute() == StatementListType.ERROR_CLAUSE) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, message)
                    .range(statement)
                    .create();
        }
    }

    public void checkInsideErrorHandler(@NotNull RapidStatement statement, @NotNull String message) {
        RapidStatementList statementList = PsiTreeUtil.getParentOfType(statement, RapidStatementList.class);
        if (statementList == null) return;
        if (statementList.getAttribute() != StatementListType.ERROR_CLAUSE) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, message)
                    .range(statement)
                    .create();
        }
    }

    public void checkInitializer(@NotNull PhysicalField field) {
        RapidExpression initializer = field.findInitializer();
        if (initializer != null) {
            checkAssignmentType(field, initializer);
            switch (field.getFieldType()) {
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
            switch (field.getFieldType()) {
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

    public void checkReturnStatement(@NotNull RapidReturnStatement statement) {
        PhysicalRoutine routine = PsiTreeUtil.getParentOfType(statement, PhysicalRoutine.class);
        if (routine == null) return;
        if (statement.getExpression() != null) {
            if (routine.getRoutineType() == RoutineType.FUNCTION) {
                checkCompatibleType(routine.getType(), statement.getExpression());
            } else {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.return.invalid", routine.getName()))
                        .range(statement)
                        .create();
            }
        } else {
            if (routine.getRoutineType() == RoutineType.FUNCTION) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.return.missing"))
                        .range(statement)
                        .create();
            }
        }
    }

    public void checkAliasType(@NotNull PhysicalAlias alias) {
        RapidType type = alias.getType();
        if (alias.getTypeElement() == null || type == null) return;
        RapidStructure structure = type.getStructure();
        if (!(structure instanceof RapidAlias)) return;
        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.alias.type.alias"))
                .range(alias.getTypeElement())
                .create();
    }

    @SafeVarargs
    public final void checkAfter(@NotNull PsiElement element, @NotNull TextRange textRange,
                                 @NotNull Class<? extends PsiElement>... elements) {
        PsiElement sibling = element;
        while ((sibling = sibling.getPrevSibling()) != null) {
            for (Class<? extends PsiElement> clazz : elements) {
                if (clazz.isInstance(sibling)) {
                    annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.wrong.arrangement"))
                            .range(textRange)
                            .create();
                }
            }
        }
    }

    public void checkFieldStatementList(@NotNull PhysicalField field) {
        PhysicalRoutine routine = PsiTreeUtil.getParentOfType(field, PhysicalRoutine.class);
        if (routine == null) {
            checkAfter(field, field.getTextRange(), PhysicalRoutine.class);
            return;
        }
        RapidStatementList statementList = PsiTreeUtil.getParentOfType(field, RapidStatementList.class);
        if (statementList != null) {
            if (statementList.getParent().equals(routine)) {
                // This field is a routine variable, and should be at the top of the statement list
                PsiElement sibling = PsiTreeUtil.skipWhitespacesAndCommentsBackward(field);
                if (sibling instanceof RapidStatement) {
                    annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.field.top.of.statement.list", field.getName(), routine.getName()))
                            .range(field)
                            .create();
                }
            } else {
                // This field is inside a compound statement (if, for, while, test)
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.field.top.of.statement.list", field.getName(), routine.getName()))
                        .range(field)
                        .create();
            }
        }
    }

    public void checkAggregateExpression(@NotNull RapidAggregateExpression expression) {
        RapidStatement statement = PsiTreeUtil.getParentOfType(expression, RapidStatement.class);
        PhysicalField field = PsiTreeUtil.getParentOfType(expression, PhysicalField.class);
        if (statement == null && field == null) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.aggregate.unable.to.calculate.type"))
                    .create();
        }
    }

    public void checkCompatibleType(@Nullable RapidType type, @Nullable RapidExpression expression) {
        if (expression == null) return;
        RapidType right = expression.getType();
        if (type == null || right == null) return;
        if (type.getDimensions() < 0 || right.getDimensions() < 0) return;
        if (!(RapidType.isAssignable(type, right))) {
            annotateIncompatibleType(type, right, expression.getTextRange());
        }
    }

    private void annotateIncompatibleType(@NotNull RapidType left, @NotNull RapidType right, @NotNull TextRange range) {
        String leftPresentableText = left.getPresentableText();
        String rightPresentableText = right.getPresentableText();
        String description =
                RapidBundle.message("annotation.description.incompatible.types", leftPresentableText, rightPresentableText);
        String tooltip =
                RapidBundle.message("annotation.tooltip.incompatible.types", leftPresentableText, rightPresentableText,
                        "#"
                                + ColorUtil.toHex(UIUtil.getContextHelpForeground()));
        annotationHolder.newAnnotation(HighlightSeverity.ERROR, description)
                .tooltip(tooltip)
                .range(range)
                .create();
    }

    public void checkParameterDeclaration(@NotNull RapidParameterList parameterList) {
        Map<String, PhysicalParameter> names = new HashMap<>();
        for (PhysicalParameterGroup group : parameterList.getParameters()) {
            List<PhysicalParameter> parameters = group.getParameters();
            if (!(group.isOptional()) && parameters.size() > 1) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.parameter.group.mutually.exclusive"))
                        .range(group)
                        .withFix(new MakeParameterListOptional(group))
                        .create();
                continue;
            }
            for (PhysicalParameter parameter : parameters) {
                String name = parameter.getName();
                if (names.containsKey(name)) {
                    annotateDuplicateSymbol(parameter, names.get(name));
                    continue;
                }
                names.put(name, parameter);
            }
        }
    }

    public void checkRoutineCall(@NotNull RapidRoutine routine, @NotNull RapidArgumentList argumentList) {
        List<? extends RapidParameterGroup> parameters = routine.getParameters();
        if (parameters == null) return;
        List<RapidParameter> previous = new ArrayList<>();
        for (RapidArgument argument : argumentList.getArguments()) {
            RapidParameter parameter = getParameter(routine, argumentList, previous, argument);
            if (parameter != null) {
                if (previous.size() > 0) {
                    int index = parameters.indexOf(parameter.getParameterGroup());
                    RapidParameter last = previous.get(previous.size() - 1);
                    int prev = parameters.indexOf(last.getParameterGroup());
                    if (prev > index) {
                        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.argument.order"))
                                .range(argument)
                                .create();
                        continue;
                    }
                    if (prev == index) {
                        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.argument.exclusive", parameter.getName(), last.getName()))
                                .range(argument)
                                .create();
                        continue;
                    }
                }
                previous.add(parameter);
                if (parameter.getParameterGroup().isOptional()) {
                    if (argument instanceof RapidRequiredArgument) {
                        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.parameter.optional", parameter.getName()))
                                .range(argument)
                                .create();
                        continue;
                    }
                    if (argument instanceof RapidOptionalArgument && argument.getArgument() == null) {
                        RapidType type = parameter.getType();
                        if (type != null && !(type.getPresentableText().equals("switch"))) {
                            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.optional.argument.not.switch", parameter.getName()))
                                    .range(argument)
                                    .create();
                            continue;
                        }
                    }
                } else {
                    if (!(argument instanceof RapidRequiredArgument)) {
                        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.parameter.required", parameter.getName()))
                                .range(argument)
                                .create();
                        continue;
                    }
                }
                checkCompatibleType(parameter.getType(), argument.getArgument());
                checkParameterArgument(parameter, argument);
            }
        }
        List<? extends RapidParameterGroup> missing = new ArrayList<>(parameters);
        List<RapidParameterGroup> groups = previous.stream()
                .map(RapidParameter::getParameterGroup)
                .toList();
        missing.removeIf(RapidParameterGroup::isOptional);
        missing.removeIf(groups::contains);
        if (missing.size() > 0) {
            long size = parameters.stream().filter(group -> !(group.isOptional())).count();
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.routine.call.number.of.components", routine.getName(), size))
                    .range(argumentList)
                    .create();
        }
    }

    private void checkParameterArgument(@NotNull RapidParameter parameter, @NotNull RapidArgument argument) {
        RapidExpression expression = argument.getArgument();
        if (expression == null) return;
        if (parameter.getParameterType() == ParameterType.INPUT) return;
        RapidReferenceExpression referenceExpression = getReferenceExpression(expression);
        if (referenceExpression == null) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.expression.not.variable"))
                    .range(expression)
                    .create();
            return;
        }
        RapidSymbol symbol = referenceExpression.getSymbol();
        if (symbol == null) return;
        if (!(symbol instanceof RapidVariable variable)) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.expression.not.variable"))
                    .range(expression)
                    .create();
            return;
        }
        if (variable instanceof RapidField field) {
            if (!(variable.isModifiable()) && parameter.getParameterType() != ParameterType.REFERENCE) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.variable.read.only", variable.getName()))
                        .range(referenceExpression)
                        .create();
                return;
            }
            if (field.getFieldType() == FieldType.CONSTANT
                    && parameter.getParameterType() != ParameterType.REFERENCE) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.variable.constant", variable.getName()))
                        .range(referenceExpression)
                        .create();
                return;
            }
            if (field.getFieldType() == FieldType.VARIABLE
                    && parameter.getParameterType() == ParameterType.PERSISTENT) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.parameter.persistent", variable.getName()))
                        .range(referenceExpression)
                        .create();
                return;
            }
        }
        switch (parameter.getParameterType()) {
            case VARIABLE -> {
                if (variable instanceof RapidParameter other) {
                    if (other.getParameterType() == ParameterType.PERSISTENT) {
                        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.parameter.persistent", variable.getName()))
                                .range(referenceExpression)
                                .create();
                    }
                }
            }
            case PERSISTENT -> {
                if (variable instanceof RapidParameter other) {
                    if (other.getParameterType() == ParameterType.INPUT) {
                        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.parameter.input", variable.getName()))
                                .range(referenceExpression)
                                .create();
                    }
                    if (other.getParameterType() == ParameterType.VARIABLE) {
                        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.parameter.variable", variable.getName()))
                                .range(referenceExpression)
                                .create();
                    }
                }
            }
        }
    }

    private @Nullable RapidReferenceExpression getReferenceExpression(@NotNull RapidExpression expression) {
        if (expression instanceof RapidReferenceExpression referenceExpression) {
            return referenceExpression;
        }
        if (expression instanceof RapidIndexExpression indexExpression) {
            if (indexExpression.getExpression() instanceof RapidReferenceExpression referenceExpression) {
                return referenceExpression;
            }
        }
        return null;
    }

    private @Nullable RapidParameter getParameter(@NotNull RapidRoutine routine,
                                                  @NotNull RapidArgumentList argumentList,
                                                  @NotNull List<RapidParameter> previous,
                                                  @NotNull RapidArgument argument) {
        if (argument.getParameter() != null) {
            RapidSymbol symbol = argument.getParameter().getSymbol();
            if (symbol == null) return null;
            if (symbol instanceof RapidParameter parameter) {
                return parameter;
            }
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.reference.not.parameter", symbol.getName()))
                    .range(argument.getParameter())
                    .create();
        } else {
            List<? extends RapidParameterGroup> groups = routine.getParameters();
            if (groups == null) return null;
            for (RapidParameterGroup group : groups) {
                if (group.isOptional()) continue;
                List<? extends RapidParameter> parameters = group.getParameters();
                if (parameters.size() == 0) continue;
                if (previous.contains(parameters.get(0))) continue;
                return parameters.get(0);
            }
            long size = groups.stream().filter(group -> !(group.isOptional())).count();
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.routine.call.number.of.components", routine.getName(), size))
                    .range(argumentList)
                    .create();
        }
        return null;
    }

    public void checkIdentifier(@NotNull PsiElement element) {
        if (element.getTextLength() > 32) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.identifier.length"))
                    .range(element)
                    .create();
        }
    }

    public void checkType(@Nullable RapidReferenceExpression expression, @NotNull Function<RapidSymbol, String> message,
                          @NotNull Predicate<RapidSymbol> predicate) {
        if (expression == null) return;
        RapidSymbol symbol = expression.getSymbol();
        if (symbol == null || predicate.test(symbol)) return;
        annotationHolder.newAnnotation(HighlightSeverity.ERROR, message.apply(symbol))
                .range(expression)
                .create();
    }

    public void checkType(@Nullable RapidExpression expression, @NotNull String message,
                          @NotNull Function<RapidSymbol, String> function, @NotNull Predicate<RapidSymbol> predicate) {
        if (expression == null) return;
        if (!(expression instanceof RapidReferenceExpression referenceExpression)) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, message)
                    .range(expression)
                    .create();
            return;
        }
        checkType(referenceExpression, function, predicate);
    }


    public void checkBinaryExpression(@NotNull RapidBinaryExpression expression) {
        RapidType left = expression.getLeft().getType();
        if (expression.getRight() == null) return;
        RapidType right = expression.getRight().getType();
        if (left == null || right == null) return;
        String sign = expression.getSign().getText();
        if (expression.getType() == null) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.expression.binary.not.applicable", sign, left.getPresentableText(), right.getPresentableText()))
                    .range(expression)
                    .create();
        }
    }

    public void checkUnaryExpression(@NotNull RapidUnaryExpression expression) {
        if (expression.getExpression() == null) return;
        RapidType type = expression.getExpression().getType();
        if (type == null) return;
        String sign = expression.getSign().getText();
        if (expression.getType() == null) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.expression.unary.not.applicable", sign, type.getPresentableText()))
                    .range(expression)
                    .create();
        }
    }

    public void checkReferenceExpressionType(@Nullable RapidExpression expression) {
        if (expression == null) return;
        if (expression instanceof RapidReferenceExpression referenceExpression) {
            RapidSymbol symbol = referenceExpression.getSymbol();
            if (symbol != null && !(symbol instanceof RapidVariable)) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.reference.not.variable", symbol.getName()))
                        .range(expression)
                        .create();
            }
        }
    }

    public void checkIndexType(@NotNull RapidIndexExpression expression) {
        RapidType type = expression.getType();
        if (type != null) {
            if (type.getDimensions() < 0) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.expression.not.array"))
                        .range(expression)
                        .create();
            }
        }
    }
}