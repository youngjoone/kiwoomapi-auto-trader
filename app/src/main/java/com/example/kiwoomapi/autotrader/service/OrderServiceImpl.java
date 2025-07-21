package com.example.kiwoomapi.autotrader.service;

import com.example.kiwoomapi.autotrader.model.TradeInfo;
import com.example.kiwoomapi.autotrader.strategy.UpperLimitBuyStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final KiwoomTokenService kiwoomTokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UpperLimitBuyStrategy upperLimitBuyStrategy;
    private final Map<String, TradeInfo> ownedStocks = new ConcurrentHashMap<>(); // 보유 주식 정보

    @Value("${kiwoom.api.host}")
    private String apiHost;

    @Value("${kiwoom.order.profit-margin}")
    private double profitMargin;

    @Value("${kiwoom.order.loss-margin}")
    private double lossMargin;

    public OrderServiceImpl(KiwoomTokenService kiwoomTokenService, UpperLimitBuyStrategy upperLimitBuyStrategy) {
        this.kiwoomTokenService = kiwoomTokenService;
        this.upperLimitBuyStrategy = upperLimitBuyStrategy;
    }

    @Scheduled(cron = "0 0 9 * * MON-FRI", zone = "Asia/Seoul")
    public void runUpperLimitBuyStrategy() throws IOException {
        upperLimitBuyStrategy.execute();
    }

    @Override
    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void placeBuyOrder(String stockCode, long quantity) throws IOException {
        String accessToken = kiwoomTokenService.getStoredAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            log.error("Access token is not available. Cannot place buy order for stock: {}.", stockCode);
            throw new IOException("Access token not available for buy order.");
        }

        // Assume actual buy price is current market price for simplicity in this example
        long buyPrice = getCurrentPrice(stockCode);
        if (buyPrice <= 0) {
            log.error("Could not get current price for stock: {}. Cannot place buy order.", stockCode);
            throw new IOException("Could not get current price for stock: " + stockCode);
        }

        String orderRequestBody = String.format("{\"CANO\":\"%s\",\"ACNT_PRDT_CD\":\"01\",\"PDNO\":\"%s\",\"ORD_DVSN\":\"01\",\"ORD_QTY\":\"%d\",\"ORD_UNPR\":\"0\"}", 
            kiwoomTokenService.getAccount(), stockCode, quantity);

        HttpRequest orderRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiHost + "/uapi/domestic-stock/v1/trading/order-cash"))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", kiwoomTokenService.getAppKey())
                .header("appsecret", kiwoomTokenService.getAppSecret())
                .header("api-id", "kt10000") // Corrected API ID for cash order
                .POST(HttpRequest.BodyPublishers.ofString(orderRequestBody))
                .build();

        try {
            HttpResponse<String> orderResponse = HttpClient.newHttpClient().send(orderRequest, HttpResponse.BodyHandlers.ofString());
            log.info("Buy order for {} placed. Response: {}", stockCode, orderResponse.body());
            // Assuming order is successful, add to owned stocks
            addTradeInfo(new TradeInfo(stockCode, buyPrice, quantity, System.currentTimeMillis()));
            startRealtimeMonitoring(); // Start monitoring after a successful buy
        } catch (InterruptedException e) {
            log.error("Error placing buy order for stock: {}. Interrupted.", stockCode, e);
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted during buy order for stock: " + stockCode, e);
        }
    }

    @Override
    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void placeSellOrder(String stockCode, long quantity) throws IOException {
        String accessToken = kiwoomTokenService.getStoredAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            log.error("Access token is not available. Cannot place sell order for stock: {}.", stockCode);
            throw new IOException("Access token not available for sell order.");
        }

        String orderRequestBody = String.format("{\"CANO\":\"%s\",\"ACNT_PRDT_CD\":\"01\",\"PDNO\":\"%s\",\"ORD_DVSN\":\"01\",\"ORD_QTY\":\"%d\",\"ORD_UNPR\":\"0\"}", 
            kiwoomTokenService.getAccount(), stockCode, quantity);

        HttpRequest orderRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiHost + "/uapi/domestic-stock/v1/trading/order-cash")) // Assuming same endpoint for sell
                .header("Content-Type", "application/json; charset=utf-8")
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", kiwoomTokenService.getAppKey())
                .header("appsecret", kiwoomTokenService.getAppSecret())
                .header("api-id", "kt10001") // Corrected API ID for cash sell order
                .POST(HttpRequest.BodyPublishers.ofString(orderRequestBody))
                .build();

        try {
            HttpResponse<String> orderResponse = HttpClient.newHttpClient().send(orderRequest, HttpResponse.BodyHandlers.ofString());
            log.info("Sell order for {} placed. Response: {}", stockCode, orderResponse.body());
            removeTradeInfo(stockCode); // Remove from owned stocks after successful sell
        } catch (InterruptedException e) {
            log.error("Error placing sell order for stock: {}. Interrupted.", stockCode, e);
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted during sell order for stock: " + stockCode, e);
        }
    }

    @Override
    public void addTradeInfo(TradeInfo tradeInfo) {
        ownedStocks.put(tradeInfo.getStockCode(), tradeInfo);
        log.info("Added stock {} to owned stocks. Current owned stocks: {}", tradeInfo.getStockCode(), ownedStocks.size());
    }

    @Override
    public void removeTradeInfo(String stockCode) {
        ownedStocks.remove(stockCode);
        log.info("Removed stock {} from owned stocks. Current owned stocks: {}", stockCode, ownedStocks.size());
    }

    @Override
    public void startRealtimeMonitoring() {
        // This is a placeholder for actual WebSocket implementation.
        // In a real application, you would establish WebSocket connection here
        // and subscribe to real-time price updates for stocks in 'ownedStocks'.
        log.info("Starting real-time monitoring for owned stocks...");
        // Example: Iterate through ownedStocks and subscribe to real-time data
        for (Map.Entry<String, TradeInfo> entry : ownedStocks.entrySet()) {
            String stockCode = entry.getKey();
            TradeInfo tradeInfo = entry.getValue();
            log.info("Subscribing to real-time data for stock: {}", stockCode);
            // Simulate real-time price update and check for sell conditions
            // In a real scenario, this would be triggered by WebSocket messages
            try {
                long currentPrice = getCurrentPrice(stockCode); // Simulate real-time price
                checkAndPlaceSellOrder(tradeInfo, currentPrice);
            } catch (IOException e) {
                log.error("Error getting current price for real-time monitoring: {}", stockCode, e);
            }
        }
    }

    @Override
    public void processRealtimeStockPrice(String stockCode, long currentPrice) throws IOException {
        TradeInfo tradeInfo = ownedStocks.get(stockCode);
        if (tradeInfo != null) {
            checkAndPlaceSellOrder(tradeInfo, currentPrice);
        }
    }

    private long getCurrentPrice(String stockCode) throws IOException {
        String accessToken = kiwoomTokenService.getStoredAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            log.error("Access token is not available. Cannot get current price for stock: {}.", stockCode);
            throw new IOException("Access token not available for current price inquiry.");
        }
        String url = apiHost + "/uapi/domestic-stock/v1/quotations/inquire-price?fid_cond_mrkt_div_code=J&fid_input_iscd=" + stockCode;
        HttpRequest priceRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", kiwoomTokenService.getAppKey())
                .header("appsecret", kiwoomTokenService.getAppSecret())
                .header("api-id", "ka10001") // Corrected API ID for stock price inquiry
                .GET()
                .build();

        try {
            HttpResponse<String> priceResponse = HttpClient.newHttpClient().send(priceRequest, HttpResponse.BodyHandlers.ofString());
            JsonNode priceResponseBody = objectMapper.readTree(priceResponse.body());
            return priceResponseBody.path("output").path("stck_prpr").asLong();
        } catch (InterruptedException e) {
            log.error("Error getting current price for stock: {}. Interrupted.", stockCode, e);
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted during get current price for stock: " + stockCode, e);
        }
    }

    private void checkAndPlaceSellOrder(TradeInfo tradeInfo, long currentPrice) throws IOException {
        double profitLossPercentage = ((double) (currentPrice - tradeInfo.getBuyPrice()) / tradeInfo.getBuyPrice()) * 100;

        if (profitLossPercentage >= profitMargin) {
            log.info("Profit target reached for {}. Placing sell order.", tradeInfo.getStockCode());
            placeSellOrder(tradeInfo.getStockCode(), tradeInfo.getQuantity());
        } else if (profitLossPercentage <= lossMargin) {
            log.info("Loss limit reached for {}. Placing sell order.", tradeInfo.getStockCode());
            placeSellOrder(tradeInfo.getStockCode(), tradeInfo.getQuantity());
        }
    }
}