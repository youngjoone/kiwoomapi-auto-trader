package com.example.kiwoomapi.autotrader.service;

import com.example.kiwoomapi.autotrader.log.LogService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.kiwoomapi.autotrader.http.HttpClientService;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.kiwoomapi.autotrader.controller.StockData;
import java.util.Collections;

@Slf4j
@Service
public class StockServiceImpl implements StockService {

    private final KiwoomTokenService kiwoomTokenService;
    private final ObjectMapper objectMapper = new ObjectMapper().configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final List<StockData> upperLimitStocks = Collections.synchronizedList(new ArrayList<>());
    private final LogService logService;
    private final HttpClientService httpClientService;

    @Value("${kiwoom.api.host}")
    private String apiHost;

    public StockServiceImpl(KiwoomTokenService kiwoomTokenService, LogService logService, HttpClientService httpClientService) {
        this.kiwoomTokenService = kiwoomTokenService;
        this.logService = logService;
        this.httpClientService = httpClientService;
    }

    @Override
    @Scheduled(cron = "0 50 8 * * MON-FRI", zone = "Asia/Seoul")
    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void fetchAndStorePreviousDayUpperLimitStocks() throws IOException, InterruptedException {
        log.info("Fetching previous day's upper limit stocks...");
        String accessToken = kiwoomTokenService.getStoredAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            log.error("Access token is not available. Cannot fetch upper limit stocks.");
            logService.saveLog("fetchUpperLimitStocks", "", "", "ERROR", "Access token not available.");
            throw new IOException("Access token not available.");
        }

        String requestBody = "{\"mrkt_tp\":\"000\",\"updown_tp\":\"6\",\"sort_tp\":\"1\",\"stk_cnd\":\"0\",\"trde_qty_tp\":\"0000\",\"crd_cnd\":\"0\",\"trde_gold_tp\":\"0\",\"stex_tp\":\"0\"}";
        String responseBodyStr = "";
        String status = "ERROR";
        String errorMessage = null;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiHost + "/api/dostk/stkinfo"))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("authorization", "Bearer " + accessToken)
                    .header("appkey", kiwoomTokenService.getAppKey())
                    .header("appsecret", kiwoomTokenService.getAppSecret())
                    .header("api-id", "ka10017")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClientService.send(request);
            responseBodyStr = response.body();
            log.info("Kiwoom API Response Status Code: {}", response.statusCode());
            log.info("Kiwoom API Response Body: {}", responseBodyStr);

            if (response.statusCode() == 200) {
                JsonNode responseBodyJson = objectMapper.readTree(responseBodyStr);
                JsonNode stockList = responseBodyJson.path("updown_pric");

                upperLimitStocks.clear();
                if (stockList.isArray()) {
                    for (JsonNode stock : stockList) {
                        StockData stockData = objectMapper.treeToValue(stock, StockData.class);
                        upperLimitStocks.add(stockData);
                    }
                }
                log.info("Successfully fetched and stored {} upper limit stocks.", upperLimitStocks.size());
                status = "SUCCESS";
            } else {
                errorMessage = "Failed to fetch upper limit stocks. Status code: " + response.statusCode() + ", Response: " + responseBodyStr;
                log.error("{}, Response: {}", errorMessage, responseBodyStr);
                throw new IOException(errorMessage);
            }
        } catch (IOException | InterruptedException e) {
            errorMessage = e.getMessage();
            log.error("Error while fetching upper limit stocks", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw e; // Re-throw the original exception
        } finally {
            logService.saveLog("fetchUpperLimitStocks", requestBody, responseBodyStr, status, errorMessage);
        }
    }

    public List<StockData> getUpperLimitStocks() {
        return upperLimitStocks;
    }
}
