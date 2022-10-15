package com.bossymr.rapid.language.psi.stubs.node;

import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import org.jetbrains.annotations.Nullable;

public class RapidRecordElement extends CompositeElement {

    public RapidRecordElement() {
        super(RapidElementTypes.RECORD);
    }

    @Override
    public int getTextOffset() {
        ASTNode name = findChildByType(RapidTokenTypes.IDENTIFIER);
        return name != null ? name.getStartOffset() : super.getTextOffset();
    }

    @Override
    public TreeElement addInternal(TreeElement first, ASTNode last, @Nullable ASTNode anchor, @Nullable Boolean before) {
        if (anchor == null) {
            if (before == null || before) {
                anchor = findChildByType(RapidTokenTypes.ENDRECORD_KEYWORD);
                before = true;
            } else {
                anchor = findChildByType(RapidTokenTypes.IDENTIFIER);
                before = false;
            }
        }
        return super.addInternal(first, last, anchor, before);
    }
}
