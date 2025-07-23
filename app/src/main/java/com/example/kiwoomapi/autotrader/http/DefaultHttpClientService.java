package com.example.kiwoomapi.autotrader.http;

import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class DefaultHttpClientService implements HttpClientService {

    private final HttpClient httpClient;

    public DefaultHttpClientService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}