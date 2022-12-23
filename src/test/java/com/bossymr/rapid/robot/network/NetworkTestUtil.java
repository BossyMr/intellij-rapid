package com.bossymr.rapid.robot.network;

import com.intellij.credentialStore.Credentials;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class NetworkTestUtil {

    public static URI DEFAULT_PATH = URI.create("http://localhost:80");
    public static Credentials DEFAULT_CREDENTIALS = new Credentials("Default User", "robotics");

    private NetworkTestUtil() {
        throw new AssertionError();
    }

    public static boolean doNetworkTest() {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder(NetworkTestUtil.DEFAULT_PATH).build();
        try {
            httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

}
