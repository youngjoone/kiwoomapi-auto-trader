package com.example.kiwoomapi.autotrader.service;

public interface KiwoomTokenService {
    String getAccessToken(String jsonData);
    String getStoredAccessToken();
    boolean revokeAccessToken();
    String getAppKey();
    String getAppSecret();
    String getAccount();
}
