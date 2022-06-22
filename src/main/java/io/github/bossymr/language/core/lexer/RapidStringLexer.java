package io.github.bossymr.language.core.lexer;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.StringEscapesTokenTypes;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.text.CharArrayUtil;
import io.github.bossymr.language.core.psi.RapidElementTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        if (tokenStart >= tokenEnd) return null;
        if (sequence.charAt(tokenStart) != '\\') {
            return RapidElementTypes.STRING_LITERAL;
        }
        if (tokenStart + 1 >= tokenEnd) {
            return StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN;
        }
        if (sequence.charAt(tokenStart + 1) == '\\') {
            return StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN;
        }
        if (tokenStart + 2 >= tokenEnd) {
            return StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN;
        }
        return isValid(sequence.charAt(tokenStart + 1)) && isValid(sequence.charAt(tokenStart + 2)) ?
                StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN : StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN;
    }

    private boolean isValid(int character) {
        return character >= '0' && character <= '9' || character >= 'A' && character <= 'F' || character >= 'a' && character <= 'f';
    }

    private int determineTokenEnd() {
        if (tokenStart >= bufferEnd) return tokenStart;
        if (sequence.charAt(tokenStart) == '\\') {
            int i = this.tokenStart + 1;
            if (i >= bufferEnd) return bufferEnd;
            if (i + 2 < bufferEnd && sequence.charAt(i) != '\\') return i + 2;
            return i + 1 < bufferEnd ? i : bufferEnd;
        } else {
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
