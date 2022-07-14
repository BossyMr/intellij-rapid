package io.github.bossymr.language.psi.stubs.node;

import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.tree.CompositeElement;
import io.github.bossymr.language.psi.RapidElementTypes;
import io.github.bossymr.language.psi.RapidTokenTypes;

public class RapidComponentElement extends CompositeElement {

    public RapidComponentElement() {
        super(RapidElementTypes.COMPONENT);
    }

    @Override
    public int getTextOffset() {
        ASTNode name = findChildByType(RapidTokenTypes.IDENTIFIER);
        return name != null ? name.getStartOffset() : super.getTextOffset();
    }
}
