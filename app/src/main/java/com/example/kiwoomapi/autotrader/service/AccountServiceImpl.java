package com.example.kiwoomapi.autotrader.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private KiwoomTokenService kiwoomTokenService;

    private static final String KIWOOM_API_HOST = "https://api.kiwoom.com";

    @Override
    public String getAccountInfo() {
        // TODO: Implement actual Kiwoom API call to get account info
        return "Account information will be implemented here.";
    }

    private String callKiwoomApi(String endpoint, String method, String apiId, String requestBody) {
        String accessToken = kiwoomTokenService.getStoredAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            return "Error: Access token not available.";
        }

        try {
            URL url = new URL(KIWOOM_API_HOST + endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod(method);
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("api-id", apiId); // api-id 헤더 추가

            if (requestBody != null && !requestBody.isEmpty()) {
                connection.setDoOutput(true);
                try (java.io.OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
            }

            int responseCode = connection.getResponseCode();
            System.out.println("API Call Response Code for " + endpoint + " (" + apiId + "): " + responseCode);

            BufferedReader in;
            if (responseCode >= 200 && responseCode < 300) {
                in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            } else {
                in = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
            }

            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling Kiwoom API: " + e.getMessage();
        }
    }

    @Override
    public String getAccountBalance(String accountNumber) {
        // kt00018 - 계좌평가잔고내역요청 (Page 396)
        String endpoint = "/api/dostk/acnt";
        String apiId = "kt00018";
        String requestBody = String.format("{\"qry_tp\":\"1\", \"dmst_stex_tp\":\"KRX\"}");
        return callKiwoomApi(endpoint, "POST", apiId, requestBody);
    }

    @Override
    public String getAccountTransactions(String accountNumber, String startDate, String endDate) {
        // kt00004 - 계좌평가현황요청 (Page 361) - Note: This API is for account evaluation status, not transactions.
        // For transactions, a different API (e.g., kt00015 위탁종합거래내역요청) might be more appropriate.
        // However, based on the user's request for fn_kt00004, I will use it as is, assuming it provides some transaction-like data.
        String endpoint = "/api/dostk/acnt";
        String apiId = "kt00004";
        String requestBody = String.format("{\"qry_tp\":\"0\", \"dmst_stex_tp\":\"KRX\"}");
        return callKiwoomApi(endpoint, "POST", apiId, requestBody);
    }

    @Override
    public String getAccountDetails(String accountNumber) {
        // ka10085 - 계좌수익률요청 (Page 207)
        String endpoint = "/api/dostk/acnt";
        String apiId = "ka10085";
        String requestBody = String.format("{\"stex_tp\":\"0\"}");
        return callKiwoomApi(endpoint, "POST", apiId, requestBody);
    }
}