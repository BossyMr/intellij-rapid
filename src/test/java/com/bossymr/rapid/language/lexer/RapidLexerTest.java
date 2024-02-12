package com.bossymr.rapid.language.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.testFramework.LexerTestCase;
import org.jetbrains.annotations.NotNull;

public class RapidLexerTest extends LexerTestCase {

    @Override
    protected @NotNull String getPathToTestDataFile(@NotNull String extension) {
        return getDirPath() + "/" + getTestName(true) + extension;
    }

    @Override
    protected void doFileTest(@NotNull String fileExt) {
        super.doFileTest(fileExt);
    }

    @Override
    protected @NotNull Lexer createLexer() {
        return new RapidLexer();
    }

    @Override
    protected @NotNull String getDirPath() {
        return "src/test/resources/com/bossymr/rapid/language/lexer";
    }

    public void testKeywords() {
        doFileTest("mod");
    }

    public void testNumbers() {
        doFileTest("mod");
    }
}
