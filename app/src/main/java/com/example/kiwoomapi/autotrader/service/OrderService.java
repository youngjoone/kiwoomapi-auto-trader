package com.example.kiwoomapi.autotrader.service;

import com.example.kiwoomapi.autotrader.model.TradeInfo;

import java.io.IOException;

public interface OrderService {
    void placeBuyOrder(String stockCode, long quantity) throws IOException;
    void placeSellOrder(String stockCode, long quantity) throws IOException;
    void addTradeInfo(TradeInfo tradeInfo);
    void removeTradeInfo(String stockCode);
    void startRealtimeMonitoring();
    void processRealtimeStockPrice(String stockCode, long currentPrice) throws IOException;
}
