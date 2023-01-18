package com.bossymr.rapid.language.parser;

import com.intellij.testFramework.ParsingTestCase;
import org.jetbrains.annotations.NotNull;

public class RapidParserTest extends ParsingTestCase {

    public RapidParserTest() {
        super("com/bossymr/rapid/language/parser", "mod", true, new RapidParserDefinition());
    }

    @Override
    protected @NotNull String getTestDataPath() {
        return "src/test/resources";
    }

    public void testModules() {
        doTest(true);
    }

    public void testFields() {
        doTest(true);
    }
}
