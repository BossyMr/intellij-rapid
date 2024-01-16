package com.bossymr.rapid.ide.editor.insight;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.editor.insight.fix.*;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.*;
import com.bossymr.rapid.language.symbol.resolve.ResolveService;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.modcommand.ModCommandAction;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.ColorUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class RapidAnnotator extends RapidElementVisitor implements Annotator {

    private AnnotationHolder annotationHolder;

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
        try {
            this.annotationHolder = annotationHolder;
            element.accept(this);
        } finally {
            this.annotationHolder = null;
        }
    }

    @Override
    public void visitModule(@NotNull PhysicalModule module) {
        checkModuleName(module);
        super.visitModule(module);
    }

    @Override
    public void visitAlias(@NotNull PhysicalAlias alias) {
        checkAliasType(alias);
        super.visitAlias(alias);
    }

    @Override
    public void visitRecord(@NotNull PhysicalRecord record) {
        super.visitRecord(record);
    }

    @Override
    public void visitComponent(@NotNull PhysicalComponent component) {
        super.visitComponent(component);
    }

    @Override
    public void visitStructure(@NotNull PhysicalStructure structure) {
        checkSymbolAfter(structure, Set.of(PhysicalField.class, PhysicalRoutine.class));
        super.visitStructure(structure);
    }

    @Override
    public void visitField(@NotNull PhysicalField field) {
        checkSymbolAfter(field, Set.of(PhysicalRoutine.class));
        super.visitField(field);
    }

    @Override
    public void visitVariable(@NotNull PhysicalVariable variable) {
        super.visitVariable(variable);
    }

    @Override
    public void visitRoutine(@NotNull PhysicalRoutine routine) {
        super.visitRoutine(routine);
    }

    @Override
    public void visitSymbol(@NotNull PhysicalSymbol symbol) {
        checkIdentifierLength(symbol);
        checkDuplicateSymbol(symbol);
        super.visitSymbol(symbol);
    }

    @Override
    public void visitTypeElement(@NotNull RapidTypeElement typeElement) {
        RapidReferenceExpression expression = typeElement.getReferenceExpression();
        if (expression != null) {
            RapidSymbol symbol = expression.getSymbol();
            if (symbol != null && !(symbol instanceof RapidStructure)) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.reference.not.type", symbol.getPresentableName()))
                                .range(expression)
                                .create();
            }
        }
        super.visitTypeElement(typeElement);
    }

    @Override
    public void visitAttributeList(@NotNull RapidAttributeList attributeList) {
        checkAttributeList(attributeList);
        super.visitAttributeList(attributeList);
    }

    @Override
    public void visitParameterList(@NotNull RapidParameterList parameterList) {
        super.visitParameterList(parameterList);
    }

    @Override
    public void visitStatementList(@NotNull RapidStatementList statementList) {
        super.visitStatementList(statementList);
    }

    @Override
    public void visitParameterGroup(@NotNull PhysicalParameterGroup parameterGroup) {
        if (!(parameterGroup.isOptional()) && parameterGroup.getParameters().size() > 1) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.parameter.group.mutually.exclusive"))
                            .range(parameterGroup)
                            .create();
        }
        super.visitParameterGroup(parameterGroup);
    }

    @Override
    public void visitParameter(@NotNull PhysicalParameter parameter) {
        super.visitParameter(parameter);
    }

    @Override
    public void visitArray(@NotNull RapidArray array) {
        checkDimensionDepth(array);
        checkDimensionExpression(array);
        super.visitArray(array);
    }

    @Override
    public void visitArgumentList(@NotNull RapidArgumentList argumentList) {
        super.visitArgumentList(argumentList);
    }

    @Override
    public void visitExpressionList(@NotNull RapidExpressionList expressionList) {
        super.visitExpressionList(expressionList);
    }

    @Override
    public void visitArgument(@NotNull RapidArgument argument) {
        // TODO: Check that all required arguments are present
        // TODO: Check that no arguments reference an unknown parameter (excess required arguments, for example)
        // TODO: Check that two or more arguments from the same parameter group are present
        super.visitArgument(argument);
    }

    @Override
    public void visitRequiredArgument(@NotNull RapidRequiredArgument argument) {
        super.visitRequiredArgument(argument);
    }

    @Override
    public void visitOptionalArgument(@NotNull RapidOptionalArgument argument) {
        super.visitOptionalArgument(argument);
    }

    @Override
    public void visitConditionalArgument(@NotNull RapidConditionalArgument argument) {
        super.visitConditionalArgument(argument);
    }

    @Override
    public void visitAggregateExpression(@NotNull RapidAggregateExpression expression) {
        super.visitAggregateExpression(expression);
    }

    @Override
    public void visitUnaryExpression(@NotNull RapidUnaryExpression expression) {
        RapidExpression component = expression.getExpression();
        if(component != null) {
           if(component.getType() != null && expression.getType() == null)  {
               String sign = expression.getSign().getText();
               annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.expression.unary.not.applicable", sign, component.getType().getPresentableText()))
                               .range(expression)
                               .create();
           }
        }
        super.visitUnaryExpression(expression);
    }

    @Override
    public void visitReferenceExpression(@NotNull RapidReferenceExpression expression) {
        checkReference(expression);
        super.visitReferenceExpression(expression);
    }

    @Override
    public void visitBinaryExpression(@NotNull RapidBinaryExpression expression) {
        RapidExpression left = expression.getLeft();
        RapidExpression right = expression.getRight();
        if(right != null) {
            if(left.getType() != null && right.getType() != null) {
                if(expression.getType() == null) {
                    String sign = expression.getSign().getText();
                    annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.expression.binary.not.applicable", sign, left.getType().getPresentableText(), right.getType().getPresentableText()))
                            .range(expression)
                            .create();
                }
            }
        }
        super.visitBinaryExpression(expression);
    }

    @Override
    public void visitFunctionCallExpression(@NotNull RapidFunctionCallExpression expression) {
        checkRoutineType(expression.getReferenceExpression(), RoutineType.FUNCTION);
        super.visitFunctionCallExpression(expression);
    }

    private void checkRoutineType(@NotNull RapidExpression expression, @NotNull RoutineType expected) {
        if (!(expression instanceof RapidReferenceExpression referenceExpression)) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.expression.not.routine.of.type", expected.getPresentableText()))
                            .range(expression)
                            .create();
            return;
        }
        RapidSymbol symbol = referenceExpression.getSymbol();
        if (symbol == null) {
            return;
        }
        if (symbol instanceof RapidRoutine routine && routine.getRoutineType() == expected) {
            return;
        }
        AnnotationBuilder builder = annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.expression.not.routine.of.type", expected.getPresentableText()))
                                                    .range(referenceExpression);
        if (symbol instanceof PhysicalRoutine routine) {
            builder = builder.withFix(new ChangeRoutineTypeFix(routine, expected));
        }
        builder.create();
    }

    @Override
    public void visitIndexExpression(@NotNull RapidIndexExpression expression) {
        RapidExpression variable = expression.getExpression();
        RapidType type = variable.getType();
        if (type != null && !(type.isArray())) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.array.type.expected", type.getPresentableText()))
                            .range(variable)
                            .create();
        }
        checkType(RapidPrimitiveType.NUMBER, expression);
        super.visitIndexExpression(expression);
    }

    @Override
    public void visitLiteralExpression(@NotNull RapidLiteralExpression expression) {
        super.visitLiteralExpression(expression);
    }

    @Override
    public void visitStatement(@NotNull RapidStatement statement) {
        super.visitStatement(statement);
    }

    @Override
    public void visitTestCaseStatement(@NotNull RapidTestCaseStatement statement) {
        RapidTestStatement testStatement = PsiTreeUtil.getParentOfType(statement, RapidTestStatement.class);
        Objects.requireNonNull(testStatement);
        RapidExpression expression = testStatement.getExpression();
        if (expression != null) {
            if (statement.getExpressions() != null) {
                for (RapidExpression condition : statement.getExpressions()) {
                    checkType(expression.getType(), condition);
                }
            }
        }
        super.visitTestCaseStatement(statement);
    }

    @Override
    public void visitAssignmentStatement(@NotNull RapidAssignmentStatement statement) {
        RapidExpression target = statement.getLeft();
        if (target != null) {
            checkType(target.getType(), statement.getRight());
        }
        super.visitAssignmentStatement(statement);
    }

    @Override
    public void visitProcedureCallStatement(@NotNull RapidProcedureCallStatement statement) {
        if (!(statement.isLate())) {
            checkType(RapidPrimitiveType.STRING, statement.getReferenceExpression());
        } else {
            RapidExpression expression = statement.getReferenceExpression();
            if (expression instanceof RapidReferenceExpression referenceExpression) {
                checkRoutineType(referenceExpression, RoutineType.PROCEDURE);
            }
        }
        super.visitProcedureCallStatement(statement);
    }

    @Override
    public void visitGotoStatement(@NotNull RapidGotoStatement statement) {
        RapidReferenceExpression expression = statement.getReferenceExpression();
        RapidSymbol symbol = expression != null ? expression.getSymbol() : null;
        if (symbol != null && !(symbol instanceof RapidLabelStatement)) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.expression.not.label"))
                            .range(expression)
                            .create();
        }
        // TODO: "A goto statement may not transfer control into a statement list."
        super.visitGotoStatement(statement);
    }

    @Override
    public void visitConnectStatement(@NotNull RapidConnectStatement statement) {
        if (statement.getLeft() != null) {
            checkConnectTarget(statement.getLeft());
        }
        if (statement.getRight() != null) {
            checkRoutineType(statement.getRight(), RoutineType.TRAP);
        }
        super.visitConnectStatement(statement);
    }

    private void checkConnectTarget(@NotNull RapidExpression expression) {
        checkType(RapidPrimitiveType.NUMBER, expression);
        if (expression instanceof RapidReferenceExpression referenceExpression) {
            RapidSymbol symbol = referenceExpression.getSymbol();
            if (symbol instanceof PhysicalField field) {
                PhysicalRoutine routine = PhysicalRoutine.getRoutine(field);
                if (routine == null) {
                    return;
                }
            }
        }
        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.connect.target.invalid"))
                        .range(expression)
                        .create();
    }

    @Override
    public void visitReturnStatement(@NotNull RapidReturnStatement statement) {
        RapidExpression expression = statement.getExpression();
        PhysicalRoutine routine = PhysicalRoutine.getRoutine(statement);
        if (routine != null) {
            if (routine.getRoutineType() == RoutineType.FUNCTION) {
                if (expression == null) {
                    annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.return.value.missing"))
                                    .range(statement)
                                    .withFix(new ChangeRoutineTypeFix(routine, RoutineType.PROCEDURE))
                                    .withFix(new ChangeRoutineTypeFix(routine, RoutineType.TRAP))
                                    .create();
                } else {
                    RapidType returnType = routine.getType();
                    RapidType providedType = expression.getType();
                    if (providedType == null) {
                        checkType(returnType, expression);
                    } else {
                        checkType(returnType, expression, new ChangeReturnTypeFix(routine, providedType));
                    }
                }
            } else if (expression != null) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.return.invalid", routine.getRoutineType().getPresentableText()))
                                .range(expression)
                                .withFix(new RemoveElementFix(expression, RapidBundle.message("quick.fix.family.remove.return.value")))
                                .create();
            }
        }
        super.visitReturnStatement(statement);
    }

    @Override
    public void visitExitStatement(@NotNull RapidExitStatement statement) {
        super.visitExitStatement(statement);
    }

    @Override
    public void visitRaiseStatement(@NotNull RapidRaiseStatement statement) {
        PhysicalRoutine routine = PhysicalRoutine.getRoutine(statement);
        Objects.requireNonNull(routine);
        RapidStatementList statementList = routine.getStatementLists().stream()
                                                  .filter(list -> PsiTreeUtil.isAncestor(list, statement, false))
                                                  .findFirst().orElseThrow();
        RapidExpression expression = statement.getExpression();
        if (statementList.getStatementListType() == BlockType.ERROR_CLAUSE) {
            if (expression != null) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.raise.value.invalid"))
                                .range(expression)
                                .withFix(new RemoveElementFix(expression, RapidBundle.message("quick.fix.family.remove.raise.value")))
                                .create();
            }
        } else {
            if (expression == null) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.raise.value.missing"))
                                .range(statement)
                                .create();
            } else {
                checkType(RapidPrimitiveType.NUMBER, expression);
            }
        }
        super.visitRaiseStatement(statement);
    }

    @Override
    public void visitRetryStatement(@NotNull RapidRetryStatement statement) {
        checkIfInErrorHandler(statement);
        super.visitRetryStatement(statement);
    }

    @Override
    public void visitTryNextStatement(@NotNull RapidTryNextStatement statement) {
        checkIfInErrorHandler(statement);
        super.visitTryNextStatement(statement);
    }

    private void checkIfInErrorHandler(@NotNull RapidStatement statement) {
        PhysicalRoutine routine = PhysicalRoutine.getRoutine(statement);
        Objects.requireNonNull(routine);
        RapidStatementList statementList = routine.getStatementLists().stream()
                                                  .filter(list -> PsiTreeUtil.isAncestor(list, statement, false))
                                                  .findFirst().orElseThrow();
        if (statementList.getStatementListType() == BlockType.ERROR_CLAUSE) {
            return;
        }
        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.statement.outside.error.handler"))
                        .range(statement)
                        .create();
    }

    @Override
    public void visitTargetVariable(@NotNull PhysicalTargetVariable variable) {
        super.visitTargetVariable(variable);
    }

    @Override
    public void visitIfStatement(@NotNull RapidIfStatement statement) {
        checkType(RapidPrimitiveType.BOOLEAN, statement.getCondition());
        super.visitIfStatement(statement);
    }

    @Override
    public void visitForStatement(@NotNull RapidForStatement statement) {
        checkType(RapidPrimitiveType.NUMBER, statement.getFromExpression());
        checkType(RapidPrimitiveType.NUMBER, statement.getToExpression());
        checkType(RapidPrimitiveType.NUMBER, statement.getStepExpression());
        super.visitForStatement(statement);
    }

    @Override
    public void visitWhileStatement(@NotNull RapidWhileStatement statement) {
        checkType(RapidPrimitiveType.BOOLEAN, statement.getCondition());
        super.visitWhileStatement(statement);
    }

    @Override
    public void visitTestStatement(@NotNull RapidTestStatement statement) {
        super.visitTestStatement(statement);
    }

    @Override
    public void visitLabel(@NotNull RapidLabelStatement statement) {
        super.visitLabel(statement);
    }

    private void checkDimensionDepth(@NotNull RapidArray array) {
        if (array.getDimensions().size() > 3) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.array.too.many.dimensions"))
                            .range(array)
                            .create();
        }
    }

    private void checkDimensionExpression(@NotNull RapidArray array) {
        for (RapidExpression dimension : array.getDimensions()) {
            checkType(RapidPrimitiveType.NUMBER, dimension);
            if (dimension.isConstant()) {
                continue;
            }
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.expression.not.constant"))
                            .range(dimension)
                            .create();
        }
    }

    private void checkType(@Nullable RapidType requiredType, @Nullable RapidExpression expression) {
        // TODO: Calculate possible actions automatically, depending on what the element is (and what is references)
        checkType(requiredType, expression, new ModCommandAction[0]);
    }

    private void checkType(@Nullable RapidType requiredType, @Nullable RapidExpression expression, @NotNull ModCommandAction @NotNull ... actions) {
        if (requiredType == null || expression == null) {
            return;
        }
        RapidType providedType = expression.getType();
        if (providedType == null) {
            return;
        }
        if (requiredType.isAssignable(providedType)) {
            return;
        }
        String message = RapidBundle.message("annotation.description.incompatible.types", requiredType.getPresentableText(), providedType.getPresentableText());
        String tooltip = RapidBundle.message("annotation.tooltip.incompatible.types",
                requiredType.getPresentableText(), providedType.getPresentableText(),
                "#" + ColorUtil.toHex(UIUtil.getContextHelpForeground()));
        AnnotationBuilder builder = annotationHolder.newAnnotation(HighlightSeverity.ERROR, message)
                                                    .tooltip(tooltip)
                                                    .range(expression);
        for (ModCommandAction action : actions) {
            builder = builder.withFix(action);
        }
        builder.create();
    }

    private void checkReference(@NotNull RapidReferenceExpression expression) {
        PsiElement identifier = expression.getIdentifier();
        if (identifier == null) {
            return;
        }
        RapidSymbol symbol = expression.getSymbol();
        if (symbol != null) {
            return;
        }
        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.reference.cannot.resolve.symbol", expression.getText()))
                        .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                        .range(identifier)
                        .create();
    }

    private void checkAliasType(@NotNull PhysicalAlias alias) {
        RapidType type = alias.getType();
        RapidTypeElement typeElement = alias.getTypeElement();
        if (typeElement == null || type == null) {
            return;
        }
        RapidStructure structure = type.getStructure();
        if (!(structure instanceof RapidAlias)) {
            return;
        }
        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.alias.type.alias"))
                        .range(typeElement)
                        .create();
    }

    private void checkIdentifierLength(@NotNull PhysicalSymbol symbol) {
        PsiElement identifier = symbol.getNameIdentifier();
        if (identifier == null) {
            return;
        }
        if (identifier.getTextLength() > 32) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.identifier.length"))
                            .range(identifier)
                            .withFix(new InvokeRenameElementFix(symbol))
                            .create();
        }
    }

    public void checkSymbolAfter(@NotNull PhysicalVisibleSymbol symbol, @NotNull Set<Class<? extends PhysicalVisibleSymbol>> elements) {
        PsiElement identifier = symbol.getNameIdentifier();
        if (identifier == null) {
            return;
        }
        PhysicalModule module = PhysicalModule.getModule(symbol);
        if (module == null) {
            return;
        }
        PsiElement sibling = PsiTreeUtil.skipWhitespacesAndCommentsBackward(symbol);
        for (Class<? extends PhysicalVisibleSymbol> element : elements) {
            if (element.isInstance(sibling)) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.wrong.arrangement"))
                                .range(identifier)
                                .withFix(new SymbolArrangementOrderFix(module))
                                .create();
            }
        }
    }

    private void checkDuplicateSymbol(@NotNull PhysicalSymbol symbol) {
        PsiElement identifier = symbol.getNameIdentifier();
        String name = symbol.getName();
        if (identifier == null || name == null) {
            return;
        }
        ResolveService service = ResolveService.getInstance(symbol.getProject());
        List<RapidSymbol> symbols = service.findSymbols(symbol, name);
        if (symbols.isEmpty() || symbols.indexOf(symbol) == 0) {
            return;
        }
        RapidSymbol duplicateSymbol = symbols.get(0);
        AnnotationBuilder annotationBuilder = annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.declaration.duplicate.symbol", name))
                                                              .range(identifier);
        if (duplicateSymbol instanceof PhysicalSymbol physicalSymbol) {
            annotationBuilder = annotationBuilder.withFix(new NavigateToAlreadyDeclaredSymbolFix(physicalSymbol));
        }
        annotationBuilder.create();
    }

    private void checkModuleName(@NotNull PhysicalModule module) {
        PsiElement identifier = module.getNameIdentifier();
        String name = module.getName();
        if (identifier == null || name == null) {
            return;
        }
        PsiFile containingFile = module.getContainingFile();
        String fileName = containingFile.getViewProvider().getVirtualFile().getNameWithoutExtension();
        if (name.equals(fileName)) {
            return;
        }
        AnnotationBuilder annotationBuilder = annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.module.name", name))
                                                              .range(identifier);
        if (containingFile instanceof RapidFile file) {
            List<PhysicalModule> modules = file.getModules();
            if (modules.size() > 1) {
                PsiDirectory containingDirectory = containingFile.getContainingDirectory();
                PsiFile correctFile = containingDirectory.findFile(name + RapidFileType.DEFAULT_DOT_EXTENSION);
                if (correctFile == null) {
                    annotationBuilder = annotationBuilder.withFix(new MoveModuleToSeparateFileFix(module));
                }
            }
            boolean canRenameFile = modules.stream().noneMatch(otherModule -> fileName.equals(otherModule.getName()));
            if (canRenameFile) {
                annotationBuilder = annotationBuilder.withFix(new RenameElementFix(containingFile, name + RapidFileType.DEFAULT_DOT_EXTENSION))
                                                     .withFix(new RenameElementFix(module, fileName));
            }
        }
        annotationBuilder.create();
    }

    private void checkAttributeList(@NotNull RapidAttributeList attributeList) {
        List<ASTNode> unsorted = List.of(attributeList.getNode().getChildren(ModuleType.TOKEN_SET));
        List<ASTNode> sorted = new ArrayList<>(unsorted);
        List<ModuleType> attributes = unsorted.stream().map(node -> ModuleType.getAttribute(node.getElementType())).toList();
        sorted.sort(Comparator.comparing(element -> ModuleType.getAttribute(element.getElementType()), Comparator.comparing(Enum::ordinal)));
        for (ASTNode element : unsorted) {
            ModuleType moduleType = ModuleType.getAttribute(element.getElementType());
            int index = unsorted.indexOf(element);
            if (sorted.indexOf(element) > index) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.module.attribute.order"))
                                .range(element)
                                .withFix(new ReorderModuleAttributeFix(attributeList))
                                .create();
            }
            int firstIndex = attributes.indexOf(moduleType);
            if (firstIndex != attributes.lastIndexOf(moduleType) && index == firstIndex) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.module.attribute.duplicate"))
                                .range(element)
                                .withFix(new RemoveModuleAttributeFix(element.getPsi()))
                                .create();
            }
            if (ModuleType.MUTUALLY_EXCLUSIVE.containsKey(moduleType)) {
                List<ModuleType> exlusiveList = ModuleType.MUTUALLY_EXCLUSIVE.get(moduleType);
                Optional<ModuleType> exlusiveType = attributes.stream().filter(exlusiveList::contains).findFirst();
                if (exlusiveType.isPresent()) {
                    ModuleType otherType = exlusiveType.orElseThrow();
                    annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.module.attribute.mutually.exclusive", otherType.getText(), moduleType.getText()))
                                    .range(element)
                                    .withFix(new RemoveModuleAttributeFix(element.getPsi()))
                                    .withFix(new RemoveModuleAttributeFix(unsorted.get(attributes.indexOf(otherType)).getPsi()))
                                    .create();
                }
            }
        }
    }
}