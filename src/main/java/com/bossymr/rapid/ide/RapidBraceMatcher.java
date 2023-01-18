package com.bossymr.rapid.ide;

import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidBraceMatcher implements PairedBraceMatcher {

    private static final BracePair[] BRACE_PAIRS = new BracePair[]{
            new BracePair(RapidTokenTypes.LPARENTH, RapidTokenTypes.RPARENTH, false),
            new BracePair(RapidTokenTypes.LBRACE, RapidTokenTypes.RBRACE, false),
            new BracePair(RapidTokenTypes.LBRACKET, RapidTokenTypes.RPARENTH, false),
            new BracePair(RapidTokenTypes.LBRACKET, RapidTokenTypes.RBRACKET, false),
            new BracePair(RapidTokenTypes.THEN_KEYWORD, RapidTokenTypes.ELSE_KEYWORD, true),
            new BracePair(RapidTokenTypes.THEN_KEYWORD, RapidTokenTypes.ENDIF_KEYWORD, true),
            new BracePair(RapidTokenTypes.DO_KEYWORD, RapidTokenTypes.ENDFOR_KEYWORD, true),
            new BracePair(RapidTokenTypes.DO_KEYWORD, RapidTokenTypes.ENDWHILE_KEYWORD, true),
    };

    @Override
    public BracePair @NotNull [] getPairs() {
        return BRACE_PAIRS;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
        return true;
    }

    @Override
    public int getCodeConstructStart(@NotNull PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }
}
