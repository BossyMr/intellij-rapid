package com.bossymr.rapid.language.psi.stubs.node;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.tree.CompositeElement;
import org.jetbrains.annotations.NotNull;

public class RapidFieldElement extends CompositeElement {

    public RapidFieldElement() {
        super(RapidElementTypes.FIELD);
    }

    @Override
    public int getTextOffset() {
        ASTNode name = findChildByType(RapidTokenTypes.IDENTIFIER);
        return name != null ? name.getStartOffset() : super.getTextOffset();
    }

    @Override
    public void deleteChildInternal(@NotNull ASTNode child) {
        if (RapidElementTypes.EXPRESSIONS.contains(child.getElementType())) {
            ASTNode equals = findChildByType(RapidTokenTypes.CEQ);
            if (equals != null) {
                deleteChildInternal(equals);
            }
        }
        super.deleteChildInternal(child);
    }
}
