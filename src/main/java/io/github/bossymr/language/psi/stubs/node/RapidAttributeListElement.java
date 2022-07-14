package io.github.bossymr.language.psi.stubs.node;

import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.tree.TokenSet;
import io.github.bossymr.language.psi.RapidElementTypes;
import io.github.bossymr.language.psi.RapidTokenTypes;
import io.github.bossymr.language.psi.impl.RapidElementUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidAttributeListElement extends CompositeElement {

    public RapidAttributeListElement() {
        super(RapidElementTypes.ATTRIBUTE_LIST);
    }

    @Override
    public TreeElement addInternal(TreeElement first, ASTNode last, @Nullable ASTNode anchor, @Nullable Boolean before) {
        if (anchor == null) {
            if (before == null || before) {
                anchor = findChildByType(RapidTokenTypes.RPARENTH);
                before = true;
            } else {
                anchor = findChildByType(RapidTokenTypes.LPARENTH);
                before = false;
            }
        }
        TreeElement treeElement = super.addInternal(first, last, anchor, before);
        if (first == last && first.getElementType().equals(RapidElementTypes.PARAMETER_GROUP)) {
            RapidElementUtil.addSeparatingComma(this, first, TokenSet.create(RapidElementTypes.PARAMETER_GROUP));
        }
        return treeElement;
    }

    @Override
    public void deleteChildInternal(@NotNull ASTNode child) {
        if (child.getElementType().equals(RapidElementTypes.PARAMETER_GROUP)) {
            RapidElementUtil.deleteSeparatingComma(this, child);
            RapidElementUtil.ensureSurroundingParenthesis(this);
        }
        super.deleteChildInternal(child);
    }
}
