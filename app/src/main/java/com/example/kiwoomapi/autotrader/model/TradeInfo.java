package com.example.kiwoomapi.autotrader.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeInfo {
    private String stockCode;
    private long buyPrice;
    private long quantity;
    private long buyTimestamp;
}
