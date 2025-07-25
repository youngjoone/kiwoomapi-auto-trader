package com.example.kiwoomapi.autotrader.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class StrategyStatusService {

    private LocalDateTime lastStrategyRunTime;

    public void setLastStrategyRunTime(LocalDateTime lastStrategyRunTime) {
        this.lastStrategyRunTime = lastStrategyRunTime;
    }

    public String getLastStrategyRunTime() {
        if (lastStrategyRunTime == null) {
            return "N/A";
        }
        return lastStrategyRunTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
