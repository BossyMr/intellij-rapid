package com.bossymr.rapid.ide.editor.formatting;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.editor.formatting.settings.RapidCodeStylePanel;
import com.bossymr.rapid.ide.editor.formatting.settings.RapidIndentOptionsEditor;
import com.bossymr.rapid.language.RapidLanguage;
import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {

    @Override
    public @Nullable CustomCodeStyleSettings createCustomSettings(@NotNull CodeStyleSettings settings) {
        return new RapidCodeStyleSettings(settings);
    }

    @Override
    public @Nullable String getCodeSample(@NotNull SettingsType settingsType) {
        return CodeStyleAbstractPanel.readFromFile(getClass(), switch (settingsType) {
            case SPACING_SETTINGS -> "SPACING_TEMPLATE.mod";
            case BLANK_LINES_SETTINGS -> "BLANK_LINE_TEMPLATE.mod";
            case WRAPPING_AND_BRACES_SETTINGS -> "WRAPPING_AND_BRACES_TEMPLATE.mod";
            default -> "GENERAL_TEMPLATE.mod";
        });
    }

    @Override
    public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
        switch (settingsType) {
            case BLANK_LINES_SETTINGS -> {
                consumer.showCustomOption(RapidCodeStyleSettings.class, "BLANK_LINES_AROUND_STRUCTURE",
                        RapidBundle.message("blank.lines.around.structure"), CodeStyleSettingsCustomizableOptions.getInstance().BLANK_LINES);
                consumer.showCustomOption(RapidCodeStyleSettings.class, "BLANK_LINES_AROUND_COMPONENT",
                        RapidBundle.message("blank.lines.around.component"), CodeStyleSettingsCustomizableOptions.getInstance().BLANK_LINES);
                consumer.showCustomOption(RapidCodeStyleSettings.class, "BLANK_LINES_AROUND_FIELD",
                        RapidBundle.message("blank.lines.around.field"), CodeStyleSettingsCustomizableOptions.getInstance().BLANK_LINES);
                consumer.showCustomOption(RapidCodeStyleSettings.class, "BLANK_LINES_AROUND_ROUTINE",
                        RapidBundle.message("blank.lines.around.routine"), CodeStyleSettingsCustomizableOptions.getInstance().BLANK_LINES);

                consumer.showStandardOptions("KEEP_BLANK_LINES_IN_DECLARATIONS", "KEEP_BLANK_LINES_IN_CODE");
            }
            case WRAPPING_AND_BRACES_SETTINGS -> {
                consumer.showStandardOptions("RIGHT_MARGIN",
                        "WRAP_ON_TYPING",
                        "KEEP_LINE_BREAKS",
                        "WRAP_LONG_LINES",
                        "CALL_PARAMETERS_LPAREN_ON_NEXT_LINE",
                        "CALL_PARAMETERS_RPAREN_ON_NEXT_LINE",
                        "METHOD_PARAMETERS_LPAREN_ON_NEXT_LINE",
                        "METHOD_PARAMETERS_RPAREN_ON_NEXT_LINE",
                        "PARENTHESES_EXPRESSION_LPAREN_WRAP",
                        "PARENTHESES_EXPRESSION_RPAREN_WRAP",
                        "ARRAY_INITIALIZER_LBRACE_ON_NEXT_LINE",
                        "ARRAY_INITIALIZER_RBRACE_ON_NEXT_LINE",
                        "BINARY_OPERATION_SIGN_ON_NEXT_LINE",
                        "CALL_PARAMETERS_WRAP",
                        "METHOD_PARAMETERS_WRAP",
                        "BINARY_OPERATION_WRAP",
                        "ARRAY_INITIALIZER_WRAP",
                        "ASSIGNMENT_WRAP",
                        "KEEP_SIMPLE_METHODS_IN_ONE_LINE",
                        "ALIGN_MULTILINE_PARAMETERS",
                        "ALIGN_MULTILINE_PARAMETERS_IN_CALLS",
                        "ALIGN_MULTILINE_BINARY_OPERATION",
                        "ALIGN_MULTILINE_PARENTHESIZED_EXPRESSION",
                        "ALIGN_MULTILINE_ARRAY_INITIALIZER_EXPRESSION",
                        "DO_NOT_INDENT_TOP_LEVEL_CLASS_MEMBERS");

                consumer.renameStandardOption("ARRAY_INITIALIZER_LBRACE_ON_NEXT_LINE", RapidBundle.message("formatting.wrapping.aggregate.after"));
                consumer.renameStandardOption("ARRAY_INITIALIZER_RBRACE_ON_NEXT_LINE", RapidBundle.message("formatting.wrapping.aggregate.before"));
                consumer.renameStandardOption("ARRAY_INITIALIZER_WRAP", RapidBundle.message("formatting.group.wrapping.aggregate"));

                consumer.showCustomOption(RapidCodeStyleSettings.class, "ATTRIBUTE_LIST_WRAP",
                        RapidBundle.message("formatting.wrapping.attribute.list"), null, CodeStyleSettingsCustomizableOptions.getInstance().WRAP_OPTIONS,
                        CodeStyleSettingsCustomizable.WRAP_VALUES);

                consumer.showCustomOption(RapidCodeStyleSettings.class, "INDENT_ROUTINE_STATEMENT_LIST",
                        RapidBundle.message("indent.handler.statement.list"), RapidBundle.message("formatting.group.wrapping.routine"));

                consumer.showCustomOption(RapidCodeStyleSettings.class, "ALIGN_MULTILINE_ATTRIBUTE_LIST",
                        RapidBundle.message("formatting.wrapping.multiline.attribute.list"), RapidBundle.message("formatting.group.wrapping.attribute.list"));

                consumer.showCustomOption(RapidCodeStyleSettings.class, "INDENT_CASE_FROM_TEST_STATEMENT",
                        RapidBundle.message("formatting.wrapping.indent.case.from.test.statement"), RapidBundle.message("formatting.group.wrapping.test.statement"));

                consumer.renameStandardOption("DO_NOT_INDENT_TOP_LEVEL_CLASS_MEMBERS", RapidBundle.message("do.not.indent.module.symbols"));
            }
            case SPACING_SETTINGS -> {
                consumer.showStandardOptions("SPACE_AROUND_ASSIGNMENT_OPERATORS",
                        "SPACE_AROUND_EQUALITY_OPERATORS",
                        "SPACE_AROUND_RELATIONAL_OPERATORS",
                        "SPACE_AROUND_ADDITIVE_OPERATORS",
                        "SPACE_AROUND_MULTIPLICATIVE_OPERATORS",
                        "SPACE_AROUND_UNARY_OPERATOR",
                        "SPACE_AFTER_COMMA",
                        "SPACE_BEFORE_COMMA",
                        "SPACE_WITHIN_PARENTHESES",
                        "SPACE_WITHIN_METHOD_CALL_PARENTHESES",
                        "SPACE_WITHIN_EMPTY_METHOD_CALL_PARENTHESES",
                        "SPACE_WITHIN_METHOD_PARENTHESES",
                        "SPACE_WITHIN_EMPTY_METHOD_PARENTHESES",
                        "SPACE_WITHIN_ARRAY_INITIALIZER_BRACES",
                        "SPACE_WITHIN_EMPTY_ARRAY_INITIALIZER_BRACES",
                        "SPACE_BEFORE_METHOD_CALL_PARENTHESES",
                        "SPACE_BEFORE_METHOD_PARENTHESES");

                consumer.showCustomOption(RapidCodeStyleSettings.class, "SPACE_BEFORE_ATTRIBUTE_LIST",
                        RapidBundle.message("space.before.attribute.list"), CodeStyleSettingsCustomizableOptions.getInstance().SPACES_BEFORE_PARENTHESES);

                consumer.renameStandardOption("SPACE_WITHIN_ARRAY_INITIALIZER_BRACES", RapidBundle.message("space.array.initializer"));
                consumer.renameStandardOption("SPACE_WITHIN_EMPTY_ARRAY_INITIALIZER_BRACES", RapidBundle.message("space.empty.array.initializer"));

                consumer.renameStandardOption("SPACE_AROUND_ASSIGNMENT_OPERATORS", RapidBundle.message("space.around.assignment.operators"));
                consumer.renameStandardOption("SPACE_AROUND_EQUALITY_OPERATORS", RapidBundle.message("space.around.equality.operators"));
                consumer.renameStandardOption("SPACE_AROUND_RELATIONAL_OPERATORS", RapidBundle.message("space.around.relational.operators"));
                consumer.renameStandardOption("SPACE_AROUND_ADDITIVE_OPERATORS", RapidBundle.message("space.around.additive.operators"));
                consumer.renameStandardOption("SPACE_AROUND_MULTIPLICATIVE_OPERATORS", RapidBundle.message("space.around.multiplicative.operators"));
                consumer.renameStandardOption("SPACE_AROUND_UNARY_OPERATORS", RapidBundle.message("space.around.unary.operators"));
            }
            case INDENT_SETTINGS, LANGUAGE_SPECIFIC, COMMENTER_SETTINGS -> consumer.showAllStandardOptions();
        }
    }

    @Override
    public @NotNull Language getLanguage() {
        return RapidLanguage.INSTANCE;
    }

    @Override
    public @Nullable IndentOptionsEditor getIndentOptionsEditor() {
        return new RapidIndentOptionsEditor();
    }

    @Override
    public @NotNull CodeStyleConfigurable createConfigurable(@NotNull CodeStyleSettings baseSettings,
                                                             @NotNull CodeStyleSettings modelSettings) {
        return new CodeStyleAbstractConfigurable(baseSettings, modelSettings, RapidLanguage.INSTANCE.getDisplayName()) {
            @Override
            protected @NotNull CodeStyleAbstractPanel createPanel(@NotNull CodeStyleSettings settings) {
                return new RapidCodeStylePanel(getCurrentSettings(), settings);
            }
        };
    }
}
