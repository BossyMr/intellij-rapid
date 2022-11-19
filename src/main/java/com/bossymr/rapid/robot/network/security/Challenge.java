package com.bossymr.rapid.robot.network.security;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Challenge(@NotNull String scheme, @NotNull Map<String, String> values) {

    public static @NotNull List<Challenge> challenges(@NotNull HttpResponse<?> response) {
        List<Challenge> challenges = new ArrayList<>();
        assert response.statusCode() == 401 || response.statusCode() == 407;
        String type = response.statusCode() == 401 ?
                "WWW-Authenticate" :
                "Proxy-Authenticate";
        for (String header : response.headers().allValues(type)) {
            String scheme = header.split(" ")[0];
            String arguments = header.substring(scheme.length() + 1).strip();
            Map<String, String> values = new HashMap<>();
            for (String argument : StringUtil.splitHonorQuotes(arguments, ',')) {
                argument = argument.strip();
                String key = argument.split("=")[0];
                String value = argument.substring(key.length() + 1).strip();
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                values.put(key, value);
            }
            challenges.add(new Challenge(scheme, values));
        }
        return challenges;
    }

}
