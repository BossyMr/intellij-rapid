package io.github.bossymr.language.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageUtil;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import io.github.bossymr.language.RapidLanguage;
import io.github.bossymr.language.lexer.RapidLexer;
import io.github.bossymr.language.psi.RapidElementTypes;
import io.github.bossymr.language.psi.RapidFile;
import io.github.bossymr.language.psi.RapidTokenType;
import org.jetbrains.annotations.NotNull;

public class RapidParserDefinition implements ParserDefinition {

    public static final IFileElementType FILE = new IFileElementType(RapidLanguage.INSTANCE);

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
        return RapidTokenType.WHITE_SPACES;
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return RapidTokenType.COMMENTS;
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return RapidTokenType.STRING_LITERALS;
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        return RapidElementTypes.Factory.createElement(node);
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new RapidFile(viewProvider);
    }

    @Override
    public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        if (left.getElementType() == RapidElementTypes.COMMENT) return SpaceRequirements.MUST_LINE_BREAK;
        return LanguageUtil.canStickTokensTogetherByLexer(left, right, new RapidLexer());
    }
}
