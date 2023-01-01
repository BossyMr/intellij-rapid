package com.bossymr.rapid.ide.insight.quickfix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.symbol.RapidAccessibleSymbol;
import com.bossymr.rapid.language.symbol.Visibility;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.codeInsight.intention.FileModifier;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.Factory;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class VisibilityFix implements IntentionAction {

    private final PhysicalSymbol symbol;
    private final Visibility visibility;

    public VisibilityFix(@NotNull PhysicalSymbol symbol, @NotNull Visibility visibility) {
        this.symbol = symbol;
        this.visibility = visibility;
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return RapidBundle.message("quick.fix.text.change.visibility", symbol.getName(), visibility.getName());
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.family.change.visibility");
    }

    @Override
    public @NotNull FileModifier getFileModifierForPreview(@NotNull PsiFile target) {
        return new VisibilityFix(PsiTreeUtil.findSameElementInCopy(symbol, target), visibility);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        if (symbol instanceof RapidAccessibleSymbol accessibleSymbol) {
            ASTNode node = getVisibility(symbol);
            ASTNode element = createVisibility(visibility);
            if (node != null) {
                // Replace or remove visibility
                if (element != null) {
                    symbol.getNode().replaceChild(node, element);
                } else {
                    symbol.getNode().removeChild(node);
                }
            } else {
                if (element != null) {
                    symbol.getNode().addChild(element, symbol.getNode().getFirstChildNode());
                }
            }
        }
    }

    private @Nullable ASTNode getVisibility(@NotNull PhysicalSymbol symbol) {
        ASTNode[] nodes = symbol.getNode().getChildren(Visibility.TOKEN_SET);
        return nodes.length > 0 ? nodes[0] : null;
    }

    private @Nullable ASTNode createVisibility(@NotNull Visibility visibility) {
        IElementType elementType = visibility.getElementType();
        String text = visibility.getText();
        if (elementType == null || text == null) return null;
        return Factory.createSingleLeafElement(visibility.getElementType(), visibility.getText(), null, symbol.getManager());
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        if (!(symbol.isValid()) || !(BaseIntentionAction.canModify(symbol))) return false;
        if (!(symbol instanceof RapidAccessibleSymbol accessibleSymbol)) return false;
        return accessibleSymbol.getVisibility() != visibility;
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
