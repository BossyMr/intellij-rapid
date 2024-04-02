package com.bossymr.rapid.ide.editor.inlay;

import com.bossymr.rapid.language.psi.RapidArgument;
import com.bossymr.rapid.language.psi.RapidReferenceExpression;
import com.bossymr.rapid.language.symbol.RapidParameter;
import com.intellij.codeInsight.hints.declarative.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidInlayParameterProvider implements InlayHintsProvider {

    @Override
    public @Nullable InlayHintsCollector createCollector(@NotNull PsiFile file, @NotNull Editor editor) {
        return new Collector();
    }

    private static class Collector implements SharedBypassCollector {
        @Override
        public void collectFromElement(@NotNull PsiElement element, @NotNull InlayTreeSink sink) {
            if (!(element instanceof RapidArgument argument)) {
                return;
            }
            if (argument.getParameter() != null) {
                // The argument already mentions the parameter name.
                return;
            }
            RapidParameter parameter = argument.getSymbol();
            if(parameter == null) {
                // The argument doesn't resolve to a parameter.
                return;
            }
            String name = parameter.getName();
            if(name == null) {
                // The parameter doesn't have a valid name to display.
                return;
            }
            int startOffset = argument.getTextRange().getStartOffset();
            InlineInlayPosition inlayPosition = new InlineInlayPosition(startOffset, false, 0);
            sink.addPresentation(inlayPosition, null, null, true, builder -> {
                builder.text(name + ": ", null);
                return null;
            });
        }
    }
}
