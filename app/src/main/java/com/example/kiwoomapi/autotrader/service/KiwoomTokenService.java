package com.example.kiwoomapi.autotrader.service;

import java.io.IOException;

public interface KiwoomTokenService {
    String getAccessToken(String jsonData) throws IOException;
    String getStoredAccessToken();
    boolean revokeAccessToken();
    String getAppKey();
    String getAppSecret();
    String getAccount();
}
