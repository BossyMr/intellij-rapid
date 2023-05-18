package com.bossymr.rapid.ide.editor.formatting.formatting;

import com.bossymr.rapid.ide.editor.formatting.RapidCodeStyleSettings;
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

    private @NotNull RapidBlock createBlock(@NotNull ASTNode node,
                                            @Nullable Indent indent,
                                            @Nullable Wrap wrap,
                                            @Nullable Alignment alignment) {
        return new RapidBlock(node, indent, wrap, alignment, customSettings, commonSettings, spacingBuilder);
    }

    private @NotNull WrapType getWrapType(int wrapType) {
        return switch (wrapType) {
            case CommonCodeStyleSettings.WRAP_ALWAYS -> WrapType.ALWAYS;
            case CommonCodeStyleSettings.WRAP_AS_NEEDED -> WrapType.NORMAL;
            case CommonCodeStyleSettings.DO_NOT_WRAP -> WrapType.NONE;
            default -> WrapType.CHOP_DOWN_IF_LONG;
        };
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
        return "RapidBlock:" + getNode().getElementType();
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
        // Each child has the same alignment, so it must be created outside the for-loop.
        Alignment childAlignment = getChildAlignment();
        for (ASTNode child : getNode().getChildren(TokenSet.ANY)) {
            if (FormatterUtil.isWhitespaceOrEmpty(child)) {
                continue;
            }
            Indent childIndent = getChildIndent(child);
            Wrap childWrap = getChildWrap(child);
            Block block = createBlock(child, childIndent, childWrap, childAlignment);
            blocks.add(block);
        }
        return blocks;
    }

    private @NotNull List<Block> buildParenthesisedBlock(@NotNull WrapType wrapType, @NotNull IElementType left, @NotNull IElementType right, boolean shouldAlign) {
        List<Block> blocks = new ArrayList<>();
        Wrap wrap = Wrap.createWrap(wrapType, false);
        Alignment alignment = shouldAlign ? Alignment.createAlignment() : null;
        for (ASTNode child : getNode().getChildren(TokenSet.ANY)) {
            IElementType elementType = child.getElementType();
            if (FormatterUtil.isWhitespaceOrEmpty(child)) {
                continue;
            }
            if (TokenSet.create(left, right).contains(elementType)) {
                // Parentheses should not be aligned, wrapped or indented.
                blocks.add(createBlock(child, Indent.getNoneIndent(), null, null));
            } else if (TokenSet.create(COMMA, COMMENT).contains(elementType)) {
                // Commas should not be aligned or wrapped, but should be indented.
                blocks.add(createBlock(child, Indent.getContinuationWithoutFirstIndent(), null, null));
            } else {
                // Everything else should be aligned, wrapped and indented, unless it's the first element.
                blocks.add(createBlock(child, Indent.getContinuationWithoutFirstIndent(), wrap, alignment));
            }
        }
        return blocks;
    }

    private @Nullable Alignment getChildAlignment() {
        if (getNode().getElementType() == BINARY_EXPRESSION) {
            if (commonSettings.ALIGN_MULTILINE_BINARY_OPERATION) {
                return Alignment.createAlignment();
            }
        }
        return null;
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
        if (indentOptions == null) {
            return null;
        }
        if (RapidTokenSets.KEYWORDS.contains(elementType)) {
            // Do not indent the keyword, as the keyword might create a new symbol, which itself would be indented.
            return Indent.getNoneIndent();
        }
        if (elementType == COMMENT) {
            // If the comment is at the start of the element, it should not be indented.
            ASTNode keyword = child.getTreeParent().findChildByType(RapidTokenSets.KEYWORDS);
            if (keyword != null) {
                if (child.getStartOffsetInParent() < keyword.getStartOffsetInParent()) {
                    return Indent.getNoneIndent();
                }
            }
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
        if (elementType == STATEMENT_LIST) {
            RapidStatementList statementList = child.getPsi(RapidStatementList.class);
            if (statementList.getAttribute() != StatementListType.STATEMENT_LIST) {
                return customSettings.INDENT_ROUTINE_STATEMENT_LIST ? Indent.getNormalIndent() : Indent.getNoneIndent();
            }
        }
        if (TokenSet.create(IF_STATEMENT, FOR_STATEMENT, WHILE_STATEMENT, TEST_CASE_STATEMENT, ROUTINE).contains(parentType)) {
            // These elements contain a statement list (or a test-case), which will indent its children.
            return Indent.getNoneIndent();
        }
        if (parentType == RECORD) {
            return Indent.getNormalIndent();
        }
        if (parentType == STATEMENT_LIST) {
            return Indent.getNormalIndent();
        }
        if (parentType == FIELD || parentType == ALIAS) {
            // VAR string field :=
            //     "Hello, World!";
            return Indent.getContinuationWithoutFirstIndent(indentOptions.USE_RELATIVE_INDENTS);
        }
        return null;
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
        if (parentType == FIELD || parentType == ALIAS) {
            return Indent.getContinuationWithoutFirstIndent(indentOptions.USE_RELATIVE_INDENTS);
        }
        return null;
    }

    @Override
    public @Nullable Indent getIndent() {
        return indent;
    }
}