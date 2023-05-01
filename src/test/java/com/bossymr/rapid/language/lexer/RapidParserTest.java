package com.bossymr.rapid.language.lexer;

import com.bossymr.rapid.language.parser.RapidParserDefinition;
import com.intellij.testFramework.ParsingTestCase;

public class RapidParserTest extends ParsingTestCase {

    public RapidParserTest() {
        super("com/bossymr/rapid/language/parser", "mod", new RapidParserDefinition());
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testModules() {
        doTest(true);
    }

    public void testFields() {
        doTest(true);
    }

    public void testStructures() {
        doTest(true);
    }

    public void testRoutines() {
        doTest(true);
    }
}
