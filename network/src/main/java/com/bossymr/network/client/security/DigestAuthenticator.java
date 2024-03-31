package com.bossymr.network.client.security;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

public class DigestAuthenticator implements Authenticator {

    private final Credentials credentials;
    private final String unique;

    private boolean isProxy;
    private int usageCounter = 0;
    private Challenge challenge;

    public DigestAuthenticator(@NotNull Credentials credentials) {
        this.credentials = credentials;
        this.unique = generate();
    }

    private @NotNull String generate() {
        SecureRandom random = new SecureRandom();
        final byte[] input = new byte[16];
        random.nextBytes(input);
        return encode(input);
    }

    private @NotNull String digest(MessageDigest digest, String value) {
        return encode(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
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

    @Override
    public @Nullable HttpRequest authenticate(@NotNull HttpRequest request) {
        if (challenge == null) {
            // This authenticator has not been initialized, i.e. #authenticate(HttpResponse<?>) has not been called.
            return null;
        }
        Map<String, String> response = getResponse(request);
        if (response == null) {
            // This authenticator could not authenticate this request.
            return null;
        }
        String headerName = isProxy ? "Proxy-Authorization" : "Authorization";
        String headerValue = "Digest " + response.entrySet().stream()
                                                 .map(entry -> entry.getKey() + "=" + switch (entry.getKey()) {
                                                     case "nc", "qop", "algorithm" -> entry.getValue();
                                                     default -> "\"" + entry.getValue() + "\"";
                                                 })
                                                 .collect(Collectors.joining(","));
        return HttpRequest.newBuilder(request, (key, value) -> !(key.equalsIgnoreCase(headerName)))
                          .header(headerName, headerValue)
                          .build();
    }

    private @Nullable Map<String, String> getResponse(@NotNull HttpRequest request) {
        String algorithm = challenge.getParameter("algorithm");
        String realm = challenge.getParameter("realm");
        String nonce = challenge.getParameter("nonce");
        String opaque = challenge.getParameter("opaque");
        if (realm == null || nonce == null || opaque == null) {
            return null;
        }
        if (algorithm == null) {
            return null;
        }
        boolean isSessionDigest = algorithm.endsWith("-sess");
        if (isSessionDigest) {
            algorithm = algorithm.substring(0, algorithm.length() - "-sess".length());
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        String counter = Integer.toHexString(usageCounter);
        usageCounter += 1;
        String A1 = digest(digest, getA1(digest, isSessionDigest));
        String A2 = digest(digest, getA2(request));
        String response = digest(digest, A1 + ":" + nonce + ":" + counter + ":" + unique + ":" + "auth" + ":" + A2);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("response", response);
        parameters.put("username", credentials.username());
        parameters.put("realm", realm);
        parameters.put("uri", request.uri().getPath());
        parameters.put("algorithm", algorithm);
        parameters.put("nonce", nonce);
        parameters.put("nc", counter);
        parameters.put("qop", "auth");
        parameters.put("cnonce", unique);
        parameters.put("opaque", opaque);
        return parameters;
    }

    private @NotNull String getA1(@NotNull MessageDigest digest, boolean isSessionDigest) {
        if (isSessionDigest) {
            return digest(digest, credentials.username() + ":" + challenge.getParameter("realm") + ":" + new String(credentials.password())) + ":" + challenge.getParameter("nonce") + ":" + unique;
        } else {
            return credentials.username() + ":" + challenge.getParameter("realm") + ":" + new String(credentials.password());
        }
    }

    private @NotNull String getA2(@NotNull HttpRequest request) {
        return request.method() + ":" + request.uri().getPath();
    }

    @Override
    public @Nullable HttpRequest authenticate(@NotNull HttpResponse<?> response) {
        String headerName = response.statusCode() == 401 ? "Authorization" : "Proxy-Authorization";
        HttpRequest previousRequest = response.request();
        boolean wasAuthenticated = previousRequest.headers().firstValue(headerName).isPresent();
        if (wasAuthenticated) {
            Challenge challenge = getChallenge(response);
            if (challenge == null) {
                return null;
            }
            if (Boolean.parseBoolean(challenge.getParameter("stale"))) {
                this.challenge = challenge;
                return authenticate(previousRequest);
            }
            return null;
        }
        Challenge challenge = getChallenge(response);
        if (challenge == null) {
            return null;
        }
        this.challenge = challenge;
        this.usageCounter = 0;
        this.isProxy = response.statusCode() == 407;
        return authenticate(previousRequest);
    }

    private @Nullable Challenge getChallenge(@NotNull HttpResponse<?> response) {
        for (Challenge challenge : getChallenges(response)) {
            if (challenge.getScheme().equals("digest")) {
                String quality = challenge.getParameter("qop");
                if (quality == null) {
                    continue;
                }
                Set<String> values = Arrays.stream(quality.split(","))
                                           .map(String::trim)
                                           .collect(Collectors.toSet());
                if (values.contains("auth")) {
                    return challenge;
                }
            }
        }
        return null;
    }

    private @NotNull List<Challenge> getChallenges(@NotNull HttpResponse<?> response) {
        String headerName = response.statusCode() == 401 ? "WWW-Authenticate" : "Proxy-Authenticate";
        List<Challenge> challenges = new ArrayList<>();
        for (String header : response.headers().allValues(headerName)) {
            challenges.addAll(Challenge.getChallenges(header));
        }
        return challenges;
    }
}
