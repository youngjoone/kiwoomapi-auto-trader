package com.example.kiwoomapi.autotrader.service;

import com.example.kiwoomapi.autotrader.http.HttpClientService;
import com.example.kiwoomapi.autotrader.log.LogService;
import com.example.kiwoomapi.autotrader.model.TradeInfo;
import com.example.kiwoomapi.autotrader.model.TradeInfoRepository;
import com.example.kiwoomapi.autotrader.model.TradeHistory;
import com.example.kiwoomapi.autotrader.model.TradeHistoryRepository;
import com.example.kiwoomapi.autotrader.websocket.KiwoomWebSocketClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final KiwoomTokenService kiwoomTokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TradeInfoRepository tradeInfoRepository;
    private final TradeHistoryRepository tradeHistoryRepository;
    private final LogService logService;
    private final HttpClientService httpClientService;
    private final KiwoomWebSocketClient kiwoomWebSocketClient;

    @Value("${kiwoom.api.host}")
    private String apiHost;

    @Value("${kiwoom.order.profit-margin}")
    private double profitMargin;

    @Value("${kiwoom.order.loss-margin}")
    private double lossMargin;

    public OrderServiceImpl(KiwoomTokenService kiwoomTokenService, TradeInfoRepository tradeInfoRepository, TradeHistoryRepository tradeHistoryRepository, LogService logService, HttpClientService httpClientService, KiwoomWebSocketClient kiwoomWebSocketClient) {
        this.kiwoomTokenService = kiwoomTokenService;
        this.tradeInfoRepository = tradeInfoRepository;
        this.tradeHistoryRepository = tradeHistoryRepository;
        this.logService = logService;
        this.httpClientService = httpClientService;
        this.kiwoomWebSocketClient = kiwoomWebSocketClient;
    }

    @Override
    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void placeBuyOrder(String stockCode, long quantity) throws IOException {
        String accessToken = kiwoomTokenService.getStoredAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            log.error("Access token is not available. Cannot place buy order for stock: {}.", stockCode);
            throw new IOException("Access token not available for buy order.");
        }

        long buyPrice = getCurrentPrice(stockCode);
        if (buyPrice <= 0) {
            log.error("Could not get current price for stock: {}. Cannot place buy order.", stockCode);
            throw new IOException("Could not get current price for stock: " + stockCode);
        }

        String stockName = getStockName(stockCode); // 종목명 가져오기

        String orderRequestBody = String.format("{\"CANO\":\"%s\",\"ACNT_PRDT_CD\":\"01\",\"PDNO\":\"%s\",\"ORD_DVSN\":\"01\",\"ORD_QTY\":\"%d\",\"ORD_UNPR\":\"0\"}",
            kiwoomTokenService.getAccount(), stockCode, quantity);

        HttpRequest orderRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiHost + "/uapi/domestic-stock/v1/trading/order-cash"))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", kiwoomTokenService.getAppKey())
                .header("appsecret", kiwoomTokenService.getAppSecret())
                .header("api-id", "kt10000")
                .POST(HttpRequest.BodyPublishers.ofString(orderRequestBody))
                .build();

        String responseBody = "";
        String status = "ERROR";
        String errorMessage = null;

        try {
            HttpResponse<String> orderResponse = httpClientService.send(orderRequest);
            responseBody = orderResponse.body();
            log.info("Buy order for {} placed. Response: {}", stockCode, responseBody);
            addTradeInfo(new TradeInfo(stockCode, stockName, buyPrice, quantity, System.currentTimeMillis())); // 종목명 추가
            kiwoomWebSocketClient.subscribeToRealtimeStockPrice(stockCode); // 매수 후 실시간 구독 시작
            status = "SUCCESS";
        } catch (InterruptedException e) {
            errorMessage = e.getMessage();
            log.error("Error placing buy order for stock: {}. Interrupted.", stockCode, e);
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted during buy order for stock: " + stockCode, e);
        } finally {
            logService.saveLog("placeBuyOrder", orderRequestBody, responseBody, status, errorMessage);
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
                .uri(URI.create(apiHost + "/uapi/domestic-stock/v1/trading/order-cash"))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", kiwoomTokenService.getAppKey())
                .header("appsecret", kiwoomTokenService.getAppSecret())
                .header("api-id", "kt10001")
                .POST(HttpRequest.BodyPublishers.ofString(orderRequestBody))
                .build();

        String responseBody = "";
        String status = "ERROR";
        String errorMessage = null;

        try {
            HttpResponse<String> orderResponse = httpClientService.send(orderRequest);
            responseBody = orderResponse.body();
            log.info("Sell order for {} placed. Response: {}", stockCode, responseBody);

            // TradeInfo에서 해당 종목을 찾아 TradeHistory에 저장 후 삭제
            tradeInfoRepository.findById(stockCode).ifPresent(tradeInfo -> {
                try {
                    long currentPrice = getCurrentPrice(stockCode); // 매도 시점의 현재가 다시 조회
                    double profitLoss = (currentPrice - tradeInfo.getBuyPrice()) * tradeInfo.getQuantity();
                    double profitLossPercentage = ((double) (currentPrice - tradeInfo.getBuyPrice()) / tradeInfo.getBuyPrice()) * 100;

                    TradeHistory tradeHistory = new TradeHistory(
                        null, // ID는 자동 생성
                        tradeInfo.getStockCode(),
                        tradeInfo.getStockName(), // 종목명 추가
                        tradeInfo.getBuyPrice(),
                        tradeInfo.getQuantity(),
                        tradeInfo.getBuyTimestamp(),
                        currentPrice,
                        tradeInfo.getQuantity(), // 매도 수량은 매수 수량과 동일하다고 가정
                        System.currentTimeMillis(),
                        profitLoss,
                        profitLossPercentage
                    );
                    tradeHistoryRepository.save(tradeHistory);
                    log.info("Trade history saved for {}. Profit/Loss: {} ({:.2f}%)", stockCode, profitLoss, profitLossPercentage);

                    removeTradeInfo(stockCode);
                } catch (IOException e) {
                    log.error("Error getting current price for stock {} during sell order processing.", stockCode, e);
                }
            });

            kiwoomWebSocketClient.unsubscribeFromRealtimeStockPrice(stockCode, "0B"); // 매도 후 실시간 구독 해지
            status = "SUCCESS";
        } catch (InterruptedException e) {
            errorMessage = e.getMessage();
            log.error("Error placing sell order for stock: {}. Interrupted.", stockCode, e);
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted during sell order for stock: " + stockCode, e);
        } finally {
            logService.saveLog("placeSellOrder", orderRequestBody, responseBody, status, errorMessage);
        }
    }

    @Override
    public void addTradeInfo(TradeInfo tradeInfo) {
        tradeInfoRepository.save(tradeInfo);
        log.info("Added stock {} to owned stocks. Current owned stocks: {}", tradeInfo.getStockCode(), tradeInfoRepository.count());
    }

    @Override
    public void removeTradeInfo(String stockCode) {
        tradeInfoRepository.deleteById(stockCode);
        log.info("Removed stock {} from owned stocks. Current owned stocks: {}", stockCode, tradeInfoRepository.count());
        kiwoomWebSocketClient.unsubscribeFromRealtimeStockPrice(stockCode, "0B"); // TradeInfo 삭제 시 실시간 구독 해지
    }

    @Override
    public void startRealtimeMonitoring() {
        log.info("Starting real-time monitoring for owned stocks...");
        List<TradeInfo> ownedStocks = tradeInfoRepository.findAll();
        for (TradeInfo tradeInfo : ownedStocks) {
            String stockCode = tradeInfo.getStockCode();
            log.info("Subscribing to real-time data for stock: {}", stockCode);
            kiwoomWebSocketClient.subscribeToRealtimeStockPrice(stockCode);
        }
    }

    @Override
    public void processRealtimeStockPrice(com.example.kiwoomapi.autotrader.controller.StockData stockData) throws IOException {
        tradeInfoRepository.findById(stockData.getStk_cd()).ifPresent(tradeInfo -> {
            try {
                checkAndPlaceSellOrder(tradeInfo, Long.parseLong(stockData.getCur_prc()));
            } catch (IOException e) {
                log.error("Error processing real-time stock price for {}", stockData.getStk_cd(), e);
            }
        });
    }

    @Override
    public void unsubscribeRealtimeStockPrice(String stockCode, String type) {
        kiwoomWebSocketClient.unsubscribeFromRealtimeStockPrice(stockCode, type);
    }

    public long getCurrentPrice(String stockCode) throws IOException {
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
                .header("api-id", "ka10001")
                .GET()
                .build();

        String responseBody = "";
        String status = "ERROR";
        String errorMessage = null;

        try {
            HttpResponse<String> priceResponse = httpClientService.send(priceRequest);
            responseBody = priceResponse.body();
            JsonNode priceResponseBody = objectMapper.readTree(responseBody);
            status = "SUCCESS";
            return priceResponseBody.path("output").path("stck_prpr").asLong();
        } catch (InterruptedException e) {
            errorMessage = e.getMessage();
            log.error("Error getting current price for stock: {}. Interrupted.", stockCode, e);
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted during get current price for stock: " + stockCode, e);
        } finally {
            logService.saveLog("getCurrentPrice", url, responseBody, status, errorMessage);
        }
    }

    private String getStockName(String stockCode) throws IOException {
        String accessToken = kiwoomTokenService.getStoredAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            log.error("Access token is not available. Cannot get stock name for stock: {}.", stockCode);
            throw new IOException("Access token not available for stock name inquiry.");
        }
        String url = apiHost + "/uapi/domestic-stock/v1/quotations/inquire-price?fid_cond_mrkt_div_code=J&fid_input_iscd=" + stockCode;
        HttpRequest stockInfoRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", kiwoomTokenService.getAppKey())
                .header("appsecret", kiwoomTokenService.getAppSecret())
                .header("api-id", "ka10001")
                .GET()
                .build();

        String responseBody = "";
        String status = "ERROR";
        String errorMessage = null;

        try {
            HttpResponse<String> stockInfoResponse = httpClientService.send(stockInfoRequest);
            responseBody = stockInfoResponse.body();
            JsonNode stockInfoResponseBody = objectMapper.readTree(responseBody);
            status = "SUCCESS";
            return stockInfoResponseBody.path("output").path("stk_nm").asText();
        } catch (InterruptedException e) {
            errorMessage = e.getMessage();
            log.error("Error getting stock name for stock: {}. Interrupted.", stockCode, e);
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted during get stock name for stock: " + stockCode, e);
        } finally {
            logService.saveLog("getStockName", url, responseBody, status, errorMessage);
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