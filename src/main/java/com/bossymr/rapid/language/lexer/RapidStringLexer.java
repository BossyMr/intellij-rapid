package com.bossymr.rapid.language.lexer;

import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.StringEscapesTokenTypes;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Lexer used to highlight valid and invalid character escapes inside of strings (and should only be run inside of
 * string literals). Character escapes in Rapid are comprised of either {@code \\} to escape a {@code \} character, or
 * {@code \[a-fA-F0-9]} ({@code \} followed by two hexadecimal characters).
 */
public class RapidStringLexer extends LexerBase {

    private int state = 0;

    private int tokenStart = 0;
    private int tokenEnd = 0;

    private CharSequence sequence;
    private int bufferEnd = 0;
    private @Nullable IElementType tokenType = null;

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        sequence = buffer;
        bufferEnd = endOffset;
        state = initialState;
        tokenEnd = startOffset;
        advance();
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public @Nullable IElementType getTokenType() {
        return tokenType;
    }

    @Override
    public int getTokenStart() {
        return tokenStart;
    }

    @Override
    public int getTokenEnd() {
        return tokenEnd;
    }

    private @Nullable IElementType determineTokenType() {
        if (tokenStart >= tokenEnd) {
            // This string is finished.
            return null;
        }
        if (sequence.charAt(tokenStart) != '\\') {
            // This token isn't an escape sequence.
            return RapidTokenTypes.STRING_LITERAL;
        }
        if (tokenStart + 1 >= tokenEnd) {
            // '\' is at the end of the string.
            return StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN;
        }
        if (sequence.charAt(tokenStart + 1) == '\\') {
            // '\' is followed by another '\'.
            return StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN;
        }
        if (tokenStart + 2 >= tokenEnd) {
            // '\' is only followed by only a single character.
            return StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN;
        }
        return isValid(sequence.charAt(tokenStart + 1)) && isValid(sequence.charAt(tokenStart + 2)) ?
                StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN : StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN;
    }

    private boolean isValid(int character) {
        return character >= '0' && character <= '9' || character >= 'A' && character <= 'F' || character >= 'a' && character <= 'f';
    }

    private int determineTokenEnd() {
        if (tokenStart >= bufferEnd) {
            // This string is finished.
            return tokenStart;
        }
        if (sequence.charAt(tokenStart) == '\\') {
            // Find the end of the escape sequence.
            int i = this.tokenStart + 1;
            if (tokenStart + 1 >= bufferEnd) {
                // The next character is the last character in the string: \".
                return bufferEnd;
            }
            if (tokenStart + 3 < bufferEnd && sequence.charAt(i) != '\\') {
                // The next character isn't '\' and a complete escape sequence is available: \xx.
                return tokenStart + 3;
            }
            // The escape sequence contains one additional character: \x or \\.
            return Math.min(tokenStart + 2, bufferEnd);
        } else {
            // Find the next '\' character.
            int i = CharArrayUtil.indexOf(sequence, "\\", tokenStart + 1, bufferEnd);
            return i != -1 ? i : bufferEnd;
        }
    }

    @Override
    public void advance() {
        tokenStart = tokenEnd;
        tokenEnd = determineTokenEnd();
        tokenType = determineTokenType();
    }

    @Override
    public @NotNull CharSequence getBufferSequence() {
        return sequence;
    }

    @Override
    public int getBufferEnd() {
        return bufferEnd;
    }
}
