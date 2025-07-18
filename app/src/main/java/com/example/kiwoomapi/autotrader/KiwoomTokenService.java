package com.example.kiwoomapi.autotrader;

public interface KiwoomTokenService {
    String getAccessToken(String jsonData);
    String getStoredAccessToken();
    boolean revokeAccessToken();
}