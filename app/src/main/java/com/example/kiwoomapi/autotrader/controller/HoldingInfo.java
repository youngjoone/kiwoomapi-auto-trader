package com.example.kiwoomapi.autotrader.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoldingInfo {
    private String stockCode;
    private String stockName;
    private long currentPrice;
    private long buyPrice;
    private long quantity;
    private double profitLoss;
    private double profitLossPercentage;
}
