package com.bossymr.rapid.ide.insight;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalField;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.lang.annotation.AnnotationHolder;
import org.jetbrains.annotations.NotNull;

public class RapidAnnotatorVisitor extends RapidElementVisitor {

    private final RapidValidator validator;

    public RapidAnnotatorVisitor(@NotNull AnnotationHolder annotationHolder) {
        this.validator = new RapidValidator(annotationHolder);
    }

    @Override
    public void visitModule(@NotNull PhysicalModule module) {
        validator.checkModuleFile(module);
        super.visitModule(module);
    }

    @Override
    public void visitAttributeList(@NotNull RapidAttributeList attributeList) {
        validator.checkAttributeList(attributeList);
        super.visitAttributeList(attributeList);
    }

    @Override
    public void visitSymbol(@NotNull PhysicalSymbol symbol) {
        validator.checkDuplicateSymbol(symbol);
        super.visitSymbol(symbol);
    }

    @Override
    public void visitField(@NotNull PhysicalField field) {
        validator.checkInitializer(field);
        validator.checkDimensions(field);
        super.visitField(field);
    }

    @Override
    public void visitAssignmentStatement(@NotNull RapidAssignmentStatement statement) {
        validator.checkStatementType(statement);
        super.visitAssignmentStatement(statement);
    }

    @Override
    public void visitReferenceExpression(@NotNull RapidReferenceExpression expression) {
        validator.checkReferenceExpression(expression);
        super.visitReferenceExpression(expression);
    }

    @Override
    public void visitAggregateExpression(@NotNull RapidAggregateExpression expression) {
        validator.checkAggregateExpression(expression);
        super.visitAggregateExpression(expression);
    }
}