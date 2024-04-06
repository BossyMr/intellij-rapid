package com.bossymr.network.client.parse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;

class ResponseModelTest {

    @Test
    void simpleEntity() {
        ResponseModel responseModel = ResponseModel.newBuilder(URI.create("http://localhost/"), "status")
                                                   .property("type", "property")
                                                   .link("type", URI.create("/"))
                                                   .entity("entity", "object")
                                                   .property("local", "value")
                                                   .link("local", URI.create("/local"))
                                                   .build()
                                                   .build();
        String text = responseModel.toXML();
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                          "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
                          "<head><title></title><base href=\"\"/></head>" +
                          "<body>" +
                          "<div class=\"status\">" +
                          "<span class=\"type\">property</span>" +
                          "<a href=\"http://localhost/\" rel=\"type\"></a>" +
                          "<ul>" +
                          "<li class=\"object\" title=\"entity\">" +
                          "<span class=\"local\">value</span>" +
                          "<a href=\"http://localhost/local\" rel=\"local\"></a>" +
                          "</li>" +
                          "</ul" +
                          "</div>" +
                          "</body>" +
                          "</html>";
        Assertions.assertEquals(expected, text);
    }

}