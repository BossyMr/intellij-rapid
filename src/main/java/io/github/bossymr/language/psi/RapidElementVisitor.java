package io.github.bossymr.language.psi;

import com.intellij.psi.PsiElementVisitor;

public abstract class RapidElementVisitor extends PsiElementVisitor {

    public void visitModule(RapidModule module) {
        visitSymbol(module);
    }

    public void visitAlias(RapidAlias alias) {
        visitStructure(alias);
    }

    public void visitRecord(RapidRecord record) {
        visitStructure(record);
    }

    public void visitComponent(RapidComponent component) {
        visitSymbol(component);
    }

    public void visitStructure(RapidStructure structure) {
        visitSymbol(structure);
    }

    public void visitField(RapidField field) {
        visitSymbol(field);
    }

    public void visitRoutine(RapidRoutine routine) {
        visitSymbol(routine);
    }

    public void visitSymbol(RapidSymbol symbol) {
        visitElement(symbol);
    }

    public void visitTypeElement(RapidTypeElement typeElement) {
        visitElement(typeElement);
    }

    public void visitAttributeList(RapidAttributeList attributeList) {
        visitElement(attributeList);
    }
}
