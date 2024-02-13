package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.RapidArgument;
import com.bossymr.rapid.language.psi.RapidArgumentList;
import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RapidArgumentListImpl extends RapidElementListImpl implements RapidArgumentList {

    public RapidArgumentListImpl(@NotNull ASTNode node) {
        super(node, RapidElementTypes.ARGUMENTS);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitArgumentList(this);
    }

    @Override
    public List<RapidArgument> getArguments() {
        return findChildrenByType(RapidElementTypes.ARGUMENTS);
    }

    @Override
    public String toString() {
        return "RapidArgumentList:" + getText();
    }
}
