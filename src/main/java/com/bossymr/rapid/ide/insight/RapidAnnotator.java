package com.bossymr.rapid.ide.insight;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalAlias;
import com.bossymr.rapid.language.symbol.physical.PhysicalField;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.bossymr.rapid.language.symbol.resolve.ResolveUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.ColorUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class RapidAnnotator extends RapidElementVisitor implements Annotator {

    private AnnotationHolder annotationHolder;

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
        this.annotationHolder = annotationHolder;
        try {
            element.accept(this);
        } finally {
            this.annotationHolder = null;
        }
    }

    @Override
    public void visitSymbol(@NotNull PhysicalSymbol symbol) {
        validateSymbol(symbol);
        super.visitSymbol(symbol);
    }

    private void validateSymbol(@NotNull PhysicalSymbol symbol) {
        String name = symbol.getName();
        PsiElement identifier = symbol.getNameIdentifier();
        if (identifier != null && name != null) {
            Set<RapidSymbol> results = ResolveUtil.getSymbols(symbol, name);
            if (results.size() > 1) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.declaration.duplicate.symbol", name))
                        .range(identifier)
                        .create();
            }
        }
    }

    @Override
    public void visitTypeElement(@NotNull RapidTypeElement typeElement) {
        validateTypeElement(typeElement);
        super.visitTypeElement(typeElement);
    }

    private void validateTypeElement(@NotNull RapidTypeElement typeElement) {
        RapidReferenceExpression expression = typeElement.getReferenceExpression();
        if (expression != null) {
            RapidSymbol symbol = expression.getSymbol();
            if (!(symbol instanceof RapidStructure)) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.reference.cannot.resolve.symbol", expression.getCanonicalText()))
                        .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                        .range(expression)
                        .create();
            }
        }
    }

    @Override
    public void visitFunctionCallExpression(@NotNull RapidFunctionCallExpression expression) {
        validateFunctionCallExpression(expression);
        super.visitFunctionCallExpression(expression);
    }

    private void validateFunctionCallExpression(@NotNull RapidFunctionCallExpression expression) {
        RapidReferenceExpression referenceExpression = expression.getReferenceExpression();
        RapidSymbol symbol = referenceExpression.getSymbol();
        if (!(symbol instanceof RapidRoutine routine) || routine.getAttribute() != RapidRoutine.Attribute.FUNCTION) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.reference.cannot.resolve.symbol", referenceExpression.getCanonicalText()))
                    .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                    .range(referenceExpression)
                    .create();
        }
    }

    @Override
    public void visitProcedureCallStatement(@NotNull RapidProcedureCallStatement statement) {
        validateProcedureCallStatement(statement);
        super.visitProcedureCallStatement(statement);
    }

    private void validateProcedureCallStatement(@NotNull RapidProcedureCallStatement statement) {
        RapidExpression expression = statement.getReferenceExpression();
        if (expression instanceof RapidReferenceExpression referenceExpression) {
            RapidSymbol symbol = referenceExpression.getSymbol();
            if (!(symbol instanceof RapidRoutine routine) || routine.getAttribute() != RapidRoutine.Attribute.PROCEDURE) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.reference.cannot.resolve.symbol", referenceExpression.getCanonicalText()))
                        .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                        .range(referenceExpression)
                        .create();
            }
        }
    }

    @Override
    public void visitAttributeList(@NotNull RapidAttributeList attributeList) {
        validateAttributeList(attributeList);
        super.visitAttributeList(attributeList);
    }

    private void validateAttributeList(@NotNull RapidAttributeList attributeList) {
        Set<RapidModule.Attribute> attributes = new HashSet<>();
        for (PsiElement child : attributeList.getChildren()) {
            if (RapidModule.Attribute.TOKEN_SET.contains(child.getNode().getElementType())) {
                RapidModule.Attribute attribute = RapidModule.Attribute.getAttribute(child.getNode().getElementType());
                if (attribute == RapidModule.Attribute.NO_VIEW) {
                    RapidModule.Attribute mutual = null;
                    if (attributes.contains(RapidModule.Attribute.NO_STEP_IN))
                        mutual = RapidModule.Attribute.NO_STEP_IN;
                    if (attributes.contains(RapidModule.Attribute.VIEW_ONLY)) mutual = RapidModule.Attribute.VIEW_ONLY;
                    if (attributes.contains(RapidModule.Attribute.READ_ONLY)) mutual = RapidModule.Attribute.READ_ONLY;
                    if (mutual != null) {
                        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.module.attribute.exclusive", attribute.getText(), mutual.getText()))
                                .range(child.getTextRange())
                                .create();
                    }
                }
                if (attribute == RapidModule.Attribute.NO_STEP_IN || attribute == RapidModule.Attribute.VIEW_ONLY || attribute == RapidModule.Attribute.READ_ONLY) {
                    if (attributes.contains(RapidModule.Attribute.NO_VIEW)) {
                        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.module.attribute.exclusive", attribute.getText(), RapidModule.Attribute.NO_VIEW.getText()))
                                .range(child.getTextRange())
                                .create();
                    }
                }
                if (attribute == RapidModule.Attribute.VIEW_ONLY) {
                    if (attributes.contains(RapidModule.Attribute.READ_ONLY)) {
                        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.module.attribute.exclusive", attribute.getText(), RapidModule.Attribute.READ_ONLY.getText()))
                                .range(child.getTextRange())
                                .create();
                    }
                }
                if (attribute == RapidModule.Attribute.READ_ONLY) {
                    if (attributes.contains(RapidModule.Attribute.VIEW_ONLY)) {
                        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.module.attribute.exclusive", attribute.getText(), RapidModule.Attribute.VIEW_ONLY.getText()))
                                .range(child.getTextRange())
                                .create();
                    }
                }
                if (attributes.contains(attribute)) {
                    annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.module.duplicate.attribute"))
                            .range(child.getTextRange())
                            .create();
                } else {
                    attributes.add(attribute);
                }
            }
        }
    }

    @Override
    public void visitField(@NotNull PhysicalField field) {
        validateField(field);
        super.visitField(field);
    }

    private void validateField(@NotNull PhysicalField field) {
        // RAPID specification (section 2.22 - 2.24)
        RapidArray array = field.getArray();
        if (array != null) {
            for (RapidExpression expression : array.getDimensions()) {
                if (!(expression.isConstant())) {
                    annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("expression.not.constant"))
                            .range(expression.getTextRange())
                            .create();
                }
            }
        }
        RapidExpression initializer = field.getInitializer();
        if (initializer != null) {
            switch (field.getAttribute()) {
                case VARIABLE, CONSTANT -> {
                    if (!(initializer.isConstant())) {
                        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("expression.not.constant"))
                                .range(initializer.getTextRange())
                                .create();
                    }
                }
                case PERSISTENT -> {
                    if (!(initializer.isLiteral())) {
                        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("expression.not.literal"))
                                .range(initializer.getTextRange())
                                .create();
                    }
                }
            }
        }
    }

    @Override
    public void visitAlias(@NotNull PhysicalAlias alias) {
        validateAlias(alias);
        super.visitAlias(alias);
    }

    private void validateAlias(@NotNull PhysicalAlias alias) {
        RapidType type = alias.getType();
        RapidTypeElement typeElement = alias.getTypeElement();
        if (type != null && typeElement != null) {
            RapidStructure structure = type.getStructure();
            if (structure instanceof RapidAlias) {
                // RAPID specification (section 2.15)
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.alias.type.alias"))
                        .range(typeElement.getTextRange())
                        .create();
            }
        }
    }

    @Override
    public void visitReferenceExpression(@NotNull RapidReferenceExpression expression) {
        validateReferenceExpression(expression);
        super.visitReferenceExpression(expression);
    }

    private void validateReferenceExpression(@NotNull RapidReferenceExpression expression) {
        PsiElement identifier = expression.getIdentifier();
        if (identifier != null) {
            RapidSymbol element = expression.getSymbol();
            if (element == null) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.reference.cannot.resolve.symbol", expression.getCanonicalText()))
                        .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                        .range(identifier)
                        .create();
            }
        }
    }

    @Override
    public void visitGotoStatement(@NotNull RapidGotoStatement statement) {
        validateGotoStatement(statement);
        super.visitGotoStatement(statement);
    }

    private void validateGotoStatement(@NotNull RapidGotoStatement statement) {
        RapidReferenceExpression expression = statement.getReferenceExpression();
        if (expression != null) {
            RapidSymbol symbol = expression.getSymbol();
            if (!(symbol instanceof RapidLabelStatement)) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.reference.cannot.resolve.symbol", expression.getCanonicalText()))
                        .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                        .range(expression)
                        .create();
            }
        }
    }

    @Override
    public void visitReturnStatement(@NotNull RapidReturnStatement statement) {
        validateReturnStatement(statement);
        super.visitReturnStatement(statement);
    }

    private void validateReturnStatement(@NotNull RapidReturnStatement statement) {
        RapidExpression expression = statement.getExpression();
        PhysicalRoutine routine = PsiTreeUtil.getParentOfType(statement, PhysicalRoutine.class);
        assert routine != null;
        RapidType type = routine.getType();
        if (type != null) {
            if (expression == null) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.return.missing"))
                        .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                        .range(statement)
                        .create();
            } else {
                checkCompatibleType(type, expression);
            }
        } else {
            if (expression != null) {
                String method = switch (routine.getAttribute()) {
                    case FUNCTION -> null;
                    case PROCEDURE -> "procedure";
                    case TRAP -> "trap";
                };
                if (method != null) {
                    annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.return.invalid", method))
                            .range(statement)
                            .create();
                }
            }
        }
    }

    @Override
    public void visitRaiseStatement(@NotNull RapidRaiseStatement statement) {
        validateRaiseStatement(statement);
        super.visitRaiseStatement(statement);
    }

    private void validateRaiseStatement(@NotNull RapidRaiseStatement statement) {
        RapidStatementList statementList = getStatementList(statement);
        RapidExpression expression = statement.getExpression();
        checkCompatibleType(RapidType.NUMBER, expression);
        if (expression == null) {
            if (statementList.getAttribute() != RapidStatementList.Attribute.ERROR_CLAUSE) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.raise.invalid"))
                        .range(statement)
                        .create();
            }
        }
    }

    @Override
    public void visitRetryStatement(@NotNull RapidRetryStatement statement) {
        validateRetryStatement(statement);
        super.visitRetryStatement(statement);
    }

    private void validateRetryStatement(@NotNull RapidRetryStatement statement) {
        RapidStatementList statementList = getStatementList(statement);
        if (statementList.getAttribute() != RapidStatementList.Attribute.ERROR_CLAUSE) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.retry.invalid"))
                    .range(statement)
                    .create();
        }
    }

    @Override
    public void visitTryNextStatement(@NotNull RapidTryNextStatement statement) {
        validateTryNextStatement(statement);
        super.visitTryNextStatement(statement);
    }

    private void validateTryNextStatement(@NotNull RapidTryNextStatement statement) {
        RapidStatementList statementList = getStatementList(statement);
        if (statementList.getAttribute() != RapidStatementList.Attribute.ERROR_CLAUSE) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.trynext.invalid"))
                    .range(statement)
                    .create();
        }
    }

    @Override
    public void visitConnectStatement(@NotNull RapidConnectStatement statement) {
        validateConnectStatement(statement);
        super.visitConnectStatement(statement);
    }

    private void validateConnectStatement(@NotNull RapidConnectStatement statement) {
        validateLeftConnect(statement);
        validateRightConnect(statement);
    }

    private void validateLeftConnect(@NotNull RapidConnectStatement statement) {
        RapidExpression left = statement.getLeft();
        if (left != null) {
            checkCompatibleType(RapidType.NUMBER, left);
        }
    }

    private void validateRightConnect(@NotNull RapidConnectStatement statement) {
        RapidExpression right = statement.getRight();
        if (right != null) {
            if (right instanceof RapidReferenceExpression referenceExpression) {
                RapidSymbol symbol = referenceExpression.getSymbol();
                if (!(symbol instanceof RapidRoutine routine) || routine.getAttribute() != RapidRoutine.Attribute.TRAP) {
                    annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.connect.target.invalid"))
                            .range(right)
                            .create();
                }
            } else {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.connect.target.invalid"))
                        .range(right)
                        .create();
            }
        }
    }

    @Override
    public void visitIfStatement(@NotNull RapidIfStatement statement) {
        validateIfStatement(statement);
        super.visitIfStatement(statement);
    }

    private void validateIfStatement(@NotNull RapidIfStatement statement) {
        checkCompatibleType(RapidType.BOOLEAN, statement.getCondition());
    }

    @Override
    public void visitWhileStatement(@NotNull RapidWhileStatement statement) {
        validateWhileStatement(statement);
        super.visitWhileStatement(statement);
    }

    private void validateWhileStatement(@NotNull RapidWhileStatement statement) {
        checkCompatibleType(RapidType.BOOLEAN, statement.getCondition());
    }

    @Override
    public void visitTestStatement(@NotNull RapidTestStatement statement) {
        validateTestStatement(statement);
        super.visitTestStatement(statement);
    }

    private void validateTestStatement(@NotNull RapidTestStatement statement) {
        RapidExpression expression = statement.getExpression();
        if (expression != null) {
            RapidType type = expression.getType();
            if (type != null) {
                for (RapidTestCaseStatement testCaseStatement : statement.getTestCaseStatements()) {
                    if (testCaseStatement.getExpressions() != null) {
                        for (RapidExpression caseStatementExpression : testCaseStatement.getExpressions()) {
                            RapidType rightType = caseStatementExpression.getType();
                            if (rightType != null && !(type.isAssignable(rightType))) {
                                createIncompatibleType(type, rightType, caseStatementExpression.getTextRange());
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void visitForStatement(@NotNull RapidForStatement statement) {
        validateForStatement(statement);
        super.visitForStatement(statement);
    }

    private void validateForStatement(@NotNull RapidForStatement statement) {
        checkCompatibleType(RapidType.NUMBER, statement.getFromExpression());
        checkCompatibleType(RapidType.NUMBER, statement.getToExpression());
        checkCompatibleType(RapidType.NUMBER, statement.getFromExpression());
    }

    private @NotNull RapidStatementList getStatementList(@NotNull RapidStatement statement) {
        RapidStatementList statementList = null;
        for (PsiElement element = statement.getParent(); element != null; element = element.getParent()) {
            if (element instanceof RapidStatementList) {
                statementList = (RapidStatementList) element;
            }
            if (element instanceof RapidRoutine) {
                return Objects.requireNonNull(statementList);
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void visitAssignmentStatement(@NotNull RapidAssignmentStatement statement) {
        RapidExpression left = statement.getLeft();
        RapidExpression right = statement.getRight();
        if (left != null && right != null) {
            RapidType leftType = left.getType();
            RapidType rightType = right.getType();
            if (leftType != null && rightType != null) {
                if (!(leftType.isAssignable(rightType))) {
                    createIncompatibleType(leftType, rightType, right.getTextRange());
                }
            }
        }
        super.visitAssignmentStatement(statement);
    }

    @Override
    public void visitLiteralExpression(@NotNull RapidLiteralExpression expression) {
        RapidType type = expression.getType();
        if (type != null && RapidType.NUMBER.isAssignable(type)) {
            validateNumericLiteral(expression);
        }
        if (type != null && RapidType.STRING.isAssignable(type)) {
            validateStringLiteral(expression);
        }
        super.visitLiteralExpression(expression);
    }

    private void validateNumericLiteral(@NotNull RapidLiteralExpression expression) {
        RapidType type = expression.getType();
        assert type != null && RapidType.NUMBER.isAssignable(type);
        if (expression.getValue() == null) {
            // If the value of the literal expression is null, its value could not be evaluated.
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.literal.numerical.size"))
                    .range(expression)
                    .create();
        }
    }

    private void validateStringLiteral(@NotNull RapidLiteralExpression expression) {
        RapidType type = expression.getType();
        assert type != null && RapidType.STRING.isAssignable(type);
        String value = (String) expression.getValue();
        assert value != null;
        if (value.length() > 80) {
            // RAPID specification (section 2.13)
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.string.length"))
                    .range(expression)
                    .create();
        }
    }

    @Override
    public void visitArray(@NotNull RapidArray array) {
        for (RapidExpression expression : array.getDimensions()) {
            checkCompatibleType(RapidType.NUMBER, expression);
        }
        super.visitArray(array);
    }

    @Override
    public void visitElement(@NotNull PsiElement element) {
        if (element.getNode().getElementType() == RapidTokenTypes.IDENTIFIER) {
            validateIdentifier(element);
        }
        if (element.getNode().getElementType() == RapidTokenTypes.COMMENT) {
            validateComment(element);
        }
        super.visitElement(element);
    }

    private void validateIdentifier(@NotNull PsiElement element) {
        assert element.getNode().getElementType() == RapidTokenTypes.IDENTIFIER;
        if (element.getText().length() > 32) {
            // RAPID specification (section 2.3)
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.identifier.length"))
                    .range(element)
                    .create();
        }
    }

    private void validateComment(@NotNull PsiElement element) {
        assert element.getNode().getElementType() == RapidTokenTypes.COMMENT;
        /*
         * According to RAPID specification (section 2.10), comments are restricted to certain scenarios.
         * For example, comments cannot appear as the first element in a record declaration.
         * However, comments in RobotStudio are not restricted, and the task is runnable with
         * a comment as the first element of a record declaration. As such, comments are as of
         * now not currently validated.
         */
    }

    private void checkCompatibleType(@NotNull RapidType expected, @Nullable RapidExpression expression) {
        if (expression != null) {
            RapidType type = expression.getType();
            if (type != null && !(expected.isAssignable(type))) {
                createIncompatibleType(expected, type, expression.getTextRange());
            }
        }
    }

    private void createIncompatibleType(@NotNull RapidType left, @NotNull RapidType right, @NotNull TextRange range) {
        String leftPresentableText = left.getPresentableText();
        String rightPresentableText = right.getPresentableText();
        String description = RapidBundle.message("annotation.description.incompatible.types", leftPresentableText, rightPresentableText);
        String tooltip = RapidBundle.message("annotation.tooltip.incompatible.types", leftPresentableText, rightPresentableText, "#" + ColorUtil.toHex(UIUtil.getContextHelpForeground()));
        annotationHolder.newAnnotation(HighlightSeverity.ERROR, description)
                .tooltip(tooltip)
                .range(range)
                .create();
    }
}
