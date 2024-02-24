package com.bossymr.rapid.ide.editor.formatting;

import com.bossymr.rapid.language.RapidLanguage;
import com.bossymr.rapid.language.psi.RapidTokenSets;
import com.intellij.formatting.*;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

import static com.bossymr.rapid.language.psi.RapidElementTypes.*;
import static com.bossymr.rapid.language.psi.RapidTokenSets.*;
import static com.bossymr.rapid.language.psi.RapidTokenTypes.*;

/**
 * An implementation of {@link FormattingModelBuilder} for the custom language.
 */
public class RapidFormattingModelBuilder implements FormattingModelBuilder {

    public static @NotNull SpacingBuilder createSpaceBuilder(@NotNull CodeStyleSettings settings) {
        CommonCodeStyleSettings common = settings.getCommonSettings(RapidLanguage.INSTANCE);
        RapidCodeStyleSettings custom = settings.getCustomSettings(RapidCodeStyleSettings.class);
        return new SpacingBuilder(settings, RapidLanguage.INSTANCE)
                .around(STRUCTURES).blankLines(custom.BLANK_LINES_AROUND_STRUCTURE)
                .around(COMPONENT).blankLines(custom.BLANK_LINES_AROUND_COMPONENT)
                .around(FIELD).blankLines(custom.BLANK_LINES_AROUND_FIELD)
                .around(ROUTINE).blankLines(custom.BLANK_LINES_AROUND_ROUTINE)

                .aroundInside(ASSIGNMENT_OPERATORS, ASSIGNMENT_STATEMENT).spaceIf(common.SPACE_AROUND_ASSIGNMENT_OPERATORS)
                .aroundInside(EQUALITY_OPERATORS, BINARY_EXPRESSION).spaceIf(common.SPACE_AROUND_EQUALITY_OPERATORS)
                .aroundInside(RELATIONAL_OPERATORS, BINARY_EXPRESSION).spaceIf(common.SPACE_AROUND_RELATIONAL_OPERATORS)
                .aroundInside(ADDITIVE_OPERATORS, BINARY_EXPRESSION).spaceIf(common.SPACE_AROUND_ADDITIVE_OPERATORS)
                .aroundInside(TokenSet.create(ASTERISK, DIV), BINARY_EXPRESSION).spaceIf(common.SPACE_AROUND_MULTIPLICATIVE_OPERATORS)
                .aroundInside(TokenSet.create(PLUS, MINUS), UNARY_EXPRESSION).spaceIf(common.SPACE_AROUND_UNARY_OPERATOR)

                .after(COMMA).spaceIf(common.SPACE_AFTER_COMMA)
                .before(COMMA).spaceIf(common.SPACE_BEFORE_COMMA)

                .around(LINE).spaces(1)

                .before(SEMICOLON).none()
                .before(COLON).none()

                .after(RapidTokenSets.STATEMENTS).lineBreakInCode()
                .before(CASE_KEYWORD).lineBreakInCode()
                .before(DEFAULT_KEYWORD).lineBreakInCode()

                .withinPairInside(LPARENTH, RPARENTH, PARENTHESISED_EXPRESSION).spaceIf(common.SPACE_WITHIN_PARENTHESES)
                .withinPairInside(LPARENTH, RPARENTH, ARGUMENT_LIST).spaceIf(common.SPACE_WITHIN_METHOD_CALL_PARENTHESES)
                .betweenInside(LPARENTH, RPARENTH, ARGUMENT_LIST).spaceIf(common.SPACE_WITHIN_EMPTY_METHOD_CALL_PARENTHESES)
                .withinPairInside(LPARENTH, RPARENTH, PARAMETER_LIST).spaceIf(common.SPACE_WITHIN_METHOD_PARENTHESES)
                .betweenInside(LPARENTH, RPARENTH, PARAMETER_LIST).spaceIf(common.SPACE_WITHIN_EMPTY_METHOD_PARENTHESES)
                .withinPairInside(LBRACKET, RBRACKET, AGGREGATE_EXPRESSION).spaceIf(common.SPACE_WITHIN_ARRAY_INITIALIZER_BRACES)
                .betweenInside(LBRACKET, RBRACKET, AGGREGATE_EXPRESSION).spaceIf(common.SPACE_WITHIN_EMPTY_ARRAY_INITIALIZER_BRACES)
                .beforeInside(ARGUMENT_LIST, FUNCTION_CALL_EXPRESSION).spaceIf(common.SPACE_BEFORE_METHOD_CALL_PARENTHESES)
                .before(PARAMETER_LIST).spaceIf(common.SPACE_BEFORE_METHOD_PARENTHESES)
                .before(ATTRIBUTE_LIST).spaceIf(custom.SPACE_BEFORE_ATTRIBUTE_LIST)

                .afterInside(LPARENTH, ARGUMENT_LIST).spaceIf(common.SPACE_WITHIN_METHOD_CALL_PARENTHESES, common.CALL_PARAMETERS_LPAREN_ON_NEXT_LINE)
                .beforeInside(RPARENTH, ARGUMENT_LIST).spaceIf(common.SPACE_WITHIN_METHOD_CALL_PARENTHESES, common.CALL_PARAMETERS_RPAREN_ON_NEXT_LINE)
                .afterInside(LPARENTH, PARAMETER_LIST).spaceIf(common.SPACE_WITHIN_METHOD_PARENTHESES, common.METHOD_PARAMETERS_LPAREN_ON_NEXT_LINE)
                .beforeInside(RPARENTH, PARAMETER_LIST).spaceIf(common.SPACE_WITHIN_METHOD_PARENTHESES, common.METHOD_PARAMETERS_RPAREN_ON_NEXT_LINE)
                .afterInside(LPARENTH, PARENTHESISED_EXPRESSION).spaceIf(common.SPACE_WITHIN_PARENTHESES, common.PARENTHESES_EXPRESSION_LPAREN_WRAP)
                .beforeInside(RPARENTH, PARENTHESISED_EXPRESSION).spaceIf(common.SPACE_WITHIN_PARENTHESES, common.PARENTHESES_EXPRESSION_RPAREN_WRAP)
                .afterInside(LBRACKET, AGGREGATE_EXPRESSION).spaceIf(common.SPACE_WITHIN_BRACES, common.ARRAY_INITIALIZER_LBRACE_ON_NEXT_LINE)
                .beforeInside(RBRACKET, AGGREGATE_EXPRESSION).spaceIf(common.SPACE_WITHIN_PARENTHESES, common.ARRAY_INITIALIZER_RBRACE_ON_NEXT_LINE)

                .afterInside(ASSIGNMENT_OPERATORS, ASSIGNMENT_STATEMENT).spaceIf(common.SPACE_AROUND_ASSIGNMENT_OPERATORS, common.BINARY_OPERATION_SIGN_ON_NEXT_LINE)
                .afterInside(EQUALITY_OPERATORS, BINARY_EXPRESSION).spaceIf(common.SPACE_AROUND_EQUALITY_OPERATORS, common.BINARY_OPERATION_SIGN_ON_NEXT_LINE)
                .afterInside(RELATIONAL_OPERATORS, BINARY_EXPRESSION).spaceIf(common.SPACE_AROUND_RELATIONAL_OPERATORS, common.BINARY_OPERATION_SIGN_ON_NEXT_LINE)
                .afterInside(ADDITIVE_OPERATORS, BINARY_EXPRESSION).spaceIf(common.SPACE_AROUND_ADDITIVE_OPERATORS, common.BINARY_OPERATION_SIGN_ON_NEXT_LINE)
                .afterInside(TokenSet.create(ASTERISK, DIV), BINARY_EXPRESSION).spaceIf(common.SPACE_AROUND_MULTIPLICATIVE_OPERATORS, common.BINARY_OPERATION_SIGN_ON_NEXT_LINE);

    }

    @Override
    public @NotNull FormattingModel createModel(@NotNull FormattingContext formattingContext) {
        CodeStyleSettings codeStyleSettings = formattingContext.getCodeStyleSettings();
        RapidCodeStyleSettings customSettings = codeStyleSettings.getCustomSettings(RapidCodeStyleSettings.class);
        CommonCodeStyleSettings commonSettings = codeStyleSettings.getCommonSettings(RapidLanguage.INSTANCE);
        return FormattingModelProvider.createFormattingModelForPsiFile(formattingContext.getContainingFile(),
                new RapidBlock(
                        formattingContext.getNode(),
                        Indent.getNoneIndent(),
                        null,
                        null,
                        customSettings,
                        commonSettings,
                        createSpaceBuilder(codeStyleSettings)
                ), codeStyleSettings);
    }
}
