package com.bossymr.network.client.security;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChallengeParser {

    private final String text;
    private final List<Challenge> challenges = new ArrayList<>();

    private ParserState state = new SchemeState();
    private int currentIndex;

    private ChallengeParser(@NotNull String text) {
        this.text = text;
    }

    public static @NotNull List<Challenge> parse(@NotNull String text) {
        ChallengeParser parser = new ChallengeParser(text);
        return parser.parse();
    }

    public @NotNull List<Challenge> parse() {
        while (currentIndex < text.length()) {
            state = state.parse();
            if (state == null) {
                return challenges;
            }
        }
        return challenges;
    }

    private void skipWhitespace() {
        while (currentIndex < text.length()) {
            char character = text.charAt(currentIndex);
            if (Character.isWhitespace(character)) {
                currentIndex += 1;
            } else {
                break;
            }
        }
    }

    private int indexOf(char... characters) {
        int index = -1;
        for (char character : characters) {
            int value = text.indexOf(character, currentIndex);
            if (value >= 0 && (index < 0 || value < index)) {
                index = value;
            }
        }
        return index;
    }

    private @NotNull String nextWord(char... delimiters) {
        skipWhitespace();
        int endIndex = indexOf(delimiters);
        if (endIndex < 0) {
            String word = text.substring(currentIndex);
            currentIndex = text.length();
            return word;
        }
        String word = text.substring(currentIndex, endIndex);
        currentIndex = endIndex;
        return word;
    }

    private boolean complete() {
        skipWhitespace();
        return currentIndex >= text.length();
    }

    private boolean current(char... characters) {
        skipWhitespace();
        return character(currentIndex, characters);
    }

    private boolean next(char... characters) {
        skipWhitespace();
        return character(currentIndex + 1, characters);
    }

    private boolean character(int index, char... characters) {
        if (index >= text.length()) {
            return false;
        }
        char current = text.charAt(index);
        for (char character : characters) {
            if (character == current) {
                return true;
            }
        }
        return false;
    }

    public interface ParserState {
        @Nullable ParserState parse();
    }

    public final class SchemeState implements ParserState {
        @Override
        public @Nullable ParserState parse() {
            while (current(',')) {
                currentIndex += 1;
            }
            String scheme = nextWord(' ', '\t', ',');
            if (scheme.isEmpty()) {
                return null;
            }
            if (current(',')) {
                challenges.add(new Challenge(scheme));
                return new SchemeState();
            }
            if (complete()) {
                challenges.add(new Challenge(scheme));
                return null;
            }
            return new ValueState(scheme);
        }
    }

    public final class ValueState implements ParserState {

        private final String scheme;

        public ValueState(@NotNull String scheme) {
            this.scheme = scheme;
        }

        @Override
        public @Nullable ParserState parse() {
            String value = nextWord(' ', '\t', '=', ',');
            if (current(',')) {
                challenges.add(new Challenge(scheme, value));
                return new SchemeState();
            }
            if (complete()) {
                challenges.add(new Challenge(scheme, value));
                return null;
            }
            if (current('=') && next('=')) {
                StringBuilder suffix = new StringBuilder();
                while (current('=')) {
                    suffix.append('=');
                    currentIndex += 1;
                }
                challenges.add(new Challenge(scheme, value + suffix));
                return complete() ? null : new SchemeState();
            }
            assert current('=');
            currentIndex += 1; // Skip the equals sign
            return new ParameterValueState(scheme, new HashMap<>(), value);
        }
    }

    public final class ParameterValueState implements ParserState {

        private final String scheme;
        private final Map<String, String> parameters;
        private final String key;

        public ParameterValueState(@NotNull String scheme, @NotNull Map<String, String> parameters, @NotNull String key) {
            this.scheme = scheme;
            this.parameters = parameters;
            this.key = key;
        }

        @Override
        public @Nullable ParserState parse() {
            if (current('"')) {
                currentIndex += 1; // Skip the question mark
                StringBuilder value = new StringBuilder();
                while (true) {
                    String word = nextWord('\\', '"');
                    value.append(word);
                    if (current('\\')) {
                        assert currentIndex + 1 < text.length();
                        value.append(text.charAt(currentIndex + 1));
                        currentIndex += 2;
                    } else if (current('"')) {
                        currentIndex += 1;
                        break;
                    }
                }
                parameters.put(key, value.toString());

            } else {
                String value = nextWord(' ', '\t', ',');
                parameters.put(key, value);
            }
            if (complete()) {
                challenges.add(new Challenge(scheme, parameters));
                return null;
            }
            while (current(',')) {
                currentIndex += 1; // Skip the comma
            }
            return new ParameterKeyState(scheme, parameters);
        }
    }

    public final class ParameterKeyState implements ParserState {

        private final String scheme;
        private final Map<String, String> parameters;

        public ParameterKeyState(@NotNull String scheme, @NotNull Map<String, String> parameters) {
            this.scheme = scheme;
            this.parameters = parameters;
        }

        @Override
        public @NotNull ParserState parse() {
            int startIndex = currentIndex;
            String key = nextWord(' ', '\t', '=', ',');
            if (complete() || !current('=')) {
                currentIndex = startIndex;
                challenges.add(new Challenge(scheme, parameters));
                return new SchemeState();
            }
            currentIndex += 1; // Skip the equals sign
            return new ParameterValueState(scheme, parameters, key);
        }
    }
}
