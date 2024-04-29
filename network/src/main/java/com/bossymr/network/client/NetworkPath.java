package com.bossymr.network.client;

import com.bossymr.network.MultiMap;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.StringJoiner;

public class NetworkPath {

    private final MultiMap<String, String> arguments = new MultiMap<>();
    private final URI path;

    public NetworkPath(@NotNull URI path) {
        String query = path.getQuery();
        if (query == null) {
            this.path = path;
            return;
        }
        for (String argument : query.split("&")) {
            String[] strings = argument.split("=");
            if (strings.length == 1) {
                arguments.put(strings[0].strip(), null);
            }
            String value = argument.substring(strings[0].length() + 1);
            arguments.put(strings[0].strip(), value);
        }
        try {
            this.path = new URI(path.getScheme(), path.getUserInfo(), path.getHost(), path.getPort(), path.getPath(), null, path.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URI is invalid: " + path);
        }
    }

    public @NotNull MultiMap<String, String> getArguments() {
        return arguments;
    }

    public @NotNull URI getPath() {
        StringJoiner query = new StringJoiner("&");
        for (Map.Entry<String, String> entry : arguments.entrySet()) {
            if (entry.getValue() == null) {
                query.add(entry.getKey());
            } else {
                query.add(entry.getKey() + "=" + entry.getValue());
            }
        }
        try {
            return new URI(path.getScheme(), path.getUserInfo(), path.getHost(), path.getPort(), path.getPath(), query.toString(), path.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URI is invalid: " + path);
        }
    }

    @Override
    public String toString() {
        URI path = getPath();
        return path.toString();
    }
}
