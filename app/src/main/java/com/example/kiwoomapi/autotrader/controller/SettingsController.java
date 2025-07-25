package com.example.kiwoomapi.autotrader.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    @Value("${kiwoom.order.total-amount}")
    private long totalAmount;

    @Value("${kiwoom.order.profit-margin}")
    private double profitMargin;

    @Value("${kiwoom.order.loss-margin}")
    private double lossMargin;

    // TODO: 실제 애플리케이션에서는 설정 값을 동적으로 변경하고 저장하는 로직이 필요합니다.
    // 현재는 @Value를 통해 주입받은 값을 반환하며, PUT 요청은 단순히 로그만 남깁니다.
    // 영구적인 저장을 위해서는 DB 연동 또는 설정 파일 수정 로직이 필요합니다.

    @GetMapping
    public Map<String, Object> getSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("totalAmount", totalAmount);
        settings.put("profitMargin", profitMargin);
        settings.put("lossMargin", lossMargin);
        return settings;
    }

    @PutMapping
    public Map<String, Object> updateSettings(@RequestBody Map<String, Object> newSettings) {
        // 실제 운영 환경에서는 이 값을 영구적으로 저장하는 로직이 필요합니다.
        // 예를 들어, 데이터베이스에 저장하거나, 설정 파일을 업데이트하는 방식 등
        if (newSettings.containsKey("totalAmount")) {
            this.totalAmount = Long.parseLong(newSettings.get("totalAmount").toString());
        }
        if (newSettings.containsKey("profitMargin")) {
            this.profitMargin = Double.parseDouble(newSettings.get("profitMargin").toString());
        }
        if (newSettings.containsKey("lossMargin")) {
            this.lossMargin = Double.parseDouble(newSettings.get("lossMargin").toString());
        }

        Map<String, Object> updatedSettings = new HashMap<>();
        updatedSettings.put("totalAmount", this.totalAmount);
        updatedSettings.put("profitMargin", this.profitMargin);
        updatedSettings.put("lossMargin", this.lossMargin);
        return updatedSettings;
    }
}
