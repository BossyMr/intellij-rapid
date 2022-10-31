package com.bossymr.rapid.language.psi.stubs.node;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.tree.CompositeElement;

public class RapidRoutineElement extends CompositeElement {

    public RapidRoutineElement() {
        super(RapidElementTypes.ROUTINE);
    }

    @Override
    public int getTextOffset() {
        ASTNode name = findChildByType(RapidTokenTypes.IDENTIFIER);
        return name != null ? name.getStartOffset() : super.getTextOffset();
    }
}
