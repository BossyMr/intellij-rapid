package com.bossymr.rapid.ide.codeStyle.formatting;

import com.bossymr.rapid.ide.codeStyle.RapidCodeStyleSettings;
import com.bossymr.rapid.language.psi.RapidBinaryExpression;
import com.bossymr.rapid.language.psi.RapidStatementList;
import com.bossymr.rapid.language.psi.RapidTokenSets;
import com.bossymr.rapid.language.psi.StatementListType;
import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.bossymr.rapid.language.psi.RapidElementTypes.*;
import static com.bossymr.rapid.language.psi.RapidTokenTypes.*;

/**
 * Implementation of a formatting block.
 */
public class RapidBlock extends AbstractBlock {

    private final @Nullable Indent indent;
    private final @NotNull RapidCodeStyleSettings customSettings;
    private final @NotNull CommonCodeStyleSettings commonSettings;
    private final @NotNull SpacingBuilder spacingBuilder;

    public RapidBlock(@NotNull ASTNode node,
                      @Nullable Indent indent,
                      @Nullable Wrap wrap,
                      @Nullable Alignment alignment,
                      @NotNull RapidCodeStyleSettings customSettings,
                      @NotNull CommonCodeStyleSettings commonSettings,
                      @NotNull SpacingBuilder spacingBuilder) {
        super(node, wrap, alignment);
        this.indent = indent;
        this.customSettings = customSettings;
        this.commonSettings = commonSettings;
        this.spacingBuilder = spacingBuilder;
    }

    public static @NotNull WrapType getWrapType(int wrapType) {
        return switch (wrapType) {
            case CommonCodeStyleSettings.WRAP_ALWAYS -> WrapType.ALWAYS;
            case CommonCodeStyleSettings.WRAP_AS_NEEDED -> WrapType.NORMAL;
            case CommonCodeStyleSettings.DO_NOT_WRAP -> WrapType.NONE;
            default -> WrapType.CHOP_DOWN_IF_LONG;
        };
    }

    private @NotNull RapidBlock createBlock(@NotNull ASTNode node,
                                            @Nullable Indent indent,
                                            @Nullable Wrap wrap,
                                            @Nullable Alignment alignment) {
        return new RapidBlock(node, indent, wrap, alignment, customSettings, commonSettings, spacingBuilder);
    }

    @Override
    public @Nullable Spacing getSpacing(@Nullable Block left, @NotNull Block right) {
        return spacingBuilder.getSpacing(this, left, right);
    }

    @Override
    public boolean isLeaf() {
        return getNode().getFirstChildNode() == null;
    }

    @Override
    public @Nullable String getDebugName() {
        return getClass().getSimpleName() + ":" + getNode().getElementType();
    }

    @Override
    protected @NotNull List<Block> buildChildren() {
        IElementType parentType = getNode().getElementType();
        if (parentType == ATTRIBUTE_LIST) {
            return buildParenthesisedBlock(getWrapType(customSettings.ATTRIBUTE_LIST_WRAP), LPARENTH, RPARENTH, customSettings.ALIGN_MULTILINE_ATTRIBUTE_LIST);
        }
        if (parentType == PARAMETER_LIST) {
            return buildParenthesisedBlock(getWrapType(commonSettings.METHOD_PARAMETERS_WRAP), LPARENTH, RPARENTH, commonSettings.ALIGN_MULTILINE_PARAMETERS);
        }
        if (parentType == ARGUMENT_LIST) {
            return buildParenthesisedBlock(getWrapType(commonSettings.CALL_PARAMETERS_WRAP), LPARENTH, RPARENTH, commonSettings.ALIGN_MULTILINE_PARAMETERS_IN_CALLS);
        }
        if (parentType == AGGREGATE_EXPRESSION) {
            return buildParenthesisedBlock(getWrapType(commonSettings.ARRAY_INITIALIZER_WRAP), LBRACKET, RBRACKET, commonSettings.ALIGN_MULTILINE_ARRAY_INITIALIZER_EXPRESSION);
        }
        if (parentType == PARENTHESISED_EXPRESSION) {
            return buildParenthesisedBlock(WrapType.NONE, LBRACKET, RBRACKET, commonSettings.ALIGN_MULTILINE_PARENTHESIZED_EXPRESSION);
        }
        List<Block> blocks = new ArrayList<>();
        Alignment childAlignment = null;
        if (parentType == BINARY_EXPRESSION) {
            if (commonSettings.ALIGN_MULTILINE_BINARY_OPERATION) {
                childAlignment = Alignment.createAlignment();
            }
        }
        for (ASTNode child : getNode().getChildren(TokenSet.ANY)) {
            if (FormatterUtil.isWhitespaceOrEmpty(child)) continue;
            Indent childIndent = getChildIndent(child);
            Wrap childWrap = getChildWrap(child);

            Block block = createBlock(child, childIndent, childWrap, childAlignment);
            blocks.add(block);
        }
        return blocks;
    }

    private @NotNull List<Block> buildParenthesisedBlock(WrapType wrapType, @NotNull IElementType left,
                                                         @NotNull IElementType right, boolean align) {
        List<Block> blocks = new ArrayList<>();
        Wrap wrap = Wrap.createWrap(wrapType, false);
        Alignment alignment = align ? Alignment.createAlignment() : null;
        for (ASTNode child : getNode().getChildren(TokenSet.ANY)) {
            IElementType elementType = child.getElementType();
            if (FormatterUtil.isWhitespaceOrEmpty(child)) continue;
            if (TokenSet.create(left, right).contains(elementType)) {
                blocks.add(createBlock(child, Indent.getNoneIndent(), null, null));
            } else if (TokenSet.create(COMMA, COMMENT).contains(elementType)) {
                blocks.add(createBlock(child, Indent.getContinuationWithoutFirstIndent(), null, null));
            } else {
                blocks.add(createBlock(child, Indent.getContinuationWithoutFirstIndent(), wrap, alignment));
            }
        }
        return blocks;
    }

    private @Nullable Wrap getChildWrap(@NotNull ASTNode child) {
        if (isBuildIndentsOnly()) return null;
        IElementType parentType = getNode().getElementType();
        IElementType elementType = child.getElementType();
        if (parentType == ARGUMENT_LIST) {
            return Wrap.createWrap(getWrapType(commonSettings.CALL_PARAMETERS_WRAP), commonSettings.CALL_PARAMETERS_LPAREN_ON_NEXT_LINE);
        }
        if (parentType == PARAMETER_LIST) {
            return Wrap.createWrap(getWrapType(commonSettings.METHOD_PARAMETERS_WRAP), commonSettings.METHOD_PARAMETERS_LPAREN_ON_NEXT_LINE);
        }
        if (parentType == BINARY_EXPRESSION) {
            RapidBinaryExpression expression = getNode().getPsi(RapidBinaryExpression.class);
            if (commonSettings.BINARY_OPERATION_SIGN_ON_NEXT_LINE) {
                if (child == expression.getLeft().getNode()) {
                    return Wrap.createWrap(getWrapType(commonSettings.BINARY_OPERATION_WRAP), false);
                }
                return null;
            } else if (child == expression.getSign().getNode()) {
                return Wrap.createWrap(getWrapType(commonSettings.BINARY_OPERATION_WRAP), false);
            }
            return null;
        }
        if (parentType == AGGREGATE_EXPRESSION) {
            return Wrap.createWrap(getWrapType(commonSettings.ARRAY_INITIALIZER_WRAP), commonSettings.ARRAY_INITIALIZER_LBRACE_ON_NEXT_LINE);
        }
        if (parentType == STATEMENT_LIST) {
            ASTNode treeParent = getNode().getTreeParent();
            if (commonSettings.KEEP_SIMPLE_METHODS_IN_ONE_LINE
                    && treeParent != null
                    && treeParent.getElementType() == ROUTINE
                    && !(getNode().textContains('\n'))) {
                return null;
            }
            return Wrap.createWrap(WrapType.NORMAL, false);
        }
        if (parentType == FIELD) {
            if (elementType == IDENTIFIER) {
                return Wrap.createWrap(WrapType.NONE, false);
            }
            if (RapidTokenSets.EXPRESSIONS.contains(elementType)) {
                return Wrap.createWrap(WrapType.NONE, false);
            }
            return null;
        }
        if (parentType == ASSIGNMENT_STATEMENT) {
            return Wrap.createWrap(commonSettings.ASSIGNMENT_WRAP, true);
        }
        return null;
    }


    private @Nullable Indent getChildIndent(@NotNull ASTNode child) {
        final IElementType parentType = getNode().getElementType();
        final IElementType elementType = child.getElementType();
        CommonCodeStyleSettings.IndentOptions indentOptions = commonSettings.getIndentOptions();
        if (indentOptions == null) return null;
        if (RapidTokenSets.KEYWORDS.contains(elementType)) {
            return Indent.getNoneIndent();
        }
        if (parentType == MODULE) {
            if (RapidTokenSets.SYMBOLS.contains(elementType)) {
                return commonSettings.DO_NOT_INDENT_TOP_LEVEL_CLASS_MEMBERS ? Indent.getNoneIndent() : Indent.getNormalIndent();
            }
        }
        if (parentType == TEST_STATEMENT) {
            if (elementType == TEST_CASE_STATEMENT) {
                return customSettings.INDENT_CASE_FROM_TEST_STATEMENT ? Indent.getNormalIndent() : Indent.getNoneIndent();
            }
        }
        if (TokenSet.create(IF_STATEMENT, FOR_STATEMENT, WHILE_STATEMENT, TEST_CASE_STATEMENT, ROUTINE, ALIAS).contains(parentType)) {
            return Indent.getNoneIndent();
        }
        if (parentType == RECORD) {
            return Indent.getNormalIndent();
        }
        if (parentType == STATEMENT_LIST) {
            return Indent.getNormalIndent();
        }
        if (elementType == STATEMENT_LIST) {
            RapidStatementList statementList = child.getPsi(RapidStatementList.class);
            if (statementList.getAttribute() != StatementListType.STATEMENT_LIST) {
                return customSettings.INDENT_ROUTINE_STATEMENT_LIST ? Indent.getNormalIndent() : Indent.getNoneIndent();
            }
        }
        if (parentType == FIELD) {
            return Indent.getContinuationWithoutFirstIndent(indentOptions.USE_RELATIVE_INDENTS);
        }
        return null;
    }

    @Override
    public @Nullable Indent getIndent() {
        return indent;
    }

    @Override
    protected @Nullable Indent getChildIndent() {
        final IElementType parentType = getNode().getElementType();
        CommonCodeStyleSettings.IndentOptions indentOptions = commonSettings.getIndentOptions();
        if (indentOptions == null) return null;
        if (TokenSet.create(TokenType.DUMMY_HOLDER, COMMENT).contains(parentType)) {
            return Indent.getNoneIndent();
        }
        if (TokenSet.create(MODULE, IF_STATEMENT, FOR_STATEMENT, WHILE_STATEMENT, TEST_STATEMENT, ROUTINE, STATEMENT_LIST).contains(parentType)) {
            return Indent.getNormalIndent();
        }
        if (parentType == FIELD) {
            return Indent.getContinuationWithoutFirstIndent(indentOptions.USE_RELATIVE_INDENTS);
        }
        return null;
    }
}