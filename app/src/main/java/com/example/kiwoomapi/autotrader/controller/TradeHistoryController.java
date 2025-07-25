package com.example.kiwoomapi.autotrader.controller;

import com.example.kiwoomapi.autotrader.model.TradeHistory;
import com.example.kiwoomapi.autotrader.model.TradeHistoryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trade-history")
public class TradeHistoryController {

    private final TradeHistoryRepository tradeHistoryRepository;

    public TradeHistoryController(TradeHistoryRepository tradeHistoryRepository) {
        this.tradeHistoryRepository = tradeHistoryRepository;
    }

    @GetMapping
    public List<TradeHistory> getAllTradeHistory() {
        return tradeHistoryRepository.findAll();
    }
}
