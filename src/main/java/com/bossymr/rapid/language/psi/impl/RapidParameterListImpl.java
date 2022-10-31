package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidParameterList;
import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.psi.stubs.RapidParameterListStub;
import com.bossymr.rapid.language.symbol.RapidParameterGroup;
import com.bossymr.rapid.language.symbol.physical.PhysicalParameterGroup;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RapidParameterListImpl extends RapidStubElement<RapidParameterListStub> implements RapidParameterList {
    public RapidParameterListImpl(@NotNull RapidParameterListStub stub) {
        super(stub, RapidStubElementTypes.PARAMETER_LIST);
    }

    public RapidParameterListImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitParameterList(this);
    }

    @Override
    public @NotNull List<RapidParameterGroup> getParameters() {
        return List.of(getStubOrPsiChildren(RapidStubElementTypes.PARAMETER_GROUP, new PhysicalParameterGroup[0]));
    }

    @Override
    public String toString() {
        return "RapidParameterList:" + getText();
    }
}
