package com.bossymr.rapid.robot.api.client.proxy;

import com.bossymr.rapid.robot.api.GenericType;
import com.bossymr.rapid.robot.api.NetworkManager;
import com.bossymr.rapid.robot.api.NetworkTarget;
import com.bossymr.rapid.robot.api.RequestMethod;
import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Property;
import com.bossymr.rapid.robot.api.client.HeavyNetworkManager;
import com.bossymr.rapid.robot.api.client.entity.EntityModel;
import com.bossymr.rapid.robot.api.client.entity.ResponseModel;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@WireMockTest
class ListProxyTest {

    @Test
    void multiplePages(WireMockRuntimeInfo runtimeInfo) throws IOException, InterruptedException {
        WireMock wireMock = runtimeInfo.getWireMock();
        try (NetworkManager manager = new HeavyNetworkManager(URI.create(runtimeInfo.getHttpBaseUrl()), null)) {
            List<EntityObject> models = setupPaging(wireMock, manager, 10, 5);
            NetworkTarget<List<EntityObject>> target = new NetworkTarget<>(RequestMethod.GET, URI.create("/"), new GenericType<>() {});
            ListProxy<EntityObject> proxy = new ListProxy<>(manager, EntityObject.class, target);
            Assertions.assertEquals(models, proxy);
        }
    }

    @Test
    void singlePage(WireMockRuntimeInfo runtimeInfo) throws IOException, InterruptedException {
        WireMock wireMock = runtimeInfo.getWireMock();
        try (NetworkManager manager = new HeavyNetworkManager(URI.create(runtimeInfo.getHttpBaseUrl()), null)) {
            List<EntityObject> models = setupPaging(wireMock, manager, 10, 1);
            NetworkTarget<List<EntityObject>> target = new NetworkTarget<>(RequestMethod.GET, URI.create("/"), new GenericType<>() {});
            ListProxy<EntityObject> proxy = new ListProxy<>(manager, EntityObject.class, target);
            Assertions.assertEquals(models, proxy);
        }
    }

    @Test
    void lazyLoadFirstPage(WireMockRuntimeInfo runtimeInfo) throws IOException, InterruptedException {
        WireMock wireMock = runtimeInfo.getWireMock();
        try (NetworkManager manager = new HeavyNetworkManager(URI.create(runtimeInfo.getHttpBaseUrl()), null)) {
            List<EntityObject> models = setupPaging(wireMock, manager, 10, 5);
            NetworkTarget<List<EntityObject>> target = new NetworkTarget<>(RequestMethod.GET, URI.create("/"), new GenericType<>() {});
            ListProxy<EntityObject> proxy = new ListProxy<>(manager, EntityObject.class, target);
            // The ListProxy should automatically retrieve the first page.
            wireMock.verifyThat(exactly(1), getRequestedFor(urlPathEqualTo("/")));
            Assertions.assertEquals(models.getFirst(), proxy.getFirst());
            // The first page should already be retrieved.
            wireMock.verifyThat(exactly(1), getRequestedFor(urlPathEqualTo("/")));
            Assertions.assertEquals(models.get(9), proxy.get(9));
            // The first page should already be retrieved.
            wireMock.verifyThat(exactly(1), getRequestedFor(urlPathEqualTo("/")));
            Assertions.assertEquals(models, proxy);
        }
    }

    @Test
    void lazyLoadMiddlePage(WireMockRuntimeInfo runtimeInfo) throws IOException, InterruptedException {
        WireMock wireMock = runtimeInfo.getWireMock();
        try (NetworkManager manager = new HeavyNetworkManager(URI.create(runtimeInfo.getHttpBaseUrl()), null)) {
            List<EntityObject> models = setupPaging(wireMock, manager, 10, 5);
            NetworkTarget<List<EntityObject>> target = new NetworkTarget<>(RequestMethod.GET, URI.create("/"), new GenericType<>() {});
            ListProxy<EntityObject> proxy = new ListProxy<>(manager, EntityObject.class, target);
            // The ListProxy should automatically retrieve the first page.
            wireMock.verifyThat(exactly(1), getRequestedFor(urlPathEqualTo("/")));
            Assertions.assertEquals(models.get(30), proxy.get(30));
            // The ListProxy should have retrieved the fourth page.
            wireMock.verifyThat(exactly(2), getRequestedFor(urlPathEqualTo("/")));
            Assertions.assertEquals(models.get(39), proxy.get(39));
            // The ListProxy should have already retrieved the fourth page.
            wireMock.verifyThat(exactly(2), getRequestedFor(urlPathEqualTo("/")));
            Assertions.assertEquals(models, proxy);
        }
    }

    private List<EntityObject> setupPaging(WireMock wireMock, NetworkManager manager, int pageSize, int pageCount) {
        List<EntityModel> models = new ArrayList<>();
        List<EntityObject> entities = new ArrayList<>();
        for (int i = 0; i < pageCount * pageSize; i++) {
            EntityModel entity = EntityModel.newBuilder("entity", "")
                    .property("id", String.valueOf(i))
                    .build();
            entities.add(manager.createEntity(EntityObject.class, entity));
            models.add(entity);
        }
        for (int i = 0; i < pageCount; i++) {
            ResponseModel model = getPage(models, i * pageSize, pageSize, pageCount * pageSize);
            wireMock.register(get(urlPathEqualTo("/"))
                    .withQueryParam("start", equalTo(String.valueOf(i * pageSize)))
                    .withQueryParam("size", equalTo(String.valueOf(pageSize)))
                    .willReturn(okForContentType("application/xhtml+xml", model.toXML())));
            if (i == 0) {
                wireMock.register(get(urlPathEqualTo("/"))
                        .withQueryParam("start", equalTo(String.valueOf(0)))
                        .withQueryParam("size", absent())
                        .willReturn(okForContentType("application/xhtml+xml", model.toXML())));
            }
        }
        return entities;
    }

    private ResponseModel getPage(List<EntityModel> entities, int pageStart, int pageSize, int pageEnd) {
        ResponseModel.Builder builder = ResponseModel.newBuilder("", "")
                .link("self", URI.create("/?start=" + pageStart + "&size=" + pageSize));
        if (pageStart > 0) {
            builder.link("prev", URI.create("/?start=" + Math.max(0, pageStart - pageSize) + "&size=" + Math.min(pageSize, pageStart)));
        }
        if (pageStart + pageSize < pageEnd) {
            builder.link("next", URI.create("/?start=" + (pageStart + pageSize) + "&size=" + Math.min(pageSize, pageEnd - (pageStart + pageSize))));
        }
        for (int i = pageStart; i < pageStart + pageSize; i++) {
            builder.entity(entities.get(i));
        }
        return builder.build();
    }

    @Entity("entity")
    public interface EntityObject {
        @Property("id")
        int getId();
    }
}