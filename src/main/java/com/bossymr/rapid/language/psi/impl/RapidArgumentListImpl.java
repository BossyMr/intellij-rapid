package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.RapidArgument;
import com.bossymr.rapid.language.psi.RapidArgumentList;
import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RapidArgumentListImpl extends RapidArgumentListElement implements RapidArgumentList {

    public RapidArgumentListImpl() {
        super(RapidElementTypes.ARGUMENT_LIST, RapidElementTypes.ARGUMENTS);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof RapidElementVisitor) {
            ((RapidElementVisitor) visitor).visitArgumentList(this);
        } else {
            visitor.visitElement(this);
        }
    }

    @Override
    public List<RapidArgument> getArguments() {
        return List.of(getChildrenAsPsiElements(RapidElementTypes.ARGUMENTS, RapidArgument[]::new));
    }

    @Override
    public String toString() {
        return "RapidArgumentList:" + getText();
    }
}
