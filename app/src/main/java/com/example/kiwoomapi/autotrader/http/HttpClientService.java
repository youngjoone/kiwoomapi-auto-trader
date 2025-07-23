package com.example.kiwoomapi.autotrader.http;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public interface HttpClientService {
    HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException;
}