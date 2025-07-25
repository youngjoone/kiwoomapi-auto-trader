package com.example.kiwoomapi.autotrader.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class TradeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String stockCode;
    private String stockName;
    private long buyPrice;
    private long buyQuantity;
    private long buyTimestamp;
    private long sellPrice;
    private long sellQuantity;
    private long sellTimestamp;
    private double profitLoss;
    private double profitLossPercentage;
}
