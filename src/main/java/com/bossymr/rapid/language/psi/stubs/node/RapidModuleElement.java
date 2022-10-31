package com.bossymr.rapid.language.psi.stubs.node;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.Nullable;

public class RapidModuleElement extends CompositeElement {

    public RapidModuleElement() {
        super(RapidElementTypes.MODULE);
    }

    @Override
    public int getTextOffset() {
        ASTNode name = findChildByType(RapidTokenTypes.IDENTIFIER);
        return name != null ? name.getStartOffset() : super.getTextOffset();
    }

    @Override
    public TreeElement addInternal(TreeElement first, ASTNode last, @Nullable ASTNode anchor, @Nullable Boolean before) {
        if (anchor == null) {
            if (first != last) {
                anchor = findChildByType(RapidTokenTypes.ENDMODULE_KEYWORD);
                before = true;
            }
            IElementType elementType = first.getElementType();
            if (TokenSet.create(RapidElementTypes.ALIAS, RapidElementTypes.RECORD).contains(elementType)) {
                if (before == null || before) {
                    anchor = findChildByType(TokenSet.create(RapidElementTypes.FIELD, RapidElementTypes.ROUTINE, RapidTokenTypes.ENDMODULE_KEYWORD));
                } else {
                    anchor = findChildByType(TokenSet.create(RapidElementTypes.ALIAS, RapidElementTypes.RECORD, RapidElementTypes.FIELD, RapidElementTypes.ROUTINE, RapidTokenTypes.ENDMODULE_KEYWORD));
                }
                before = true;
            } else if (TokenSet.create(RapidElementTypes.FIELD).contains(elementType)) {
                if (before == null || before) {
                    anchor = findChildByType(TokenSet.create(RapidElementTypes.ROUTINE, RapidTokenTypes.ENDMODULE_KEYWORD));
                } else {
                    anchor = findChildByType(TokenSet.create(RapidElementTypes.FIELD, RapidElementTypes.ROUTINE, RapidTokenTypes.ENDMODULE_KEYWORD));
                }
                before = true;
            } else if (TokenSet.create(RapidElementTypes.ROUTINE).contains(elementType)) {
                if (before == null || before) {
                    anchor = findChildByType(RapidTokenTypes.ENDMODULE_KEYWORD);
                } else {
                    anchor = findChildByType(TokenSet.create(RapidElementTypes.ROUTINE, RapidTokenTypes.ENDMODULE_KEYWORD));
                }
                before = true;
            }
        }
        return super.addInternal(first, last, anchor, before);
    }
}
