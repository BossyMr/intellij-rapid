package io.github.bossymr.language.parser;

import com.intellij.lang.WhitespacesAndCommentsBinder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import io.github.bossymr.language.psi.RapidElementTypes;

/**
 * Utility methods used to parse language.
 */
public final class RapidParserUtil extends GeneratedParserUtilBase {

    public static WhitespacesAndCommentsBinder ADJACENT_LINE_COMMENTS = (tokens, atStreamEdge, getter) -> {
        if (tokens.size() == 0) return 0;
        int result = tokens.size();
        for (int i = tokens.size() - 1; i >= 0; i--) {
            final IElementType elementType = tokens.get(i);
            if (TokenSet.WHITE_SPACE.contains(elementType)) {
                if (StringUtil.getLineBreakCount(getter.get(i)) > 1) break;
            } else if (elementType.equals(RapidElementTypes.COMMENT)) {
                result = i;
            } else {
                break;
            }
        }
        return result;
    };
}
