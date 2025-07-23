package com.example.kiwoomapi.autotrader.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class TradeInfo {
    @Id
    private String stockCode;
    private long buyPrice;
    private long quantity;
    private long buyTimestamp;
}
