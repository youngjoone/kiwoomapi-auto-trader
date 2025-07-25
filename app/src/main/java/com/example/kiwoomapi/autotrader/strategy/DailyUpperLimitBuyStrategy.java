package com.example.kiwoomapi.autotrader.strategy;

import com.example.kiwoomapi.autotrader.controller.StockData;
import com.example.kiwoomapi.autotrader.service.OrderService;
import com.example.kiwoomapi.autotrader.service.StockService;
import com.example.kiwoomapi.autotrader.service.StrategyStatusService; // StrategyStatusService import 추가
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.time.LocalDateTime; // LocalDateTime import 추가
import java.util.List;

@Slf4j
@Component
public class DailyUpperLimitBuyStrategy implements TradingStrategy {

    private final StockService stockService;
    private final OrderService orderService;
    private final StrategyStatusService strategyStatusService; // StrategyStatusService 필드 추가

    @Value("${kiwoom.order.total-amount}")
    private long totalInvestmentAmount;

    public DailyUpperLimitBuyStrategy(StockService stockService, OrderService orderService, StrategyStatusService strategyStatusService) {
        this.stockService = stockService;
        this.orderService = orderService;
        this.strategyStatusService = strategyStatusService;
    }

    @Override
    @Scheduled(cron = "0 0 9 ? * MON-FRI", zone = "Asia/Seoul") // 월-금 오전 9시
    public void execute() throws IOException {
        log.info("DailyUpperLimitBuyStrategy executed at 9:00 AM.");
        strategyStatusService.setLastStrategyRunTime(LocalDateTime.now()); // 전략 실행 시간 업데이트
        List<StockData> upperLimitStocks = stockService.getUpperLimitStocks();

        if (upperLimitStocks.isEmpty()) {
            log.info("No upper limit stocks found for today.");
            return;
        }

        // 실제 구현에서는 계좌 잔고, 종목별 최소/최대 주문 수량 등을 고려해야 합니다.
        long amountPerStock = totalInvestmentAmount / upperLimitStocks.size();

        for (StockData stock : upperLimitStocks) {
            try {
                // 현재가 조회 (StockData에 이미 현재가가 있으므로 재사용)
                long currentPrice = Long.parseLong(stock.getCur_prc());
                long quantity = amountPerStock / currentPrice;

                if (quantity > 0) {
                    log.info("Placing buy order for {} - Quantity: {}", stock.getStk_nm(), quantity);
                    orderService.placeBuyOrder(stock.getStk_cd(), quantity);
                } else {
                    log.warn("Calculated quantity for {} is 0. Skipping buy order.", stock.getStk_nm());
                }
            } catch (NumberFormatException e) {
                log.error("Error parsing current price for stock {}: {}", stock.getStk_nm(), e.getMessage());
            } catch (Exception e) {
                log.error("Error placing buy order for stock {}: {}", stock.getStk_nm(), e.getMessage());
            }
        }
    }
}
