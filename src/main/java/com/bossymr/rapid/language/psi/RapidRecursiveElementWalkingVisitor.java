package com.bossymr.rapid.language.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveVisitor;
import com.intellij.psi.PsiWalkingState;
import org.jetbrains.annotations.NotNull;

public class RapidRecursiveElementWalkingVisitor extends RapidElementVisitor implements PsiRecursiveVisitor {

    private final PsiWalkingState walkingState = new PsiWalkingState(this) {
        @Override
        public void elementFinished(@NotNull PsiElement element) {
            RapidRecursiveElementWalkingVisitor.this.elementFinished(element);
        }
    };

    @Override
    public void visitElement(@NotNull PsiElement element) {
        walkingState.elementStarted(element);
    }

    protected void elementFinished(@NotNull PsiElement element) {}

    public void stopWalking() {
        walkingState.stopWalking();
    }

}
