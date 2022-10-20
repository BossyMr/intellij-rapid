package com.bossymr.rapid.ide.highlight;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.RapidLanguage;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum RapidColor {

    KEYWORD(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.keyword"), DefaultLanguageHighlighterColors.KEYWORD),
    NUMBER(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.number"), DefaultLanguageHighlighterColors.NUMBER),

    COMMENT(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.comment"), DefaultLanguageHighlighterColors.LINE_COMMENT),

    STRING(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.string"), DefaultLanguageHighlighterColors.STRING),
    VALID_STRING_ESCAPE(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.valid.escape.sequence"), DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE),
    INVALID_STRING_ESCAPE(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.invalid.escape.sequence"), DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE),

    OPERATOR_SIGN(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.operator.sign"), DefaultLanguageHighlighterColors.OPERATION_SIGN),
    PARENTHESES(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.parentheses"), DefaultLanguageHighlighterColors.PARENTHESES),
    BRACES(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.braces"), DefaultLanguageHighlighterColors.BRACES),
    BRACKETS(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.brackets"), DefaultLanguageHighlighterColors.BRACKETS),
    COMMA(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.comma"), DefaultLanguageHighlighterColors.COMMA),
    SEMICOLON(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.semicolon"), DefaultLanguageHighlighterColors.SEMICOLON),
    DOT(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.dot"), DefaultLanguageHighlighterColors.DOT),
    LINE(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.line"), DefaultLanguageHighlighterColors.PARENTHESES),


    MODULE(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.module"), DefaultLanguageHighlighterColors.CLASS_NAME),
    SYSTEM_MODULE(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.system.module"), DefaultLanguageHighlighterColors.CLASS_NAME),

    ATOMIC(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.atomic"), DefaultLanguageHighlighterColors.CLASS_NAME),
    RECORD(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.record"), DefaultLanguageHighlighterColors.CLASS_NAME),
    ALIAS(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.alias"), DefaultLanguageHighlighterColors.CLASS_NAME),
    COMPONENT(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.component"), DefaultLanguageHighlighterColors.INSTANCE_FIELD),

    VARIABLE(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.variable"), DefaultLanguageHighlighterColors.INSTANCE_FIELD),
    PERSISTENT(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.persistent"), DefaultLanguageHighlighterColors.STATIC_FIELD),
    CONSTANT(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.constant"), DefaultLanguageHighlighterColors.INSTANCE_FIELD),

    TASK_VARIABLE(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.task.variable"), VARIABLE.textAttributesKey),
    TASK_PERSISTENT(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.task.persistent"), PERSISTENT.textAttributesKey),

    LOCAL_VARIABLE(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.local.variable"), DefaultLanguageHighlighterColors.LOCAL_VARIABLE),
    REASSIGNED_LOCAL_VARIABLE(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.reassigned.local.variable"), DefaultLanguageHighlighterColors.REASSIGNED_LOCAL_VARIABLE),

    FUNCTION_CALL(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.function.call"), DefaultLanguageHighlighterColors.FUNCTION_CALL),
    PROCEDURE_CALL(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.procedure.call"), DefaultLanguageHighlighterColors.FUNCTION_CALL),
    TRAP_CALL(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.trap.call"), DefaultLanguageHighlighterColors.FUNCTION_CALL),

    PARAMETER(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.parameter"), DefaultLanguageHighlighterColors.PARAMETER),
    OPTIONAL_PARAMETER(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.optional.parameter"), PARAMETER.textAttributesKey),
    REASSIGNED_PARAMETER(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.reassigned.parameter"), DefaultLanguageHighlighterColors.REASSIGNED_PARAMETER),

    FUNCTION(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.function"), DefaultLanguageHighlighterColors.CLASS_NAME),
    PROCEDURE(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.procedure"), DefaultLanguageHighlighterColors.CLASS_NAME),
    TRAP(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.trap"), DefaultLanguageHighlighterColors.CLASS_NAME),

    PUBLIC(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.public"), null),
    LOCAL(RapidBundle.messagePointer("settings.rapid.attribute.descriptor.local"), null);

    public final @NotNull TextAttributesKey textAttributesKey;
    public final @NotNull AttributesDescriptor attributesDescriptor;

    RapidColor(Supplier<String> name, TextAttributesKey fallback) {
        this.textAttributesKey = TextAttributesKey.createTextAttributesKey(RapidLanguage.INSTANCE.getID() + '_' + name(), fallback);
        this.attributesDescriptor = new AttributesDescriptor(name, textAttributesKey);
    }

    public TextAttributesKey textAttributesKey() {
        return textAttributesKey;
    }

    public AttributesDescriptor attributesDescriptor() {
        return attributesDescriptor;
    }
}
