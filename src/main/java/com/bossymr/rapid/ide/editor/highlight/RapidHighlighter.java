package com.bossymr.rapid.ide.editor.highlight;

import com.bossymr.rapid.language.lexer.RapidLexer;
import com.bossymr.rapid.language.lexer.RapidStringLexer;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.StringEscapesTokenTypes;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RapidHighlighter extends SyntaxHighlighterBase {

    private static final Map<IElementType, RapidColor> HIGHLIGHTS = new HashMap<>();

    static {
        fillMap(RapidTokenTypes.OPERATIONS, RapidColor.OPERATOR_SIGN);
        fillMap(RapidTokenTypes.KEYWORDS, RapidColor.KEYWORD);

        HIGHLIGHTS.put(RapidTokenTypes.INTEGER_LITERAL, RapidColor.NUMBER);
        HIGHLIGHTS.put(RapidTokenTypes.STRING_LITERAL, RapidColor.STRING);
        HIGHLIGHTS.put(StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN, RapidColor.VALID_STRING_ESCAPE);
        HIGHLIGHTS.put(StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN, RapidColor.INVALID_STRING_ESCAPE);
        HIGHLIGHTS.put(StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN, RapidColor.INVALID_STRING_ESCAPE);

        HIGHLIGHTS.put(RapidTokenTypes.LPARENTH, RapidColor.PARENTHESES);
        HIGHLIGHTS.put(RapidTokenTypes.RPARENTH, RapidColor.PARENTHESES);

        HIGHLIGHTS.put(RapidTokenTypes.LBRACE, RapidColor.BRACES);
        HIGHLIGHTS.put(RapidTokenTypes.RBRACE, RapidColor.BRACES);

        HIGHLIGHTS.put(RapidTokenTypes.LBRACKET, RapidColor.BRACKETS);
        HIGHLIGHTS.put(RapidTokenTypes.RBRACKET, RapidColor.BRACKETS);

        HIGHLIGHTS.put(RapidTokenTypes.COMMA, RapidColor.COMMA);
        HIGHLIGHTS.put(RapidTokenTypes.DOT, RapidColor.DOT);
        HIGHLIGHTS.put(RapidTokenTypes.SEMICOLON, RapidColor.SEMICOLON);
        HIGHLIGHTS.put(RapidTokenTypes.LINE, RapidColor.LINE);

        HIGHLIGHTS.put(RapidTokenTypes.COMMENT, RapidColor.COMMENT);
    }

    private static void fillMap(TokenSet tokenSet, RapidColor color) {
        for (IElementType elementType : tokenSet.getTypes()) {
            HIGHLIGHTS.put(elementType, color);
        }
    }

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        LayeredLexer lexer = new LayeredLexer(new RapidLexer());
        lexer.registerLayer(new RapidStringLexer(), RapidTokenTypes.STRING_LITERAL);
        return lexer;
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        TextAttributesKey textAttributesKey = HIGHLIGHTS.containsKey(tokenType) ? HIGHLIGHTS.get(tokenType).textAttributesKey : null;
        return pack(textAttributesKey);
    }
}
