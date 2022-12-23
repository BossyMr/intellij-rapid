package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.navigation.TargetPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LightTreeUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public interface RapidModule extends RapidSymbol {

    @NotNull Set<Attribute> getAttributes();

    boolean hasAttribute(@NotNull Attribute attribute);

    default void setAttribute(@NotNull Attribute attribute, boolean setAttribute) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @NotNull List<RapidAccessibleSymbol> getSymbols();

    @NotNull List<RapidStructure> getStructures();

    @NotNull List<RapidField> getFields();

    @NotNull List<RapidRoutine> getRoutines();

    @Override
    default @NotNull TargetPresentation getTargetPresentation() {
        return TargetPresentation.builder(Objects.requireNonNullElse(getName(), ""))
                .icon(hasAttribute(Attribute.SYSTEM_MODULE) ? RapidIcons.SYSTEM_MODULE : RapidIcons.MODULE)
                .presentation();
    }

    enum Attribute {
        SYSTEM_MODULE(RapidTokenTypes.SYSMODULE_KEYWORD, "SYSMODULE"),
        NO_VIEW(RapidTokenTypes.NOVIEW_KEYWORD, "NOVIEW"),
        NO_STEP_IN(RapidTokenTypes.NOSTEPIN_KEYWORD, "NOSTEPIN"),
        VIEW_ONLY(RapidTokenTypes.VIEWONLY_KEYWORD, "VIEWONLY"),
        READ_ONLY(RapidTokenTypes.READONLY_KEYWORD, "READONLY");

        private final IElementType elementType;
        private final String text;

        Attribute(@NotNull IElementType elementType, @NotNull String text) {
            this.elementType = elementType;
            this.text = text;
        }

        public static @NotNull Set<Attribute> getAttributes(@NotNull PsiElement element) {
            TokenSet tokenSet = TokenSet.create(RapidTokenTypes.SYSMODULE_KEYWORD, RapidTokenTypes.NOVIEW_KEYWORD, RapidTokenTypes.NOSTEPIN_KEYWORD, RapidTokenTypes.VIEWONLY_KEYWORD, RapidTokenTypes.READONLY_KEYWORD);
            ASTNode[] nodes = element.getNode().getChildren(tokenSet);
            Set<Attribute> attributes = EnumSet.noneOf(Attribute.class);
            for (ASTNode node : nodes) {
                IElementType elementType = node.getElementType();
                attributes.add(getAttribute(elementType));
            }
            return attributes;
        }

        public static @NotNull Set<Attribute> getAttributes(@NotNull LighterAST tree, @NotNull LighterASTNode node) {
            TokenSet tokenSet = TokenSet.create(RapidTokenTypes.SYSMODULE_KEYWORD, RapidTokenTypes.NOVIEW_KEYWORD, RapidTokenTypes.NOSTEPIN_KEYWORD, RapidTokenTypes.VIEWONLY_KEYWORD, RapidTokenTypes.READONLY_KEYWORD);
            List<LighterASTNode> nodes = LightTreeUtil.getChildrenOfType(tree, node, tokenSet);
            Set<Attribute> attributes = EnumSet.noneOf(Attribute.class);
            for (LighterASTNode element : nodes) {
                IElementType elementType = element.getTokenType();
                attributes.add(getAttribute(elementType));
            }
            return attributes;
        }

        private static @NotNull Attribute getAttribute(@NotNull IElementType elementType) {
            if (elementType == RapidTokenTypes.SYSMODULE_KEYWORD) {
                return Attribute.SYSTEM_MODULE;
            }
            if (elementType == RapidTokenTypes.NOVIEW_KEYWORD) {
                return Attribute.NO_VIEW;
            }
            if (elementType == RapidTokenTypes.NOSTEPIN_KEYWORD) {
                return Attribute.NO_STEP_IN;
            }
            if (elementType == RapidTokenTypes.VIEWONLY_KEYWORD) {
                return Attribute.VIEW_ONLY;
            }
            if (elementType == RapidTokenTypes.READONLY_KEYWORD) {
                return Attribute.READ_ONLY;
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

}