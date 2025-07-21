package com.example.kiwoomapi.autotrader.service;

import com.example.kiwoomapi.autotrader.service.KiwoomTokenService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;

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
    public String getAccessToken(String jsonData) {
        try {
            // 1. 요청할 API URL
            // String host = "https://mockapi.kiwoom.com"; // 모의투자
            String host = "https://api.kiwoom.com"; // 실전투자
            String endpoint = "/oauth2/token";
            String urlString = host + endpoint;

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 2. Header 데이터 설정
            connection.setRequestMethod("POST"); // 메서드 타입
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8"); // 컨텐츠타입
            connection.setDoOutput(true);

            // 3. JSON 데이터 전송
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 4. 응답 헤더 출력
            System.out.println("Code: "+ connection.getResponseCode());
            System.out.println("Header:");
            String[] headerKeys = {"cont-yn","next-key","api-id"};
            connection.getHeaderFields().forEach((key, value) -> {
                if(Arrays.asList(headerKeys).contains(key)){
                    System.out.println("    " + key + ": " + value.get(0));
                }
            });

            // 5. 응답 본문 출력 및 토큰 파싱
            System.out.println("Body:");
            try (Scanner scanner = new Scanner(connection.getInputStream(), "utf-8")) {
                String responseBody = scanner.useDelimiter("\\A").next();
                System.out.println("    " + responseBody);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                accessToken = rootNode.path("token").asText();
                long expiresInSeconds = rootNode.path("expires_in").asLong(); // Assuming 'expires_in' is in seconds
                expiresIn = System.currentTimeMillis() + (expiresInSeconds * 1000); // Convert to milliseconds
                System.out.println("DEBUG: Access token set in service (full): [" + accessToken + "]");
                return accessToken;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getStoredAccessToken() {
        if (accessToken == null || System.currentTimeMillis() >= expiresIn) {
            System.out.println("DEBUG: Access token expired or not available. Renewing...");
            // In a real scenario, you would call getAccessToken with appropriate jsonData
            // For now, let's simulate a renewal or throw an exception if jsonData is needed
            // For simplicity, let's assume jsonData is not needed for renewal or is handled internally
            try {
                // Simulate token renewal. In a real app, this would be a proper API call.
                String renewedToken = getAccessToken("{\"grant_type\":\"client_credentials\",\"appkey\":\"" + appKey + "\",\"secretkey\":\"" + appSecret + "\"}");
                if (renewedToken != null) {
                    System.out.println("DEBUG: Access token renewed successfully.");
                    return renewedToken;
                } else {
                    System.err.println("DEBUG: Failed to renew access token.");
                    return null;
                }
            } catch (Exception e) {
                System.err.println("DEBUG: Exception during token renewal: " + e.getMessage());
                return null;
            }
        }
        System.out.println("DEBUG: getStoredAccessToken called. Current token (full): [" + accessToken + "]");
        return accessToken;
    }

    @Override
    public boolean revokeAccessToken() {
        System.out.println("DEBUG: revokeAccessToken called. Token before check (full): [" + accessToken + "]");
        if (accessToken == null || accessToken.isEmpty()) {
            System.out.println("No access token to revoke.");
            return false;
        }

        try {
            String host = "https://api.kiwoom.com"; // 실전투자
            String endpoint = "/oauth2/revoke"; // 키움 API 토큰 폐기 엔드포인트 (가정)
            String urlString = host + endpoint;

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8"); // 컨텐츠타입
            connection.setDoOutput(true);

            String requestBody = "{\"token\":\"" + accessToken + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Revoke Token Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Access token successfully revoked from Kiwoom API.");
                this.accessToken = null; // 로컬 토큰 무효화
                return true;
            } else {
                try (Scanner scanner = new Scanner(connection.getErrorStream(), "utf-8")) {
                    String errorResponse = scanner.useDelimiter("\\A").next();
                    System.err.println("Failed to revoke token. Error: " + errorResponse);
                }
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}