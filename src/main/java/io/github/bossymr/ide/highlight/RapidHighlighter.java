package io.github.bossymr.ide.highlight;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.StringEscapesTokenTypes;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import io.github.bossymr.language.core.lexer.RapidHighlightingLexer;
import io.github.bossymr.language.core.psi.RapidElementTypes;
import io.github.bossymr.language.core.psi.RapidTokenType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RapidHighlighter extends SyntaxHighlighterBase {

    private static final Map<IElementType, TextAttributesKey> HIGHLIGHTS = new HashMap<>();

    static {
        fillMap(HIGHLIGHTS, RapidTokenType.KEYWORDS, RapidHighlightingColors.KEYWORD);
        fillMap(HIGHLIGHTS, RapidTokenType.LITERALS, RapidHighlightingColors.KEYWORD);
        fillMap(HIGHLIGHTS, RapidTokenType.OPERATIONS, RapidHighlightingColors.OPERATION_SIGN);

        HIGHLIGHTS.put(RapidElementTypes.INTEGER_LITERAL, RapidHighlightingColors.NUMBER);
        HIGHLIGHTS.put(RapidElementTypes.STRING_LITERAL, RapidHighlightingColors.STRING);
        HIGHLIGHTS.put(StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN, RapidHighlightingColors.VALID_STRING_ESCAPE);
        HIGHLIGHTS.put(StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN, RapidHighlightingColors.INVALID_STRING_ESCAPE);
        HIGHLIGHTS.put(StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN, RapidHighlightingColors.INVALID_STRING_ESCAPE);

        HIGHLIGHTS.put(RapidElementTypes.LPARENTH, RapidHighlightingColors.PARENTHESES);
        HIGHLIGHTS.put(RapidElementTypes.RPARENTH, RapidHighlightingColors.PARENTHESES);

        HIGHLIGHTS.put(RapidElementTypes.LBRACE, RapidHighlightingColors.BRACES);
        HIGHLIGHTS.put(RapidElementTypes.RBRACE, RapidHighlightingColors.BRACES);

        HIGHLIGHTS.put(RapidElementTypes.LBRACKET, RapidHighlightingColors.BRACKETS);
        HIGHLIGHTS.put(RapidElementTypes.RBRACKET, RapidHighlightingColors.BRACKETS);

        HIGHLIGHTS.put(RapidElementTypes.COMMA, RapidHighlightingColors.COMMA);
        HIGHLIGHTS.put(RapidElementTypes.DOT, RapidHighlightingColors.DOT);
        HIGHLIGHTS.put(RapidElementTypes.SEMICOLON, RapidHighlightingColors.SEMICOLON);

        HIGHLIGHTS.put(RapidElementTypes.COMMENT, RapidHighlightingColors.COMMENT);
        HIGHLIGHTS.put(TokenType.BAD_CHARACTER, HighlighterColors.BAD_CHARACTER);
    }

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new RapidHighlightingLexer();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        return pack(HIGHLIGHTS.get(tokenType));
    }
}
