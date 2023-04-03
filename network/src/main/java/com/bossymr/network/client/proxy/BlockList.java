package com.bossymr.network.client.proxy;

import com.bossymr.network.client.NetworkManager;
import com.bossymr.network.client.ResponseModel;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class BlockList<T> extends AbstractList<Block<T>> {

    private final @NotNull Block<T> @NotNull [] sections;

    @SuppressWarnings("unchecked")
    public BlockList(@NotNull NetworkManager manager, @NotNull Class<T> entityType, @NotNull ResponseModel model) {
        URI path = model.model().reference("last");
        if (path == null) {
            this.sections = new Block[1];
            this.sections[0] = new Block<>(manager, entityType, model);
        } else {
            Map<String, String> query = Arrays.stream(path.getQuery().split("&amp;|&"))
                    .map(value -> value.split("="))
                    .collect(Collectors.toMap(value -> value[0], value -> value[1]));
            int start = Integer.parseInt(query.get("start"));
            int limit = Integer.parseInt(query.get("limit"));
            int sections = start / model.entities().size();
            this.sections = new Block[sections + 1];
            for (int i = 0; i < sections; i++) {
                this.sections[i] = new Block<>(manager, entityType, model.entities().size());
            }
            this.sections[sections] = new Block<>(manager, entityType, limit);
        }
    }

    @Override
    public @NotNull Block<T> get(int index) {
        return sections[index];
    }

    @Override
    public int size() {
        return sections.length;
    }
}
