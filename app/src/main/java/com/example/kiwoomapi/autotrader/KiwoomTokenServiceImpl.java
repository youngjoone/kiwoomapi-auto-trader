package com.example.kiwoomapi.autotrader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;

@Service
public class KiwoomTokenServiceImpl implements KiwoomTokenService {

    private String accessToken;

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
                accessToken = rootNode.path("access_token").asText();
                return accessToken;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getStoredAccessToken() {
        return accessToken;
    }
}