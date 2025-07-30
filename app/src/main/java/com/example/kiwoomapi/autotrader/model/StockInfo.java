package com.example.kiwoomapi.autotrader.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockInfo {
    private final String stockName;
    private final long currentPrice;
}
