package com.example.kiwoomapi.autotrader.controller;

import com.example.kiwoomapi.autotrader.model.TradeInfoRepository;
import com.example.kiwoomapi.autotrader.model.TradeHistoryRepository;
import com.example.kiwoomapi.autotrader.model.TradeHistory;
import com.example.kiwoomapi.autotrader.service.StrategyStatusService; // StrategyStatusService import 추가
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final TradeInfoRepository tradeInfoRepository;
    private final TradeHistoryRepository tradeHistoryRepository;
    private final StrategyStatusService strategyStatusService; // StrategyStatusService 필드 추가

    public DashboardController(TradeInfoRepository tradeInfoRepository, TradeHistoryRepository tradeHistoryRepository, StrategyStatusService strategyStatusService) {
        this.tradeInfoRepository = tradeInfoRepository;
        this.tradeHistoryRepository = tradeHistoryRepository;
        this.strategyStatusService = strategyStatusService;
    }

    @GetMapping
    public DashboardInfo getDashboardInfo() {
        long totalOwnedStocks = tradeInfoRepository.count();
        long totalTrades = tradeHistoryRepository.count();
        double totalProfitLoss = tradeHistoryRepository.findAll().stream()
                .mapToDouble(TradeHistory::getProfitLoss)
                .sum();

        String lastStrategyRunTime = strategyStatusService.getLastStrategyRunTime(); // StrategyStatusService에서 가져오기

        return new DashboardInfo(totalOwnedStocks, totalTrades, totalProfitLoss, lastStrategyRunTime);
    }
}
