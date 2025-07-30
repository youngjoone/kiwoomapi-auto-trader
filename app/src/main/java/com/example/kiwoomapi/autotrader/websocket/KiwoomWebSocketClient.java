package com.example.kiwoomapi.autotrader.websocket;

import com.example.kiwoomapi.autotrader.controller.StockData;
import com.example.kiwoomapi.autotrader.service.KiwoomTokenService;
import com.example.kiwoomapi.autotrader.service.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class KiwoomWebSocketClient extends TextWebSocketHandler {

    private WebSocketSession session;
    private final KiwoomTokenService kiwoomTokenService;
    private final OrderService orderService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Value("${kiwoom.api.websocket-host}")
    private String websocketHost;

    public KiwoomWebSocketClient(KiwoomTokenService kiwoomTokenService, @Lazy OrderService orderService) {
        this.kiwoomTokenService = kiwoomTokenService;
        this.orderService = orderService;
    }

    public void connect() {
        try {
            StandardWebSocketClient client = new StandardWebSocketClient();
            org.springframework.web.socket.WebSocketHttpHeaders headers = new org.springframework.web.socket.WebSocketHttpHeaders();
            String accessToken = kiwoomTokenService.getStoredAccessToken();
            if (accessToken != null && !accessToken.isEmpty()) {
                headers.add("authorization", "Bearer " + accessToken);
                headers.add("appkey", kiwoomTokenService.getAppKey());
                headers.add("appsecret", kiwoomTokenService.getAppSecret());
                headers.add("tr_type", "1"); // 1: 실시간 등록
            }
            this.session = client.execute(this, headers, URI.create(websocketHost)).get(10, TimeUnit.SECONDS); // Timeout increased to 10s
            log.info("Successfully connected to WebSocket: {}", this.session.getId());
        } catch (Exception e) {
            log.error("Error connecting to WebSocket: {}", e.getMessage(), e);
            reconnect();
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
        this.session = session;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String messagePayload = message.getPayload();
        log.info("Received message: {}", messagePayload);
        try {
            JsonNode rootNode = objectMapper.readTree(messagePayload);
            // Check if it's a real-time execution data
            if (rootNode.has("header") && "H0STCNI0".equals(rootNode.path("header").path("tr_id").asText())) {
                JsonNode body = rootNode.path("body");
                StockData stockData = new StockData();
                stockData.setStk_cd(body.path("stck_shrn_iscd").asText());
                stockData.setCur_prc(body.path("stck_prpr").asText());
                // Pass to order service to process
                orderService.processRealtimeStockPrice(stockData);
            }
        } catch (IOException e) {
            log.error("Error processing WebSocket message: {}", messagePayload, e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed for session {}: {}", session.getId(), status);
        this.session = null; // Clear the session
        reconnect();
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage(), exception);
        this.session = null; // Clear the session
        reconnect();
    }

    private void reconnect() {
        log.warn("Attempting to reconnect to WebSocket in 5 seconds...");
        scheduler.schedule(this::connect, 5, TimeUnit.SECONDS);
    }

    public void subscribeToRealtimeStockPrice(String stockCode) {
        if (session != null && session.isOpen()) {
            String accessToken = kiwoomTokenService.getStoredAccessToken();
            String subscribeMessage = String.format("{\"header\":{\"authorization\":\"Bearer %s\",\"appkey\":\"%s\",\"appsecret\":\"%s\",\"tr_type\":\"1\"},\"body\":{\"input\":{\"tr_id\":\"H0STCNI0\",\"tr_key\":\"%s\"}}}", 
                accessToken, kiwoomTokenService.getAppKey(), kiwoomTokenService.getAppSecret(), stockCode);
            try {
                session.sendMessage(new TextMessage(subscribeMessage));
                log.info("Subscribed to real-time data for stock: {}", stockCode);
            } catch (IOException e) {
                log.error("Error subscribing to real-time data for stock {}: {}", stockCode, e);
            }
        } else {
            log.warn("WebSocket session is not open. Cannot subscribe to {}.", stockCode);
        }
    }

    public void unsubscribeFromRealtimeStockPrice(String stockCode, String type) {
        if (session != null && session.isOpen()) {
            String accessToken = kiwoomTokenService.getStoredAccessToken();
            String unsubscribeMessage = String.format("{\"header\":{\"authorization\":\"Bearer %s\",\"appkey\":\"%s\",\"appsecret\":\"%s\",\"tr_type\":\"2\"},\"body\":{\"input\":{\"tr_id\":\"H0STCNI0\",\"tr_key\":\"%s\"}}}", 
                accessToken, kiwoomTokenService.getAppKey(), kiwoomTokenService.getAppSecret(), stockCode);
            try {
                session.sendMessage(new TextMessage(unsubscribeMessage));
                log.info("Unsubscribed from real-time data for stock {} and type {}.", stockCode, type);
            } catch (IOException e) {
                log.error("Error unsubscribing from real-time data for stock {} and type {}: {}", stockCode, type, e);
            }
        } else {
            log.warn("WebSocket session is not open. Cannot unsubscribe from {} ({}).", stockCode, type);
        }
    }
    
    public WebSocketSession getSession() {
        return session;
    }
}
