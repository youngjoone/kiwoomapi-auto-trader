package com.example.kiwoomapi.autotrader.service;

import com.example.kiwoomapi.autotrader.log.LogService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

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

    public KiwoomTokenServiceImpl(LogService logService) {
        this.logService = logService;
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
    public String getAccessToken(String jsonData) throws IOException {
        log.info("Attempting to get access token...");
        String responseBody = "";
        String status = "ERROR";
        String errorMessage = null;
        try {
            String host = "https://api.kiwoom.com"; // 실전투자
            String endpoint = "/oauth2/token";
            String urlString = host + endpoint;

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            log.info("Response Code: {}", connection.getResponseCode());

            try (Scanner scanner = new Scanner(connection.getInputStream(), "utf-8")) {
                responseBody = scanner.useDelimiter("\\A").next();
                log.info("Response Body: {}", responseBody);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                accessToken = rootNode.path("token").asText();
                long expiresInSeconds = rootNode.path("expires_in").asLong();
                expiresIn = System.currentTimeMillis() + (expiresInSeconds * 1000);
                log.info("Access token obtained successfully.");
                status = "SUCCESS";
                return accessToken;
            }

        } catch (IOException e) {
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
    public boolean revokeAccessToken() {
        log.info("Attempting to revoke access token...");
        if (accessToken == null || accessToken.isEmpty()) {
            log.warn("No access token to revoke.");
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

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            log.info("Revoke Token Response Code: {}", responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                log.info("Access token successfully revoked from Kiwoom API.");
                this.accessToken = null;
                status = "SUCCESS";
                return true;
            } else {
                try (Scanner scanner = new Scanner(connection.getErrorStream(), "utf-8")) {
                    responseBody = scanner.useDelimiter("\\A").next();
                    errorMessage = "Failed to revoke token. Error: " + responseBody;
                    log.error(errorMessage);
                }
                return false;
            }

        } catch (Exception e) {
            errorMessage = e.getMessage();
            log.error("Error during token revocation: {}", errorMessage, e);
            return false;
        } finally {
            logService.saveLog("revokeAccessToken", requestBody, responseBody, status, errorMessage);
        }
    }
}