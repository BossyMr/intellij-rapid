package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.psi.RapidElement;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.impl.RapidStubElement;
import com.bossymr.rapid.language.psi.stubs.RapidParameterGroupStub;
import com.bossymr.rapid.language.symbol.RapidParameter;
import com.bossymr.rapid.language.symbol.RapidParameterGroup;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PhysicalParameterGroup extends RapidStubElement<RapidParameterGroupStub> implements RapidParameterGroup, RapidElement {

    public PhysicalParameterGroup(@NotNull RapidParameterGroupStub stub) {
        super(stub, RapidStubElementTypes.PARAMETER_GROUP);
    }

    public PhysicalParameterGroup(@NotNull ASTNode node) {
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
        return List.of(getStubOrPsiChildren(RapidStubElementTypes.PARAMETER, new PhysicalParameter[0]));
    }

    @Override
    public String toString() {
        return "PhysicalParameterGroup";
    }
}
