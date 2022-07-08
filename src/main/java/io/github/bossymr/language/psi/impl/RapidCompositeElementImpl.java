package io.github.bossymr.language.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import io.github.bossymr.language.psi.RapidElement;
import org.jetbrains.annotations.NotNull;

public abstract class RapidCompositeElementImpl extends ASTWrapperPsiElement implements RapidElement {

    public RapidCompositeElementImpl(@NotNull ASTNode node) {
        super(node);
    }
}
