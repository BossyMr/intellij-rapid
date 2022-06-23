package io.github.bossymr.ide.highlight;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.StringEscapesTokenTypes;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import io.github.bossymr.language.core.lexer.RapidHighlightingLexer;
import io.github.bossymr.language.core.psi.RapidElementTypes;
import io.github.bossymr.language.core.psi.RapidTokenType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RapidHighlighter extends SyntaxHighlighterBase {

    private static final Map<IElementType, RapidColor> HIGHLIGHTS = new HashMap<>();

    static {
        fillMap(RapidTokenType.KEYWORDS, RapidColor.KEYWORD);
        fillMap(RapidTokenType.LITERALS, RapidColor.KEYWORD);
        fillMap(RapidTokenType.OPERATIONS, RapidColor.OPERATION_SIGN);

        HIGHLIGHTS.put(RapidElementTypes.INTEGER_LITERAL, RapidColor.NUMBER);
        HIGHLIGHTS.put(RapidElementTypes.STRING_LITERAL, RapidColor.STRING);
        HIGHLIGHTS.put(StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN, RapidColor.VALID_STRING_ESCAPE);
        HIGHLIGHTS.put(StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN, RapidColor.INVALID_STRING_ESCAPE);
        HIGHLIGHTS.put(StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN, RapidColor.INVALID_STRING_ESCAPE);

        HIGHLIGHTS.put(RapidElementTypes.LPARENTH, RapidColor.PARENTHESES);
        HIGHLIGHTS.put(RapidElementTypes.RPARENTH, RapidColor.PARENTHESES);

        HIGHLIGHTS.put(RapidElementTypes.LBRACE, RapidColor.BRACES);
        HIGHLIGHTS.put(RapidElementTypes.RBRACE, RapidColor.BRACES);

        HIGHLIGHTS.put(RapidElementTypes.LBRACKET, RapidColor.BRACKETS);
        HIGHLIGHTS.put(RapidElementTypes.RBRACKET, RapidColor.BRACKETS);

        HIGHLIGHTS.put(RapidElementTypes.COMMA, RapidColor.COMMA);
        HIGHLIGHTS.put(RapidElementTypes.DOT, RapidColor.DOT);
        HIGHLIGHTS.put(RapidElementTypes.SEMICOLON, RapidColor.SEMICOLON);

        HIGHLIGHTS.put(RapidElementTypes.COMMENT, RapidColor.COMMENT);
    }

    private static void fillMap(TokenSet tokenSet, RapidColor color) {
        for (IElementType elementType : tokenSet.getTypes()) {
            HIGHLIGHTS.put(elementType, color);
        }
    }

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new RapidHighlightingLexer();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        TextAttributesKey textAttributesKey = HIGHLIGHTS.containsKey(tokenType) ? HIGHLIGHTS.get(tokenType).textAttributesKey : null;
        return pack(textAttributesKey);
    }
}
