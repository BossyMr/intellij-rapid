package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidFieldList;
import com.bossymr.rapid.language.symbol.RapidField;
import com.bossymr.rapid.language.symbol.physical.PhysicalField;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RapidFieldListImpl extends RapidElementImpl implements RapidFieldList {

    public RapidFieldListImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitFieldList(this);
    }


    @Override
    public List<RapidField> getFields() {
        return List.of(findChildrenByClass(PhysicalField.class));
    }

    @Override
    public String toString() {
        return "RapidFieldList";
    }
}
