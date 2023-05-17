package com.bossymr.rapid.ide.highlight;

import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidReferenceExpression;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.*;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class RapidHighlighterVisitor extends RapidElementVisitor {

    private final @NotNull AnnotationHolder annotationHolder;

    public RapidHighlighterVisitor(@NotNull AnnotationHolder annotationHolder) {
        this.annotationHolder = annotationHolder;
    }

    @Override
    public void visitReferenceExpression(@NotNull RapidReferenceExpression expression) {
        PsiElement identifier = expression.getIdentifier();
        if (identifier != null) {
            RapidSymbol symbol = expression.getSymbol();
            if (symbol instanceof RapidModule module) {
                visitModuleSymbol(identifier, module);
            }
            if (symbol instanceof RapidAtomic atomic) {
                visitAtomicSymbol(identifier, atomic);
            }
            if (symbol instanceof RapidAlias alias) {
                visitAliasSymbol(identifier, alias);
            }
            if (symbol instanceof RapidRecord record) {
                visitRecordSymbol(identifier, record);
            }
            if (symbol instanceof RapidComponent component) {
                visitComponentSymbol(identifier, component);
            }
            if (symbol instanceof RapidField field) {
                visitFieldSymbol(identifier, field);
            }
            if (symbol instanceof RapidRoutine routine) {
                visitRoutineSymbol(identifier, routine);
            }
            if (symbol instanceof RapidParameter parameter) {
                visitParameterSymbol(identifier, parameter);
            }
        }
        super.visitReferenceExpression(expression);
    }

    @Override
    public void visitModule(@NotNull PhysicalModule module) {
        visitPhysicalSymbol(module, this::visitModuleSymbol);
        super.visitModule(module);
    }

    @Override
    public void visitAlias(@NotNull PhysicalAlias alias) {
        visitPhysicalSymbol(alias, this::visitAliasSymbol);
        super.visitAlias(alias);
    }

    @Override
    public void visitRecord(@NotNull PhysicalRecord record) {
        visitPhysicalSymbol(record, this::visitRecordSymbol);
        super.visitRecord(record);
    }

    @Override
    public void visitComponent(@NotNull PhysicalComponent component) {
        visitPhysicalSymbol(component, this::visitComponentSymbol);
        super.visitComponent(component);
    }

    @Override
    public void visitField(@NotNull PhysicalField field) {
        visitPhysicalSymbol(field, this::visitFieldSymbol);
        super.visitField(field);
    }

    @Override
    public void visitRoutine(@NotNull PhysicalRoutine routine) {
        visitPhysicalSymbol(routine, this::visitRoutineSymbol);
        super.visitRoutine(routine);
    }

    @Override
    public void visitParameter(@NotNull PhysicalParameter parameter) {
        visitPhysicalSymbol(parameter, this::visitParameterSymbol);
        super.visitParameter(parameter);
    }

    public void visitModuleSymbol(@NotNull PsiElement identifier, @NotNull RapidModule module) {
        if (module.hasAttribute(ModuleType.SYSTEM_MODULE)) {
            annotate(identifier, RapidColor.SYSTEM_MODULE);
        } else {
            annotate(identifier, RapidColor.MODULE);
        }
    }

    public void visitAtomicSymbol(@NotNull PsiElement identifier, @NotNull RapidAtomic atomic) {
        annotate(identifier, RapidColor.ATOMIC);
    }

    public void visitAliasSymbol(@NotNull PsiElement identifier, @NotNull RapidAlias alias) {
        annotate(identifier, RapidColor.ALIAS);
    }

    public void visitRecordSymbol(@NotNull PsiElement identifier, @NotNull RapidRecord record) {
        annotate(identifier, RapidColor.RECORD);
    }

    public void visitComponentSymbol(@NotNull PsiElement identifier, @NotNull RapidComponent component) {
        annotate(identifier, RapidColor.COMPONENT);
    }

    public void visitFieldSymbol(@NotNull PsiElement identifier, @NotNull RapidField field) {
        annotate(identifier, switch (field.getFieldType()) {
            case VARIABLE -> RapidColor.VARIABLE;
            case CONSTANT -> RapidColor.CONSTANT;
            case PERSISTENT -> RapidColor.PERSISTENT;
        });
    }

    public void visitRoutineSymbol(@NotNull PsiElement identifier, @NotNull RapidRoutine routine) {
        annotate(identifier, switch (routine.getRoutineType()) {
            case FUNCTION -> RapidColor.FUNCTION;
            case PROCEDURE -> RapidColor.PROCEDURE;
            case TRAP -> RapidColor.TRAP;
        });
    }

    public void visitParameterSymbol(@NotNull PsiElement identifier, @NotNull RapidParameter parameter) {
        annotate(identifier, parameter.getParameterGroup().isOptional() ? RapidColor.OPTIONAL_PARAMETER : RapidColor.PARAMETER);
    }

    private <T extends PhysicalSymbol> void visitPhysicalSymbol(@NotNull T symbol, @NotNull BiConsumer<PsiElement, ? super T> consumer) {
        PsiElement nameIdentifier = symbol.getNameIdentifier();
        if (nameIdentifier == null) {
            return;
        }
        consumer.accept(nameIdentifier, symbol);
    }

    private void annotate(@NotNull PsiElement identifier, @NotNull RapidColor color) {
        annotationHolder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                .range(identifier)
                .textAttributes(color.textAttributesKey())
                .create();
    }
}
