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
public class AppSetting {
    @Id
    private Long id; // 단일 레코드를 위한 고정 ID (예: 1L)
    private long totalAmount;
    private double profitMargin;
    private double lossMargin;
}
