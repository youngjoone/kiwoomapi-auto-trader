package com.example.kiwoomapi.autotrader.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardInfo {
    private long totalOwnedStocks;
    private long totalTrades;
    private double totalProfitLoss;
    private String lastStrategyRunTime;
}
