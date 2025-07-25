package com.example.kiwoomapi.autotrader.service;

import java.io.IOException;

public interface KiwoomTokenService {
    String getAccessToken(String jsonData) throws IOException, InterruptedException;
    String getStoredAccessToken();
    boolean revokeAccessToken() throws IOException, InterruptedException;
    String getAppKey();
    String getAppSecret();
    String getAccount();
}
