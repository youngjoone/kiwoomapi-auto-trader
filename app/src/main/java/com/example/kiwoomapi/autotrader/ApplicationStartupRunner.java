package com.example.kiwoomapi.autotrader;

import com.example.kiwoomapi.autotrader.websocket.KiwoomWebSocketClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartupRunner {

    private final KiwoomWebSocketClient kiwoomWebSocketClient;

    public ApplicationStartupRunner(KiwoomWebSocketClient kiwoomWebSocketClient) {
        this.kiwoomWebSocketClient = kiwoomWebSocketClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        kiwoomWebSocketClient.connect();
    }
}
