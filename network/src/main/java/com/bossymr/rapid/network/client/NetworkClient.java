package com.bossymr.rapid.network.client;

import com.bossymr.rapid.network.client.model.EntityModel;
import com.bossymr.rapid.network.client.model.Link;
import com.bossymr.rapid.network.client.model.Model;
import com.bossymr.rapid.network.client.model.ModelFactory;
import com.burgstaller.okhttp.AuthenticationCacheInterceptor;
import com.burgstaller.okhttp.CachingAuthenticatorDecorator;
import com.burgstaller.okhttp.digest.CachingAuthenticator;
import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class NetworkClient {

    private final URI path;
    private final OkHttpClient httpClient;

    private final ModelFactory factory;

    public NetworkClient(@NotNull URI path, @NotNull String username, char @NotNull [] password) {
        this.path = path;
        this.factory = new ModelFactory(path);
        final Credentials credentials = new Credentials(username, new String(password));
        final DigestAuthenticator digestAuthenticator = new DigestAuthenticator(credentials);
        final Map<String, CachingAuthenticator> authenticatorCache = new ConcurrentHashMap<>();
        this.httpClient = new OkHttpClient.Builder()
                .authenticator(new CachingAuthenticatorDecorator(digestAuthenticator, authenticatorCache))
                .addInterceptor(new AuthenticationCacheInterceptor(authenticatorCache))
                .cookieJar(new CookieJar() {

                    private final List<Cookie> cookies = new ArrayList<>();

                    @Override
                    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> cookies) {
                        this.cookies.addAll(cookies);
                    }

                    @NotNull
                    @Override
                    public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                        cookies.removeIf(cookie -> cookie.expiresAt() < System.currentTimeMillis());
                        return cookies.stream().filter(cookie -> cookie.matches(httpUrl)).toList();
                    }
                })
                .build();
    }

    public static @NotNull NetworkClient connect(@NotNull URI path, @NotNull String username, char @NotNull [] password) {
        return new NetworkClient(path, username, password);
    }

    public @NotNull CompletableFuture<Void> send(@NotNull NetworkCall networkCall) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        httpClient.newCall(toRequest(networkCall))
                .enqueue(new NetworkCallback<>(factory, completableFuture) {
                    @Override
                    public void onResponse(@NotNull Response response) {
                        completableFuture.complete(null);
                    }
                });
        return completableFuture;
    }

    public @NotNull CompletableFuture<Model> fetch(@NotNull NetworkCall networkCall) {
        CompletableFuture<Model> completableFuture = new CompletableFuture<>();
        httpClient.newCall(toRequest(networkCall))
                .enqueue(new NetworkCallback<>(factory, completableFuture) {
                    @Override
                    public void onResponse(@NotNull Response response) throws IOException {
                        completableFuture.complete(factory.getModel(response));
                    }
                });
        return completableFuture;
    }

    public @NotNull CompletableFuture<List<EntityModel>> fetchAll(@NotNull NetworkCall networkCall) {
        return fetch(networkCall)
                .thenCompose(response -> {
                    List<EntityModel> entities = new ArrayList<>();
                    return getNext(entities, (model) -> entities.addAll(model.entities()), networkCall, response);
                });
    }

    public <T> @NotNull CompletableFuture<T> fetch(@NotNull NetworkCall networkCall, @NotNull Class<T> clazz) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        httpClient.newCall(toRequest(networkCall))
                .enqueue(new NetworkCallback<>(factory, completableFuture) {
                    @Override
                    public void onResponse(@NotNull Response response) throws IOException {
                        completableFuture.complete(factory.getEntity(response, clazz));
                    }
                });
        return completableFuture;
    }

    public <T> @NotNull CompletableFuture<List<T>> fetchAll(@NotNull NetworkCall networkCall, @NotNull Class<T> clazz) {
        return fetch(networkCall)
                .thenComposeAsync(response -> {
                    List<T> entities = new ArrayList<>();
                    return getNext(entities, (model) -> entities.addAll(factory.getList(model, clazz)) , networkCall, response);
                });
    }

    private <T> @NotNull CompletableFuture<List<T>> getNext(@NotNull List<T> entities, @NotNull Consumer<Model> consumer, @NotNull NetworkCall networkCall, @NotNull Model model) {
        consumer.accept(model);
        Optional<Link> next = model.entity().getLink("next");
        if(next.isEmpty()) return CompletableFuture.supplyAsync(() -> entities);
        URI path = next.get().path();
        NetworkCall nextCall = NetworkCall.newBuilder(networkCall)
                .setPath(path).setQuery(new HashMap<>())
                .build();
        return fetch(nextCall)
                .thenComposeAsync(response -> getNext(entities, consumer, networkCall, response));
    }

    private @NotNull Request toRequest(@NotNull NetworkCall networkCall) {
        Request.Builder builder = new Request.Builder();
        builder.url(getPath(networkCall).toString());
        builder.method(networkCall.getMethod().name(), getBody(networkCall));
        return builder.build();
    }

    private @NotNull URI getPath(@NotNull NetworkCall networkCall) {
        URI absolute = path.resolve(networkCall.getPath());
        if (networkCall.getQuery().isEmpty()) return absolute;
        StringJoiner stringJoiner = new StringJoiner("&");
        if (absolute.getQuery() != null) stringJoiner.add(absolute.getQuery());
        networkCall.getQuery().forEach((name, value) -> {
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
        if (networkCall.getField().isEmpty()) return null;
        FormBody.Builder formBody = new FormBody.Builder();
        networkCall.getField().forEach((name, value) -> {
            for (String content : value) {
                formBody.add(name, content);
            }
        });
        return formBody.build();
    }

}
