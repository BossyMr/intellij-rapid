package com.bossymr.network.client.response;

import com.bossymr.network.ResponseConverter;
import com.bossymr.network.ResponseConverterFactory;
import com.bossymr.network.client.GenericType;
import com.bossymr.network.client.NetworkManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.http.HttpResponse;
import java.util.Optional;

public class StringConverter implements ResponseConverter<String> {

    public static final ResponseConverterFactory FACTORY = new ResponseConverterFactory() {
        @SuppressWarnings("unchecked")
        @Override
        public <T> ResponseConverter<T> create(@NotNull NetworkManager manager, @NotNull HttpResponse<?> response, @NotNull GenericType<T> type) {
            if (type.getRawType().equals(String.class)) {
                Optional<String> optional = response.headers().firstValue("Content-Type");
                if (optional.isPresent() && !(optional.orElseThrow().equals("text/plain"))) {
                    return null;
                }
                return (ResponseConverter<T>) new StringConverter();
            }
            return null;
        }
    };

    @Override
    public @Nullable String convert(@NotNull HttpResponse<byte[]> response) {
        return new String(response.body());
    }
}
