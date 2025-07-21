package com.example.kiwoomapi.autotrader.service;

import java.io.IOException;

public interface StockService {

    void fetchAndStorePreviousDayUpperLimitStocks() throws IOException;
}
