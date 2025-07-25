package com.example.kiwoomapi.autotrader.controller;

import com.example.kiwoomapi.autotrader.model.TradeInfo;
import com.example.kiwoomapi.autotrader.model.TradeInfoRepository;
import com.example.kiwoomapi.autotrader.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/holdings")
public class HoldingsController {

    private final TradeInfoRepository tradeInfoRepository;
    private final OrderService orderService;

    public HoldingsController(TradeInfoRepository tradeInfoRepository, OrderService orderService) {
        this.tradeInfoRepository = tradeInfoRepository;
        this.orderService = orderService;
    }

    @GetMapping
    public List<HoldingInfo> getAllHoldings() throws IOException {
        List<HoldingInfo> holdings = new ArrayList<>();
        List<TradeInfo> ownedStocks = tradeInfoRepository.findAll();

        for (TradeInfo tradeInfo : ownedStocks) {
            long currentPrice = orderService.getCurrentPrice(tradeInfo.getStockCode());
            double profitLoss = (currentPrice - tradeInfo.getBuyPrice()) * tradeInfo.getQuantity();
            double profitLossPercentage = ((double) (currentPrice - tradeInfo.getBuyPrice()) / tradeInfo.getBuyPrice()) * 100;

            holdings.add(new HoldingInfo(
                tradeInfo.getStockCode(),
                tradeInfo.getStockName(),
                currentPrice,
                tradeInfo.getBuyPrice(),
                tradeInfo.getQuantity(),
                profitLoss,
                profitLossPercentage
            ));
        }
        return holdings;
    }
}
