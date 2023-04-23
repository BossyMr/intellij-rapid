package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LightTreeUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public enum ModuleType {
    SYSTEM_MODULE(RapidTokenTypes.SYSMODULE_KEYWORD, "SYSMODULE"),
    NO_VIEW(RapidTokenTypes.NOVIEW_KEYWORD, "NOVIEW"),
    NO_STEP_IN(RapidTokenTypes.NOSTEPIN_KEYWORD, "NOSTEPIN"),
    VIEW_ONLY(RapidTokenTypes.VIEWONLY_KEYWORD, "VIEWONLY"),
    READ_ONLY(RapidTokenTypes.READONLY_KEYWORD, "READONLY");

    public static final Map<ModuleType, List<ModuleType>> MUTUALLY_EXCLUSIVE = Map.of(
            NO_VIEW, List.of(NO_STEP_IN, VIEW_ONLY, READ_ONLY),
            NO_STEP_IN, List.of(NO_VIEW),
            VIEW_ONLY, List.of(NO_VIEW, READ_ONLY),
            READ_ONLY, List.of(NO_VIEW, VIEW_ONLY)
    );


    public static final TokenSet TOKEN_SET = TokenSet.create(RapidTokenTypes.SYSMODULE_KEYWORD, RapidTokenTypes.NOVIEW_KEYWORD, RapidTokenTypes.NOSTEPIN_KEYWORD, RapidTokenTypes.VIEWONLY_KEYWORD, RapidTokenTypes.READONLY_KEYWORD);

    private final IElementType elementType;
    private final String text;

    ModuleType(@NotNull IElementType elementType, @NotNull String text) {
        this.elementType = elementType;
        this.text = text;
    }

    public static @NotNull List<ModuleType> getAttributes(@NotNull PsiElement element) {
        ASTNode[] nodes = element.getNode().getChildren(TOKEN_SET);
        List<ModuleType> moduleTypes = new ArrayList<>();
        for (ASTNode node : nodes) {
            IElementType elementType = node.getElementType();
            moduleTypes.add(getAttribute(elementType));
        }
        return moduleTypes;
    }

    public static @NotNull List<ModuleType> getAttributes(@NotNull LighterAST tree, @NotNull LighterASTNode node) {
        List<LighterASTNode> nodes = LightTreeUtil.getChildrenOfType(tree, node, TOKEN_SET);
        List<ModuleType> moduleTypes = new ArrayList<>();
        for (LighterASTNode element : nodes) {
            IElementType elementType = element.getTokenType();
            moduleTypes.add(getAttribute(elementType));
        }
        return moduleTypes;
    }

    public static @NotNull ModuleType getAttribute(@NotNull IElementType elementType) {
        if (elementType == RapidTokenTypes.SYSMODULE_KEYWORD) {
            return ModuleType.SYSTEM_MODULE;
        }
        if (elementType == RapidTokenTypes.NOVIEW_KEYWORD) {
            return ModuleType.NO_VIEW;
        }
        if (elementType == RapidTokenTypes.NOSTEPIN_KEYWORD) {
            return ModuleType.NO_STEP_IN;
        }
        if (elementType == RapidTokenTypes.VIEWONLY_KEYWORD) {
            return ModuleType.VIEW_ONLY;
        }
        if (elementType == RapidTokenTypes.READONLY_KEYWORD) {
            return ModuleType.READ_ONLY;
        }
        throw new AssertionError();
    }

    public @NotNull IElementType getElementType() {
        return elementType;
    }

    public @NotNull String getText() {
        return text;
    }
}
