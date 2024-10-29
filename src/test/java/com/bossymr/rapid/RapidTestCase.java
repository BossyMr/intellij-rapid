package com.bossymr.rapid;

import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class RapidTestCase {

    private CodeInsightTestFixture fixture;

    @BeforeEach
    void setUp() throws Exception {
        LightProjectDescriptor descriptor = LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR;
        IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
        IdeaProjectTestFixture ideaFixture = factory
                .createLightFixtureBuilder(descriptor, "Rapid")
                .getFixture();
        fixture = factory.createCodeInsightFixture(ideaFixture);
        fixture.setUp();
    }

    @AfterEach
    void tearDown() throws Exception {
        fixture.tearDown();
    }

    private String getTestDataPath() {
        return "src/test/resources/com/bossymr/rapid/";
    }

    public CodeInsightTestFixture getFixture() {
        return fixture;
    }
}
