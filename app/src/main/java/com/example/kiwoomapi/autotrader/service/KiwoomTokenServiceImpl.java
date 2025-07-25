package com.example.kiwoomapi.autotrader.service;

import com.example.kiwoomapi.autotrader.log.LogService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.example.kiwoomapi.autotrader.http.HttpClientService;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Service
public class KiwoomTokenServiceImpl implements KiwoomTokenService {

    private String accessToken;
    private long expiresIn; // Token expiration timestamp

    @Value("${kiwoom.api.appkey}")
    private String appKey;

    @Value("${kiwoom.api.secretkey}")
    private String appSecret;

    @Value("${kiwoom.account.cano}")
    private String account;

    private final LogService logService;
    private final HttpClientService httpClientService;

    public KiwoomTokenServiceImpl(LogService logService, HttpClientService httpClientService) {
        this.logService = logService;
        this.httpClientService = httpClientService;
    }

    @Override
    public String getAppKey() {
        return appKey;
    }

    @Override
    public String getAppSecret() {
        return appSecret;
    }

    @Override
    public String getAccount() {
        return account;
    }

    @Override
    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public String getAccessToken(String jsonData) throws IOException, InterruptedException {
        log.info("Attempting to get access token...");
        String responseBody = "";
        String status = "ERROR";
        String errorMessage = null;
        try {
            String host = "https://api.kiwoom.com"; // 실전투자
            String endpoint = "/oauth2/token";
            String urlString = host + endpoint;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(urlString))
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                    .build();

            HttpResponse<String> response = httpClientService.send(request);
            responseBody = response.body();
            log.info("Response Code: {}", response.statusCode());

            if (response.statusCode() == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                accessToken = rootNode.path("token").asText();
                long expiresInSeconds = rootNode.path("expires_in").asLong();
                expiresIn = System.currentTimeMillis() + (expiresInSeconds * 1000);
                log.info("Access token obtained successfully.");
                status = "SUCCESS";
                return accessToken;
            } else {
                errorMessage = "Failed to obtain token. Status code: " + response.statusCode() + ", Response: " + responseBody;
                log.error(errorMessage);
                status = "ERROR";
                return null;
            }

        } catch (IOException | InterruptedException e) {
            log.error("IOException during get access token: {}", e.getMessage());
            errorMessage = e.getMessage();
            throw e; // Re-throw to trigger retry
        } catch (Exception e) {
            log.error("Error during get access token: {}", e.getMessage());
            errorMessage = e.getMessage();
            return null;
        } finally {
            logService.saveLog("getAccessToken", jsonData, responseBody, status, errorMessage);
        }
    }

    @Override
    public String getStoredAccessToken() {
        if (accessToken == null || System.currentTimeMillis() >= expiresIn) {
            log.info("Access token expired or not available. Renewing...");
            try {
                String requestBody = String.format("{\"grant_type\":\"client_credentials\",\"appkey\":\"%s\",\"secretkey\":\"%s\"}", appKey, appSecret);
                String renewedToken = getAccessToken(requestBody);
                if (renewedToken != null) {
                    log.info("Access token renewed successfully.");
                    return renewedToken;
                } else {
                    log.error("Failed to renew access token.");
                    return null;
                }
            } catch (Exception e) {
                log.error("Exception during token renewal: {}", e.getMessage());
                return null;
            }
        }
        return accessToken;
    }

    @Override
    public boolean revokeAccessToken() throws IOException, InterruptedException {
        log.info("Attempting to revoke access token...");
        if (accessToken == null || accessToken.isEmpty()) {
            log.warn("No access token to revoke.");
            logService.saveLog("revokeAccessToken", "", "", "ERROR", "No token to revoke.");
            return false;
        }

        String requestBody = String.format("{\"token\":\"%s\"}", accessToken);
        String responseBody = "";
        String status = "ERROR";
        String errorMessage = null;

        try {
            String host = "https://api.kiwoom.com"; // 실전투자
            String endpoint = "/oauth2/revoke";
            String urlString = host + endpoint;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(urlString))
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClientService.send(request);
            int responseCode = response.statusCode();
            responseBody = response.body();
            log.info("Revoke Token Response Code: {}", responseCode);

            if (responseCode == 200) {
                log.info("Access token successfully revoked from Kiwoom API.");
                this.accessToken = null;
                status = "SUCCESS";
                return true;
            } else {
                errorMessage = "Failed to revoke token. Error: " + responseBody;
                log.error(errorMessage);
                return false;
            }

        } catch (IOException | InterruptedException e) {
            errorMessage = e.getMessage();
            log.error("Error during token revocation: {}", errorMessage, e);
            return false;
        } finally {
            logService.saveLog("revokeAccessToken", requestBody, responseBody, status, errorMessage);
        }
    }
}