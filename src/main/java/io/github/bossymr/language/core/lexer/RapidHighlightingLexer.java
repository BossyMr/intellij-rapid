package io.github.bossymr.language.core.lexer;

import com.intellij.lexer.LayeredLexer;
import io.github.bossymr.language.core.psi.RapidElementTypes;

public class RapidHighlightingLexer extends LayeredLexer {
    public RapidHighlightingLexer() {
        super(new RapidLexer());
        registerLayer(new RapidStringLexer(), RapidElementTypes.STRING_LITERAL);
    }
}
