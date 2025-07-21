package com.example.kiwoomapi.autotrader.websocket;

import com.example.kiwoomapi.autotrader.model.TradeInfo;
import com.example.kiwoomapi.autotrader.service.KiwoomTokenService;
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

@Slf4j
@Component
@ClientEndpoint
public class KiwoomWebSocketClient {

    private Session session;
    private final KiwoomTokenService kiwoomTokenService;
    private final OrderService orderService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, TradeInfo> ownedStocks = new ConcurrentHashMap<>();

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
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("Received message: {}", message);
        try {
            JsonNode rootNode = objectMapper.readTree(message);
            String trId = rootNode.path("trnm").asText(); // trnm for real-time messages
            String stockCode = rootNode.path("item").asText(); // item for stock code

            if ("REAL".equals(trId)) { 
                JsonNode values = rootNode.path("values");
                long currentPrice = values.path("10").asLong(); // 10 for current price (현재가)
                orderService.processRealtimeStockPrice(stockCode, currentPrice);
            }
        } catch (IOException e) {
            log.error("Error processing WebSocket message: {}", message, e);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("WebSocket error for session {}: {}", session.getId(), throwable.getMessage(), throwable);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        log.info("WebSocket closed for session {}: {}", session.getId(), closeReason.getReasonPhrase());
    }

    public void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, URI.create(websocketHost));
        } catch (DeploymentException | IOException e) {
            log.error("Error connecting to WebSocket: {}", e.getMessage(), e);
        }
    }

    public void subscribeToRealtimeStockPrice(String stockCode) {
        String accessToken = kiwoomTokenService.getStoredAccessToken();
        if (session != null && session.isOpen() && accessToken != null) {
            // Kiwoom API real-time subscription message format for OB (주식체결)
            String subscribeMessage = String.format("{\"trnm\":\"REG\",\"grp_no\":\"1\",\"refresh\":\"1\",\"data\":[{\"item\":\"%s\",\"type\":\"OB\"}]}", stockCode);
            try {
                session.getBasicRemote().sendText(subscribeMessage);
                log.info("Subscribed to real-time data for stock: {}", stockCode);
            } catch (IOException e) {
                log.error("Error subscribing to real-time data for stock {}: {}", stockCode, e);
            }
        } else {
            log.warn("WebSocket session is not open or access token is null. Cannot subscribe to {}.", stockCode);
        }
    }

    public void addOwnedStock(TradeInfo tradeInfo) {
        ownedStocks.put(tradeInfo.getStockCode(), tradeInfo);
        subscribeToRealtimeStockPrice(tradeInfo.getStockCode());
    }

    public void removeOwnedStock(String stockCode) {
        ownedStocks.remove(stockCode);
        // Optionally unsubscribe from real-time data
    }
}