package com.example.kiwoomapi.autotrader.controller;

import com.example.kiwoomapi.autotrader.service.KiwoomTokenService;
import com.example.kiwoomapi.autotrader.websocket.KiwoomWebSocketClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/status")
public class SystemStatusController {

    private final KiwoomTokenService kiwoomTokenService;
    private final KiwoomWebSocketClient kiwoomWebSocketClient;

    public SystemStatusController(KiwoomTokenService kiwoomTokenService, KiwoomWebSocketClient kiwoomWebSocketClient) {
        this.kiwoomTokenService = kiwoomTokenService;
        this.kiwoomWebSocketClient = kiwoomWebSocketClient;
    }

    @GetMapping
    public Map<String, String> getSystemStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("accessTokenStatus", kiwoomTokenService.getStoredAccessToken() != null && !kiwoomTokenService.getStoredAccessToken().isEmpty() ? "Valid" : "Invalid/Missing");
        status.put("websocketConnectionStatus", kiwoomWebSocketClient.getSession() != null && kiwoomWebSocketClient.getSession().isOpen() ? "Connected" : "Disconnected");
        // TODO: 추가적인 시스템 상태 지표 (예: 마지막 전략 실행 성공 여부, DB 연결 상태 등)
        return status;
    }
}
