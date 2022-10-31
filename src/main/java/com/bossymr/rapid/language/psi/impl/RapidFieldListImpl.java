package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidFieldList;
import com.bossymr.rapid.language.symbol.RapidField;
import com.bossymr.rapid.language.symbol.physical.PhysicalField;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RapidFieldListImpl extends RapidCompositeElement implements RapidFieldList {

    public RapidFieldListImpl() {
        super(RapidElementTypes.FIELD_LIST);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitFieldList(this);
    }


    @Override
    public List<RapidField> getFields() {
        return List.of(getChildrenAsPsiElements(RapidElementTypes.FIELD, PhysicalField[]::new));
    }
}
