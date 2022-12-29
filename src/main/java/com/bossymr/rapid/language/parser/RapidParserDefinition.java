package com.bossymr.rapid.language.parser;

import com.bossymr.rapid.language.lexer.RapidLexer;
import com.bossymr.rapid.language.psi.RapidElementType;
import com.bossymr.rapid.language.psi.RapidStubElementType;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.impl.RapidFileImpl;
import com.bossymr.rapid.language.psi.stubs.RapidFileStub;
import com.bossymr.rapid.language.psi.stubs.type.RapidFileElementType;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageUtil;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public class RapidParserDefinition implements ParserDefinition {

    public static final IStubFileElementType<RapidFileStub> FILE = new RapidFileElementType();

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new RapidLexer();
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return new RapidParser();
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public @NotNull TokenSet getWhitespaceTokens() {
        return TokenSet.create(TokenType.WHITE_SPACE);
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return TokenSet.create(RapidTokenTypes.COMMENT);
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return TokenSet.create(RapidTokenTypes.STRING_LITERAL);
    }

    @Override
    public @NotNull PsiElement createElement(@NotNull ASTNode node) {
        final IElementType type = node.getElementType();
        if (type instanceof RapidElementType elementType) {
            return elementType.createElement(node);
        }
        if (type instanceof RapidStubElementType<?, ?> elementType) {
            return elementType.createElement(node);
        }
        throw new IllegalArgumentException("Cannot create element for '" + node.getElementType() + "'");
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new RapidFileImpl(viewProvider);
    }

    @Override
    public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        if (left.getElementType() == RapidTokenTypes.COMMENT) return SpaceRequirements.MUST_LINE_BREAK;
        return LanguageUtil.canStickTokensTogetherByLexer(left, right, new RapidLexer());
    }
}
