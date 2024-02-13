package com.bossymr.rapid.language.psi.stubs;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.impl.source.tree.LightTreeUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class StubUtil {

    private StubUtil() {}

    public static @Nullable String getText(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull IElementType elementType) {
        return getText(tree, node, TokenSet.create(elementType));
    }

    public static @Nullable String getText(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull TokenSet tokenSet) {
        LighterASTNode element = LightTreeUtil.firstChildOfType(tree, node, tokenSet);
        return getText(tree, element);
    }

    public static @Nullable String getText(@NotNull LighterAST tree, @Nullable LighterASTNode node) {
        if (node == null) {
            return null;
        }
        return LightTreeUtil.toFilteredString(tree, node, null);
    }

    public static boolean hasChild(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull IElementType elementType) {
        return hasChild(tree, node, TokenSet.create(elementType));
    }

    public static boolean hasChild(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull TokenSet tokenSet) {
        return LightTreeUtil.firstChildOfType(tree, node, tokenSet) != null;
    }

    public static int getLength(@NotNull LighterAST tree, @NotNull LighterASTNode node) {
        LighterASTNode array = LightTreeUtil.firstChildOfType(tree, node, RapidElementTypes.ARRAY);
        if (array == null) {
            return 0;
        }
        List<LighterASTNode> expressions = LightTreeUtil.getChildrenOfType(tree, node, RapidElementTypes.EXPRESSIONS);
        return expressions.size();
    }
}
