package com.example.kiwoomapi.autotrader.strategy;

import com.example.kiwoomapi.autotrader.controller.StockData;
import com.example.kiwoomapi.autotrader.service.KiwoomTokenService;
import com.example.kiwoomapi.autotrader.service.OrderService;
import com.example.kiwoomapi.autotrader.service.StockService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UpperLimitBuyStrategy implements TradingStrategy {

    private final StockService stockService;
    private final KiwoomTokenService kiwoomTokenService;
    private final OrderService orderService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kiwoom.order.total-amount}")
    private long totalAmount;

    @Value("${kiwoom.api.host}")
    private String apiHost;

    public UpperLimitBuyStrategy(StockService stockService, KiwoomTokenService kiwoomTokenService, OrderService orderService) {
        this.stockService = stockService;
        this.kiwoomTokenService = kiwoomTokenService;
        this.orderService = orderService;
    }

    @Override
    public void execute() throws IOException {
        List<String> stockCodes = stockService.getUpperLimitStocks().stream()
                .map(StockData::getStk_cd)
                .collect(Collectors.toList());
        if (stockCodes.isEmpty()) {
            log.info("No upper limit stocks to buy.");
            return;
        }

        long amountPerStock = totalAmount / stockCodes.size();
        String accessToken = kiwoomTokenService.getStoredAccessToken();

        for (String stockCode : stockCodes) {
            try {
                // Get current price
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

                HttpResponse<String> priceResponse = HttpClient.newHttpClient().send(priceRequest, HttpResponse.BodyHandlers.ofString());
                JsonNode priceResponseBody = objectMapper.readTree(priceResponse.body());
                long currentPrice = priceResponseBody.path("output").path("stck_prpr").asLong();

                if (currentPrice > 0) {
                    long quantity = amountPerStock / currentPrice;
                    if (quantity > 0) {
                        orderService.placeBuyOrder(stockCode, quantity);
                    }
                }
            } catch (IOException | InterruptedException e) {
                log.error("Error placing buy order for stock: {}", stockCode, e);
                Thread.currentThread().interrupt();
            }
        }
    }
}