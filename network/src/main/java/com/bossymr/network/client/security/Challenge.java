package com.bossymr.network.client.security;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A {@code Challenge} represents an HTTP challenge.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7235">RFC 7235: Authentication</a>
 */
public class Challenge {

    private final @NotNull String scheme;
    private final @Nullable String value;
    private final @Nullable Map<String, String> parameters;

    /**
     * Creates a new challenge without any value.
     *
     * @param scheme the challenge scheme.
     */
    public Challenge(@NotNull String scheme) {
        this.scheme = scheme.toLowerCase();
        this.value = null;
        this.parameters = null;
    }

    /**
     * Creates a new challenge with a single value.
     *
     * @param scheme the challenge scheme.
     * @param value the value.
     */
    public Challenge(@NotNull String scheme, @NotNull String value) {
        this.scheme = scheme.toLowerCase();
        this.value = value;
        this.parameters = null;
    }

    /**
     * Creates a new challenge with the specified parameters;
     *
     * @param scheme the challenge scheme.
     * @param parameters the parameters.
     */
    public Challenge(@NotNull String scheme, @NotNull Map<String, String> parameters) {
        this.scheme = scheme.toLowerCase();
        this.value = null;
        this.parameters = new HashMap<>();
        parameters.forEach((key, value) -> this.parameters.put(key.toLowerCase(), value));
    }

    public static @NotNull List<Challenge> getChallenges(@NotNull String header) {
        return ChallengeParser.parse(header);
    }

    public @NotNull String getScheme() {
        return scheme;
    }

    public @Nullable String getValue() {
        return value;
    }

    public @Nullable String getParameter(@NotNull String parameterName) {
        return parameters != null ? parameters.get(parameterName.toLowerCase()) : null;
    }

    @Override
    public String toString() {
        if (value != null) {
            return scheme + " " + value;
        } else if (parameters != null) {
            return scheme + " " + parameters.entrySet().stream()
                                            .map(entry -> entry.getKey() + "=" + "\"" + entry.getValue() + "\"")
                                            .collect(Collectors.joining(", "));
        } else {
            return scheme;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Challenge challenge = (Challenge) o;
        return Objects.equals(scheme, challenge.scheme) && Objects.equals(value, challenge.value) && Objects.equals(parameters, challenge.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheme, value, parameters);
    }
}
