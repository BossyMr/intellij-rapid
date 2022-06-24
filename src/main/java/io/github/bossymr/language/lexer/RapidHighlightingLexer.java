package io.github.bossymr.language.lexer;

import com.intellij.lexer.LayeredLexer;
import io.github.bossymr.language.psi.RapidElementTypes;

/**
 * Lexer used to perform syntax highlighting, and uses a combination of a regular {@link RapidLexer} and
 * {@link RapidStringLexer} to highlight valid and invalid escaped characters.
 */
public class RapidHighlightingLexer extends LayeredLexer {
    public RapidHighlightingLexer() {
        super(new RapidLexer());
        registerLayer(new RapidStringLexer(), RapidElementTypes.STRING_LITERAL);
    }
}
