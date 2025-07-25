package com.example.kiwoomapi.autotrader.service;

import java.io.IOException;
import java.util.List;

public interface StockService {

    void fetchAndStorePreviousDayUpperLimitStocks() throws IOException, InterruptedException;
    List<com.example.kiwoomapi.autotrader.controller.StockData> getUpperLimitStocks();
}
