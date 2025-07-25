package com.example.kiwoomapi.autotrader.websocket;

import com.example.kiwoomapi.autotrader.model.TradeInfo;
import com.example.kiwoomapi.autotrader.service.KiwoomTokenService;
import com.example.kiwoomapi.autotrader.controller.StockData;
import com.example.kiwoomapi.autotrader.service.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ClientEndpoint
public class KiwoomWebSocketClient {

    private Session session;
    private final KiwoomTokenService kiwoomTokenService;
    private final OrderService orderService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, TradeInfo> ownedStocks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Value("${kiwoom.api.websocket-host}")
    private String websocketHost;

    public KiwoomWebSocketClient(KiwoomTokenService kiwoomTokenService, OrderService orderService) {
        this.kiwoomTokenService = kiwoomTokenService;
        this.orderService = orderService;
    }

    @OnOpen
    public void onOpen(Session session) {
        log.info("WebSocket opened: {}", session.getId());
        this.session = session;
        // 재연결 시 이전에 구독했던 종목들을 다시 구독
        for (Map.Entry<String, TradeInfo> entry : ownedStocks.entrySet()) {
            subscribeToRealtimeStockPrice(entry.getKey());
        }
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("Received message: {}", message);
        try {
            JsonNode rootNode = objectMapper.readTree(message);
            String trId = rootNode.path("trnm").asText(); // trnm for real-time messages
            String item = rootNode.path("item").asText(); // item for stock code
            String type = rootNode.path("type").asText(); // type for real-time item type (e.g., 0B for stock price)

            if ("REAL".equals(trId) && "0B".equals(type)) { 
                JsonNode values = rootNode.path("values");
                StockData stockData = new StockData();
                stockData.setStk_cd(item);
                stockData.setCur_prc(values.path("10").asText());
                stockData.setPred_pre(values.path("11").asText());
                stockData.setFlu_rt(values.path("12").asText());
                stockData.setSel_bid(values.path("27").asText());
                stockData.setBuy_bid(values.path("28").asText());
                stockData.setTrde_qty(values.path("13").asText()); // 누적거래량
                stockData.setOpen_pric(values.path("16").asText());
                stockData.setHigh_pric(values.path("17").asText());
                stockData.setLow_pric(values.path("18").asText());
                stockData.setPred_pre_sig(values.path("25").asText());
                // pred_trde_qty_pre_rt는 0B 응답에 직접적으로 없으므로, 일단 비워두거나 다른 방식으로 처리해야 합니다.
                stockData.setPred_trde_qty_pre_rt("");

                orderService.processRealtimeStockPrice(stockData);
            }
        } catch (IOException e) {
            log.error("Error processing WebSocket message: {}", message, e);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("WebSocket error for session {}: {}", session.getId(), throwable.getMessage(), throwable);
        reconnect();
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        log.info("WebSocket closed for session {}: {}", session.getId(), closeReason.getReasonPhrase());
        reconnect();
    }

    public void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, URI.create(websocketHost));
        } catch (DeploymentException | IOException e) {
            log.error("Error connecting to WebSocket: {}", e.getMessage(), e);
            reconnect();
        }
    }

    private void reconnect() {
        log.warn("Attempting to reconnect to WebSocket in 5 seconds...");
        scheduler.schedule(this::connect, 5, TimeUnit.SECONDS);
    }

    public void subscribeToRealtimeStockPrice(String stockCode) {
        String accessToken = kiwoomTokenService.getStoredAccessToken();
        if (session != null && session.isOpen() && accessToken != null) {
            // Kiwoom API real-time subscription message format for 0B (주식체결)
            String subscribeMessage = String.format("{\"trnm\":\"REG\",\"grp_no\":\"1\",\"refresh\":\"1\",\"data\":[{\"item\":[\"%s\"],\"type\":[\"0B\"]}]}", stockCode);
            try {
                session.getBasicRemote().sendText(subscribeMessage);
                log.info("Subscribed to real-time data for stock: {}", stockCode);
            } catch (IOException e) {
                log.error("Error subscribing to real-time data for stock {}: {}", stockCode, e);
            }
        }
        else {
            log.warn("WebSocket session is not open or access token is null. Cannot subscribe to {}.", stockCode);
        }
    }

    public void unsubscribeFromRealtimeStockPrice(String stockCode, String type) {
        String accessToken = kiwoomTokenService.getStoredAccessToken();
        if (session != null && session.isOpen() && accessToken != null) {
            String unsubscribeMessage = String.format("{\"trnm\":\"REMOVE\",\"grp_no\":\"1\",\"data\":[{\"item\":[\"%s\"],\"type\":[\"%s\"]}]}", stockCode, type);
            try {
                session.getBasicRemote().sendText(unsubscribeMessage);
                log.info("Unsubscribed from real-time data for stock {} and type {}.", stockCode, type);
            } catch (IOException e) {
                log.error("Error unsubscribing from real-time data for stock {} and type {}: {}", stockCode, type, e);
            }
        }
        else {
            log.warn("WebSocket session is not open or access token is null. Cannot unsubscribe from {} ({}).", stockCode, type);
        }
    }

    public void addOwnedStock(TradeInfo tradeInfo) {
        ownedStocks.put(tradeInfo.getStockCode(), tradeInfo);
        subscribeToRealtimeStockPrice(tradeInfo.getStockCode());
    }

    public void removeOwnedStock(String stockCode) {
        ownedStocks.remove(stockCode);
        // Optionally unsubscribe from real-time data - will be called from OrderService
    }
}
