package com.bossymr.rapid.network.client;

import com.bossymr.rapid.network.NetworkQuery;
import com.bossymr.rapid.network.client.model.Model;
import com.burgstaller.okhttp.AuthenticationCacheInterceptor;
import com.burgstaller.okhttp.CachingAuthenticatorDecorator;
import com.burgstaller.okhttp.digest.CachingAuthenticator;
import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class NetworkClient {

    private final URI path;
    private final OkHttpClient httpClient;

    public NetworkClient(@NotNull URI path, @NotNull String username, char @NotNull [] password) {
        this.path = path;
        final Credentials credentials = new Credentials(username, new String(password));
        final DigestAuthenticator digestAuthenticator = new DigestAuthenticator(credentials);
        final Map<String, CachingAuthenticator> authenticatorCache = new ConcurrentHashMap<>();
        this.httpClient = new OkHttpClient.Builder()
                .authenticator(new CachingAuthenticatorDecorator(digestAuthenticator, authenticatorCache))
                .addInterceptor(new AuthenticationCacheInterceptor(authenticatorCache))
                .build();
    }

    public static @NotNull NetworkClient connect(@NotNull URI path, @NotNull String username, char @NotNull [] password) {
        return new NetworkClient(path, username, password);
    }

    public @NotNull NetworkQuery<Void> getQuery(@NotNull NetworkCall networkCall) {
        return new NetworkQuery<>(path, httpClient.newCall(getRequest(networkCall)), null);
    }

    public <T> @NotNull NetworkQuery<T> getQuery(@NotNull NetworkCall networkCall, @NotNull Function<Model, T> converter) {
        return new NetworkQuery<>(path, httpClient.newCall(getRequest(networkCall)), converter);
    }

    private @NotNull Request getRequest(@NotNull NetworkCall networkCall) {
        Request.Builder builder = new Request.Builder();
        builder.url(getPath(networkCall).toString());
        builder.method(networkCall.getMethod().name(), getBody(networkCall));
        return builder.build();
    }

    private @NotNull URI getPath(@NotNull NetworkCall networkCall) {
        URI absolute = path.resolve(networkCall.getPath());
        if (networkCall.getQueries().isEmpty()) return absolute;
        StringJoiner stringJoiner = new StringJoiner("&");
        if (absolute.getQuery() != null) stringJoiner.add(absolute.getQuery());
        networkCall.getQueries().forEach((name, value) -> {
            for (String content : value) {
                stringJoiner.add(name + "=" + content);
            }
        });
        try {
            return new URI(absolute.getScheme(), absolute.getUserInfo(), absolute.getHost(), absolute.getPort(), absolute.getPath(), stringJoiner.toString(), absolute.getFragment());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private @Nullable RequestBody getBody(@NotNull NetworkCall networkCall) {
        if (networkCall.getFields().isEmpty()) return null;
        FormBody.Builder formBody = new FormBody.Builder();
        networkCall.getFields().forEach((name, value) -> {
            for (String content : value) {
                formBody.add(name, content);
            }
        });
        return formBody.build();
    }

}
