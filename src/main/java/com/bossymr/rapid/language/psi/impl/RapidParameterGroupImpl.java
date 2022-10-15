package com.bossymr.rapid.language.psi.impl;

import com.intellij.lang.ASTNode;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.stubs.RapidParameterGroupStub;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RapidParameterGroupImpl extends RapidStubElement<RapidParameterGroupStub> implements RapidParameterGroup {

    public RapidParameterGroupImpl(@NotNull RapidParameterGroupStub stub) {
        super(stub, RapidStubElementTypes.PARAMETER_GROUP);
    }

    public RapidParameterGroupImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitParameterGroup(this);
    }

    @Override
    public boolean isOptional() {
        RapidParameterGroupStub stub = getGreenStub();
        if (stub != null) {
            return stub.isOptional();
        } else {
            return findChildByType(RapidTokenTypes.BACKSLASH) != null;
        }
    }

    @Override
    public @NotNull List<RapidParameter> getParameters() {
        return List.of(getStubOrPsiChildren(RapidStubElementTypes.PARAMETER, new RapidParameter[0]));
    }

    @Override
    public String toString() {
        return "RapidParameterGroup:" + getText();
    }
}
