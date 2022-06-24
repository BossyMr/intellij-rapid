package io.github.bossymr.language.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import io.github.bossymr.language.psi.RapidElement;
import org.jetbrains.annotations.NotNull;

public class RapidElementImpl extends ASTWrapperPsiElement implements RapidElement {

    public RapidElementImpl(@NotNull ASTNode node) {
        super(node);
    }
}
