package com.bossymr.network.client.security.impl;

import org.jetbrains.annotations.NotNull;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@code Challenge} represents an authentication challenge.
 *
 * @param scheme the authentication scheme.
 * @param values the authentication values.
 */
public record Challenge(@NotNull String scheme, @NotNull Map<String, String> values) {

    /**
     * Finds all challenges in the specified unsuccessful response. The correctness of this method should not depend on
     * as it might not be correct.
     *
     * @param response the response.
     * @return the challenges in the specified response.
     * @throws IllegalArgumentException if the specified response is successful.
     */
    public static @NotNull List<Challenge> challenges(@NotNull HttpResponse<?> response) {
        List<Challenge> challenges = new ArrayList<>();
        if (response.statusCode() != 401 && response.statusCode() != 407) throw new IllegalArgumentException();
        String type = response.statusCode() == 401 ?
                "WWW-Authenticate" :
                "Proxy-Authenticate";
        for (String header : response.headers().allValues(type)) {
            String scheme = header.split(" ")[0];
            String arguments = header.substring(scheme.length() + 1).strip();
            Map<String, String> values = getSections(arguments);
            challenges.add(new Challenge(scheme, values));
        }
        return challenges;
    }

    private static @NotNull Map<String, String> getSections(@NotNull String arguments) throws IllegalArgumentException {
        Map<String, String> sections = new HashMap<>();
        Pattern pattern = Pattern.compile("([^ \"]*)=((\"([^\"]*)\")|([^ ]*))");
        Matcher matcher = pattern.matcher(arguments);
        matcher.results().forEach(result -> {
            String group = result.group();
            String name = group.split("=")[0];
            String value = group.substring(name.length() + 1);
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            sections.put(name, value);
        });
        return sections;
    }

}
