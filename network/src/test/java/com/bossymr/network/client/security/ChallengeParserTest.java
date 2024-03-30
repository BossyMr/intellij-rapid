package com.bossymr.network.client.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class ChallengeParserTest {

    private void assertEquals(List<Challenge> expected, String input) {
        List<Challenge> actual = Challenge.getChallenges(input);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void simpleChallenge() {
        assertEquals(List.of(new Challenge("Basic", Map.of("realm", "foo"))),
                "Basic realm=\"foo\"");
    }

    @Test
    void challengeWithUpperCase() {
        assertEquals(List.of(new Challenge("Basic", Map.of("realm", "foo"))),
                "BASIC REALM=\"foo\"");
    }

    @Test
    void challengeWithWhitespace() {
        assertEquals(List.of(new Challenge("Basic", Map.of("realm", "foo"))),
                "Basic realm = \"foo\"");
    }

    @Test
    void challengeWithEscapeSequence() {
        assertEquals(List.of(new Challenge("Basic", Map.of("realm", "foo"))),
                "Basic realm=\"\\f\\o\\o\"");
    }

    @Test
    void challengeWithEscapeQuote() {
        assertEquals(List.of(new Challenge("Basic", Map.of("realm", "\"foo\""))),
                "Basic realm=\"\\\"foo\\\"\"");
    }

    @Test
    void challengeWithMultipleParameters() {
        assertEquals(List.of(new Challenge("Basic", Map.of("realm", "foo", "bar", "xyz", "a", "b", "c", "d"))),
                "Basic realm=\"foo\", bar=\"xyz\",, a=b,,,c=d");
    }

    @Test
    void multipleChallengesWithParameters() {
        assertEquals(List.of(new Challenge("Basic", Map.of("realm", "basic")), new Challenge("Newauth", Map.of("realm", "newauth"))),
                "Basic realm=\"basic\", Newauth realm=\"newauth\"");
    }

    @Test
    void multipleChallenges() {
        assertEquals(List.of(new Challenge("Basic", Map.of("realm", "basic")), new Challenge("Newauth")),
                "Basic realm=\"basic\", Newauth");
    }

    @Test
    void emptyChallenge() {
        assertEquals(List.of(new Challenge("Basic", Map.of("realm", "basic"))),
                ",Basic realm=\"basic\"");
    }
}