package io.github.bossymr.ide.highlight;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

public final class RapidHighlightingColors {

    public static final TextAttributesKey COMMENT = TextAttributesKey.createTextAttributesKey("RAPID_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey KEYWORD = TextAttributesKey.createTextAttributesKey("RAPID_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey NUMBER = TextAttributesKey.createTextAttributesKey("RAPID_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey STRING = TextAttributesKey.createTextAttributesKey("RAPID_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey OPERATION_SIGN = TextAttributesKey.createTextAttributesKey("RAPID_OPERATION_SIGN", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey PARENTHESES = TextAttributesKey.createTextAttributesKey("RAPID_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey BRACKETS = TextAttributesKey.createTextAttributesKey("RAPID_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey BRACES = TextAttributesKey.createTextAttributesKey("RAPID_BRACES", DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey COMMA = TextAttributesKey.createTextAttributesKey("RAPID_COMMA", DefaultLanguageHighlighterColors.COMMA);
    public static final TextAttributesKey DOT = TextAttributesKey.createTextAttributesKey("RAPID_DOT", DefaultLanguageHighlighterColors.DOT);
    public static final TextAttributesKey SEMICOLON = TextAttributesKey.createTextAttributesKey("RAPID_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON);


    public static final TextAttributesKey VALID_STRING_ESCAPE = TextAttributesKey.createTextAttributesKey("RAPID_VALID_STRING_ESCAPE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
    public static final TextAttributesKey INVALID_STRING_ESCAPE = TextAttributesKey.createTextAttributesKey("RAPID_INVALID_STRING_ESCAPE", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE);
}
