package com.example.kiwoomapi.autotrader.controller;

import com.example.kiwoomapi.autotrader.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    private final OrderService orderService;

    public TestController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/buy")
    public ResponseEntity<Map<String, String>> testBuyOrder() {
        Map<String, String> response = new HashMap<>();
        String stockCode = "045970"; // 코아시아
        long quantity = 1; // 1주

        try {
            log.info("Test: Attempting to buy {} shares of {} ({}).", quantity, stockCode, "코아시아");
            orderService.placeBuyOrder(stockCode, quantity);
            response.put("status", "SUCCESS");
            response.put("message", "매수 테스트 주문 성공: " + stockCode + " " + quantity + "주");
            log.info("Test: Buy order successful for {}.", stockCode);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Test: Failed to place buy order for {}: {}", stockCode, e.getMessage());
            response.put("status", "ERROR");
            response.put("message", "매수 테스트 주문 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/sell")
    public ResponseEntity<Map<String, String>> testSellOrder() {
        Map<String, String> response = new HashMap<>();
        String stockCode = "045970"; // 코아시아
        long quantity = 1; // 1주

        try {
            log.info("Test: Attempting to sell {} shares of {} ({}).", quantity, stockCode, "코아시아");
            orderService.placeSellOrder(stockCode, quantity);
            response.put("status", "SUCCESS");
            response.put("message", "매도 테스트 주문 성공: " + stockCode + " " + quantity + "주");
            log.info("Test: Sell order successful for {}.", stockCode);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Test: Failed to place sell order for {}: {}", stockCode, e.getMessage());
            response.put("status", "ERROR");
            response.put("message", "매도 테스트 주문 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
