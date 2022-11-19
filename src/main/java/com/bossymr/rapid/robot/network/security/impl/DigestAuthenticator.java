package com.bossymr.rapid.robot.network.security.impl;

import com.bossymr.rapid.robot.network.security.Authenticator;
import com.bossymr.rapid.robot.network.security.Challenge;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Supplier;

public class DigestAuthenticator implements Authenticator {
    private static final Logger LOG = Logger.getInstance(DigestAuthenticator.class);

    private final Supplier<Credentials> supplier;
    private final String unique;

    private boolean isProxy;
    private Credentials credentials;
    private Challenge challenge;
    private int usages = 0;

    public DigestAuthenticator(@NotNull Supplier<Credentials> supplier) {
        this.supplier = supplier;
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
        if (challenge.values().containsKey("qop")) {
            Set<String> qualities = new HashSet<>(Arrays.asList(challenge.values().get("qop").split(",")));
            if (qualities.contains("auth-int")) return "auth-int";
            if (qualities.contains("auth")) return "auth";
        }
        return null;
    }

    private @NotNull Charset getCharset() {
        String charset = challenge.values().get("charset");
        return charset != null ? Charset.forName(charset) : StandardCharsets.UTF_8;
    }

    @Override
    public @Nullable HttpRequest authenticate(@NotNull HttpRequest request) {
        if (challenge == null) return null;
        Charset charset = getCharset();
        String algorithm = challenge.values().getOrDefault("algorithm", "MD5");
        boolean session = algorithm.endsWith("-sess");
        if (session) algorithm = algorithm.substring(0, algorithm.length() - "-sess".length());
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        usages += 1;
        String path = Objects.requireNonNull(request.uri().getPath());
        String realm = Objects.requireNonNull(challenge.values().get("realm"));
        String nonce = Objects.requireNonNull(challenge.values().get("nonce"));
        String nc = String.format("%08X", usages);
        String A1, A2;
        if (session) {
            A1 = digest(digest, credentials.getUserName() + ":" + realm + ":" + Objects.requireNonNull(credentials.getPasswordAsString()), charset);
            A1 += ":" + nonce + ":" + unique;
        } else {
            A1 = credentials.getUserName() + ":" + realm + ":" + Objects.requireNonNull(credentials.getPasswordAsString());
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
        parameters.put("username", credentials.getUserName());
        parameters.put("realm", realm);
        parameters.put("uri", path);
        parameters.put("algorithm", algorithm);
        parameters.put("nonce", nonce);
        parameters.put("nc", nc);
        if (quality != null) {
            parameters.put("qop", quality);
            parameters.put("cnonce", unique);
        }
        parameters.put("opaque", Objects.requireNonNull(challenge.values().get("opaque")));
        String name = isProxy ? "Proxy-Authorization" : "Authorization";
        String output = parse(parameters);
        return HttpRequest.newBuilder(request, (n, v) -> true)
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

    @Override
    public @Nullable HttpRequest authenticate(@NotNull HttpResponse<?> response) {
        if (response.request().headers().firstValue("Proxy-Authorization").isPresent() || response.request().headers().firstValue("Authorization").isPresent()) {
            // The specified response was already authenticated.
            Challenge challenge = findChallenge(response);
            if (challenge != null) {
                Map<String, String> arguments = challenge.values();
                if (Boolean.parseBoolean(arguments.get("stale"))) {
                    // Calculate new authorization with previous credentials.
                    this.challenge = challenge;
                } else {
                    // Calculate new authorization with new credentials.
                    this.isProxy = response.statusCode() == 407;
                    setChallenge(challenge);
                }
                return authenticate(response.request());
            } else {
                LOG.error("Failed to find new challenge to: " + response);
            }
        } else {
            // The specified response was not authenticated.
            // This authenticator should be updated to use a new challenge.
            for (Challenge challenge : Challenge.challenges(response)) {
                this.isProxy = response.statusCode() == 407;
                setChallenge(challenge);
                return authenticate(response.request());
            }
        }
        return null;
    }

    private void setChallenge(@NotNull Challenge challenge) {
        this.challenge = challenge;
        this.credentials = supplier.get();
        this.usages = 0;
    }

    private @Nullable Challenge findChallenge(@NotNull HttpResponse<?> response) {
        for (Challenge challenge : Challenge.challenges(response)) {
            if (challenge.scheme().equals(this.challenge.scheme())) {
                return challenge;
            }
        }
        return null;
    }
}
