package com.bossymr.network.client.response;

import com.bossymr.network.GenericType;
import com.bossymr.network.NetworkManager;
import com.bossymr.network.ResponseConverter;
import com.bossymr.network.ResponseConverterFactory;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class StringConverter implements ResponseConverter<String> {

    public static final ResponseConverterFactory FACTORY = new ResponseConverterFactory() {
        @SuppressWarnings("unchecked")
        @Override
        public <T> ResponseConverter<T> create(@NotNull NetworkManager action, @NotNull GenericType<T> type) {
            if (type.getRawType().equals(String.class)) {
                return (ResponseConverter<T>) new StringConverter();
            }
            return null;
        }
    };

    @Override
    public @Nullable String convert(@NotNull Response response) throws IOException {
        return response.body().string();
    }
}
