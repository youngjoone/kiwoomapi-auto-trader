'''package com.example.kiwoomapi.autotrader.controller;

import com.example.kiwoomapi.autotrader.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/upper-limit")
    public ResponseEntity<List<String>> getUpperLimitStocks() {
        try {
            stockService.fetchAndStorePreviousDayUpperLimitStocks();
            List<String> upperLimitStockCodes = stockService.getUpperLimitStockCodes();
            return ResponseEntity.ok(upperLimitStockCodes);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
'''
