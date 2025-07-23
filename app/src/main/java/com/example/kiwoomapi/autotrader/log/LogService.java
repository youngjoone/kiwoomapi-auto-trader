package com.example.kiwoomapi.autotrader.log;

public interface LogService {
    void saveLog(String apiId, String request, String response, String status, String errorMessage);
}
