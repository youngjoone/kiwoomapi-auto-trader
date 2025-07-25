package com.example.kiwoomapi.autotrader;

import com.example.kiwoomapi.autotrader.service.KiwoomTokenService;
import com.example.kiwoomapi.autotrader.model.TradeHistory;
import com.example.kiwoomapi.autotrader.model.TradeHistoryRepository;
import com.example.kiwoomapi.autotrader.model.TradeInfo;
import com.example.kiwoomapi.autotrader.model.TradeInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableRetry
@EnableScheduling
public class App {

    @Value("${kiwoom.api.appkey}")
    private String kiwoomAppkey;

    @Value("${kiwoom.api.secretkey}")
    private String kiwoomSecretkey;

    @Autowired
    private KiwoomTokenService kiwoomTokenService;

    @Autowired
    private TradeHistoryRepository tradeHistoryRepository;

    @Autowired
    private TradeInfoRepository tradeInfoRepository;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            System.out.println("Attempting to get Kiwoom API Access Token...");
            String jsonData = String.format(
                "{\"grant_type\" : \"client_credentials\",\"appkey\" : \"%s\",\"secretkey\" : \"%s\"}",
                kiwoomAppkey, kiwoomSecretkey
            );
            String accessToken = kiwoomTokenService.getAccessToken(jsonData);

            if (accessToken != null) {
                System.out.println("Kiwoom API Access Token obtained successfully: " + accessToken);
            } else {
                System.err.println("Failed to obtain Kiwoom API Access Token.");
            }

            // 보유 종목 더미 데이터 추가
            System.out.println("Adding dummy owned stocks data...");
            tradeInfoRepository.save(new TradeInfo("005930", "삼성전자", 70000, 10, System.currentTimeMillis() - 86400000L));
            tradeInfoRepository.save(new TradeInfo("000660", "SK하이닉스", 100000, 5, System.currentTimeMillis() - 172800000L));
            tradeInfoRepository.save(new TradeInfo("035420", "NAVER", 200000, 3, System.currentTimeMillis() - 259200000L));
            System.out.println("Dummy owned stocks data added.");
        };
    }
}