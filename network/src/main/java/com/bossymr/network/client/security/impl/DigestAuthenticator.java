package com.bossymr.network.client.security.impl;

import com.bossymr.network.client.security.Credentials;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

public class DigestAuthenticator implements Authenticator {
    private final String unique;
    private final @Nullable Credentials credentials;
    private boolean isProxy;
    private Challenge challenge;
    private int usages = 0;

    public DigestAuthenticator(@Nullable Credentials credentials) {
        this.credentials = credentials;
        this.unique = generate();
    }

    private @NotNull String generate() {
        SecureRandom random = new SecureRandom();
        final byte[] input = new byte[16];
        random.nextBytes(input);
        return encode(input);
    }

    private @NotNull String digest(MessageDigest digest, String value, Charset charset) {
        return encode(digest.digest(value.getBytes(charset)));
    }

    private @NotNull String encode(byte @NotNull [] input) {
        final char[] HEXADECIMAL = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        int length = input.length;
        final char[] output = new char[length * 2];
        for (int i = 0; i < length; i++) {
            final int low = (input[i] & 0x0f);
            final int high = ((input[i] & 0xf0) >> 4);
            output[i * 2] = HEXADECIMAL[high];
            output[(i * 2) + 1] = HEXADECIMAL[low];
        }
        return new String(output);
    }

    private @Nullable String getQuality() {
        if (challenge.authParams().containsKey("qop")) {
            Set<String> qualities = new HashSet<>(Arrays.asList(challenge.authParams().get("qop").split(",")));
            if (qualities.contains("auth-int")) return "auth-int";
            if (qualities.contains("auth")) return "auth";
        }
        return null;
    }

    private @NotNull Charset getCharset() {
        String charset = challenge.authParams().get("charset");
        return charset != null ? Charset.forName(charset) : StandardCharsets.UTF_8;
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NotNull Response response) {
        for (Challenge challenge : response.challenges()) {
            // If this is preemptive auth, use a preemptive credential.
            if (challenge.scheme().equalsIgnoreCase("OkHttp-Preemptive")) {
                return authenticate(response.request());
            }
        }
        return authenticate(response);
    }

    public @Nullable Request authenticate(@NotNull Request request) {
        if (challenge == null || credentials == null) return null;
        Charset charset = getCharset();
        String algorithm = challenge.authParams().getOrDefault("algorithm", "MD5");
        boolean session = algorithm.endsWith("-sess");
        if (session) algorithm = algorithm.substring(0, algorithm.length() - "-sess".length());
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        usages += 1;
        String path = Objects.requireNonNull(request.url().uri().getPath());
        String realm = Objects.requireNonNull(challenge.authParams().get("realm"));
        String nonce = Objects.requireNonNull(challenge.authParams().get("nonce"));
        String nc = String.format("%08X", usages);
        String A1, A2;
        if (session) {
            A1 = digest(digest, credentials.username() + ":" + realm + ":" + new String(credentials.password()), charset);
            A1 += ":" + nonce + ":" + unique;
        } else {
            A1 = credentials.username() + ":" + realm + ":" + new String(credentials.password());
        }
        A1 = digest(digest, A1, charset);
        String quality = getQuality();
        A2 = request.method() + ":" + path;
        A2 = digest(digest, A2, charset);
        String response;
        if (quality != null) {
            response = A1 + ":" + nonce + ":" + nc + ":" + unique + ":" + quality + ":" + A2;
        } else {
            response = A1 + ":" + nonce + ":" + A2;
        }
        response = digest(digest, response, charset);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("response", response);
        parameters.put("username", credentials.username());
        parameters.put("realm", realm);
        parameters.put("uri", path);
        parameters.put("algorithm", algorithm);
        parameters.put("nonce", nonce);
        parameters.put("nc", nc);
        if (quality != null) {
            parameters.put("qop", quality);
            parameters.put("cnonce", unique);
        }
        parameters.put("opaque", Objects.requireNonNull(challenge.authParams().get("opaque")));
        String name = isProxy ? "Proxy-Authorization" : "Authorization";
        String output = parse(parameters);
        return new Request.Builder(request)
                .header(name, output)
                .build();
    }

    private String parse(Map<String, String> parameters) {
        StringJoiner joiner = new StringJoiner(", ");
        for (var entry : parameters.entrySet()) {
            boolean quote = !(Set.of("nc", "qop", "algorithm").contains(entry.getKey()));
            String value = entry.getKey();
            if (entry.getValue() != null) {
                value += "=";
                if (quote) {
                    value += "\"";
                    value += entry.getValue();
                    value += "\"";
                } else {
                    value += entry.getValue();
                }
            }
            joiner.add(value);
        }
        return "Digest" + " " + joiner;
    }

    public @Nullable Request authenticate(@NotNull Response response) {
        if (response.request().header("Proxy-Authorization") != null || response.request().header("Authorization") != null) {
            // The specified response was already authenticated.
            Challenge challenge = findChallenge(response);
            if (challenge != null) {
                Map<String, String> arguments = challenge.authParams();
                if (Boolean.parseBoolean(arguments.get("stale"))) {
                    // Calculate new authorization with previous credentials.
                    this.challenge = challenge;
                } else {
                    // Calculate new authorization with new credentials.
                    this.isProxy = response.code() == 407;
                    setChallenge(challenge);
                }
                return authenticate(response.request());
            }
        } else {
            // The specified response was not authenticated.
            // This authenticator should be updated to use a new challenge.
            for (Challenge challenge : response.challenges()) {
                this.isProxy = response.code() == 407;
                setChallenge(challenge);
                return authenticate(response.request());
            }
        }
        return null;
    }

    private void setChallenge(@NotNull Challenge challenge) {
        this.challenge = challenge;
        this.usages = 0;
    }

    private @Nullable Challenge findChallenge(@NotNull Response response) {
        for (Challenge challenge : response.challenges()) {
            if (challenge.scheme().equals(this.challenge.scheme())) {
                return challenge;
            }
        }
        return null;
    }
}
