package com.bossymr.rapid.ide.highlight;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.RapidIcons;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class RapidColorSettingsPage implements ColorSettingsPage {

    @Override
    public @Nullable Icon getIcon() {
        return RapidIcons.RAPID;
    }

    @Override
    public @NotNull SyntaxHighlighter getHighlighter() {
        return new RapidHighlighter();
    }

    @Override
    public @NonNls @NotNull String getDemoText() {
        return """
                MODULE <MODULE>SomeModule</MODULE>(READONLY)

                    RECORD <PUBLIC><RECORD>SomeRecord</RECORD></PUBLIC>
                       num <UNUSED><COMPONENT>SomeNumber</COMPONENT></UNUSED>;
                    ENDRECORD

                    ALIAS <PUBLIC><RECORD>SomeRecord</RECORD></PUBLIC> <PUBLIC><ALIAS>SomeAlias</ALIAS></PUBLIC>;

                    LOCAL VAR <ATOMIC>num</ATOMIC> <LOCAL><VARIABLE>localField</VARIABLE></LOCAL> := 1;

                    VAR string <PUBLIC><VARIABLE>Field</VARIABLE></PUBLIC> = "Another\\\\String\\01\\OK";

                    CONST <ATOMIC>num</ATOMIC> <CONSTANT>constantField</CONSTANT> = 2;
                    PERS <ATOMIC>num</ATOMIC> <PERSISTENT>persistentField</PERSISTENT> = 3;

                    FUNC <ATOMIC>num</ATOMIC> Calculate(INOUT <ATOMIC>num</ATOMIC> <PARAMETER>param1</PARAMETER>, \\switch <OPTIONAL_PARAMETER>switch1</OPTIONAL_PARAMETER> | switch <OPTIONAL_PARAMETER>switch2</OPTIONAL_PARAMETER>, <ATOMIC>num</ATOMIC>{*} reassignedParam)
                        VAR num <REASSIGNED_LOCAL_VARIABLE>reassignedValue</REASSIGNED_LOCAL_VARIABLE> = <CONSTANT>constantField</CONSTANT> + <PARAMETER>param1</PARAMETER>;
                        VAR string <LOCAL_VARIABLE>localVar</LOCAL_VARIABLE> = "Intellij";
                        <REASSIGNED_LOCAL_VARIABLE>reassignedValue</REASSIGNED_LOCAL_VARIABLE> := <REASSIGNED_LOCAL_VARIABLE>reassignedValue</REASSIGNED_LOCAL_VARIABLE> + <LOCAL><VARIABLE>localField</VARIABLE></LOCAL>;
                        IF <FUNCTION_CALL>Present</FUNCTION_CALL>(<OPTIONAL_PARAMETER>switch1</OPTIONAL_PARAMETER>) AND <WARNING><KEYWORD>NOT</KEYWORD> <FUNCTION_CALL>Present</FUNCTION_CALL><PARENTHESES>(</PARENTHESES><OPTIONAL_PARAMETER>switch2</OPTIONAL_PARAMETER><PARENTHESES>)</PARENTHESES></WARNING> THEN
                            <REASSIGNED_PARAMETER>reassignedParam</REASSIGNED_PARAMETER> := [2,3,4];
                        ENDIF
                    ENDFUNC

                ENDMODULE""";
    }

    @Override
    public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        Map<String, TextAttributesKey> keyMap = Arrays.stream(RapidColor.values())
                .collect(Collectors.toMap(RapidColor::name, RapidColor::textAttributesKey));
        keyMap.put("WARNING", CodeInsightColors.WARNINGS_ATTRIBUTES);
        keyMap.put("UNUSED", CodeInsightColors.NOT_USED_ELEMENT_ATTRIBUTES);
        return keyMap;
    }

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return Arrays.stream(RapidColor.values())
                .map(RapidColor::attributesDescriptor)
                .toArray(AttributesDescriptor[]::new);
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @Override
    public @NotNull String getDisplayName() {
        return RapidBundle.message("settings.rapid.color.scheme.title");
    }
}
