package io.github.bossymr.ide.highlight;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.options.OptionsBundle;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import io.github.bossymr.RapidBundle;
import io.github.bossymr.language.RapidLanguage;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum RapidColor {

    KEYWORD(RapidBundle.messagePointer("settings.rapid.color.keyword"), DefaultLanguageHighlighterColors.KEYWORD),

    NUMBER(RapidBundle.messagePointer("settings.rapid.color.number"), DefaultLanguageHighlighterColors.NUMBER),
    STRING(RapidBundle.messagePointer("settings.rapid.color.string"), DefaultLanguageHighlighterColors.STRING),

    COMMENT(OptionsBundle.messagePointer("options.language.defaults.line.comment"), DefaultLanguageHighlighterColors.LINE_COMMENT),

    BRACES(OptionsBundle.messagePointer("options.language.defaults.braces"), DefaultLanguageHighlighterColors.BRACES),
    BRACKETS(OptionsBundle.messagePointer("options.language.defaults.brackets"), DefaultLanguageHighlighterColors.BRACKETS),
    OPERATION_SIGN(RapidBundle.messagePointer("settings.rapid.color.operation.sign"), DefaultLanguageHighlighterColors.OPERATION_SIGN),
    SEMICOLON(OptionsBundle.messagePointer("options.language.defaults.semicolon"), DefaultLanguageHighlighterColors.SEMICOLON),
    DOT(OptionsBundle.messagePointer("options.language.defaults.dot"), DefaultLanguageHighlighterColors.DOT),
    COMMA(OptionsBundle.messagePointer("options.language.defaults.comma"), DefaultLanguageHighlighterColors.COMMA),
    PARENTHESES(OptionsBundle.messagePointer("options.language.defaults.parentheses"), DefaultLanguageHighlighterColors.PARENTHESES),

    VALID_STRING_ESCAPE(RapidBundle.messagePointer("settings.rapid.color.valid.escape.sequence"), DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE),
    INVALID_STRING_ESCAPE(RapidBundle.messagePointer("settings.rapid.invalid.escape.sequence"), DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE);

    public final @NotNull TextAttributesKey textAttributesKey;

    public final @NotNull AttributesDescriptor attributesDescriptor;

    RapidColor(Supplier<String> name, TextAttributesKey fallback) {
        this.textAttributesKey = TextAttributesKey.createTextAttributesKey(RapidLanguage.INSTANCE.getID() + '_' + name(), fallback);
        this.attributesDescriptor = new AttributesDescriptor(name, textAttributesKey);
    }
}
