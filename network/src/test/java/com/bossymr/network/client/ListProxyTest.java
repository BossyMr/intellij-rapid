package com.bossymr.network.client;

import com.bossymr.network.NetworkQuery;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okForContentType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@WireMockTest
class ListProxyTest {

    @Test
    void retrieveTest(@NotNull WireMockRuntimeInfo runtimeInfo) throws IOException, InterruptedException {
        WireMock wireMock = runtimeInfo.getWireMock();
        NetworkManager manager = NetworkManager.newBuilder(URI.create(runtimeInfo.getHttpBaseUrl())).build();
        getModels(wireMock, getElements(manager), 10);
        int requests = wireMock.getServeEvents().size();
        HttpRequest request = manager.getNetworkClient().createRequest()
                .setPath(URI.create("/elements"))
                .build();
        NetworkQuery<List<SimpleEntity>> query = manager.createQuery(new GenericType<List<SimpleEntity>>() {}, request);
        List<SimpleEntity> entities = query.get();
        assertNotNull(entities);
        assertEquals(requests, wireMock.getServeEvents().size());
        assertEquals(95, entities.size());
        assertEquals(requests + 1, wireMock.getServeEvents().size());
        assertNotNull(entities.get(0));
        assertNotNull(entities.get(9));
        assertEquals(requests + 1, wireMock.getServeEvents().size());
        assertNotNull(entities.get(entities.size() - 1));
        assertEquals(requests + 2, wireMock.getServeEvents().size());
        assertNotNull(entities.get(entities.size() - 2));
        assertEquals(requests + 2, wireMock.getServeEvents().size());
    }

    private void getModels(@NotNull WireMock wireMock, @NotNull List<EntityModel> entities, int size) {
        List<List<EntityModel>> sections = new ArrayList<>();
        int length = entities.size();
        for (int i = 0; i < length; i += size) {
            sections.add(entities.subList(i, Math.min(length, i + size)));
        }
        for (int i = 0; i < sections.size(); i++) {
            ResponseModel model = getModel(i, sections);
            if (i == 0) {
                wireMock.register(get("/elements").willReturn(okForContentType("application/xhtml+xml", model.toText())));
            }
            wireMock.register(get("/elements?start=" + getStart(i, sections) + "&limit=" + sections.get(i).size()).willReturn(okForContentType("application/xhtml+xml", model.toText())));
        }
    }

    private @NotNull ResponseModel getModel(int index, @NotNull List<List<EntityModel>> sections) {
        Map<String, URI> references = new HashMap<>();
        references.put("last", URI.create("/elements?start=" + getStart(sections.size() - 1, sections) + "&amp;limit=" + sections.get(sections.size() - 1).size()));
        references.put("self", URI.create("/elements?start=" + getStart(index, sections) + "&amp;limit=" + sections.get(index).size()));
        if ((index + 1) < sections.size()) {
            references.put("next", URI.create("/elements?start=" + getStart(index + 1, sections) + "&amp;limit=" + sections.get(index + 1).size()));
        }
        return ResponseModel.newBuilder()
                .setEntities(sections.get(index))
                .setReferences(references)
                .build();
    }

    private int getStart(int index, @NotNull List<List<EntityModel>> sections) {
        return IntStream.range(0, index).map(i -> sections.size()).sum();
    }

    private @NotNull List<EntityModel> getElements(@NotNull NetworkManager manager) {
        List<EntityModel> entities = new ArrayList<>();
        for (int i = 0; i < 95; i++) {
            EntityModel entity = EntityModel.newBuilder(String.valueOf(i), "entity-li").build();
            entities.add(entity);
        }
        return entities;
    }

    @Entity("entity")
    public interface SimpleEntity {

        @Property("property")
        @NotNull String getProperty();

    }

}
