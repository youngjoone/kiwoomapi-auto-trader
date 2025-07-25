package com.example.kiwoomapi.autotrader.service;

import com.example.kiwoomapi.autotrader.model.AppSetting;
import com.example.kiwoomapi.autotrader.model.AppSettingRepository;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class SettingsService {

    private final AppSettingRepository appSettingRepository;

    public SettingsService(AppSettingRepository appSettingRepository) {
        this.appSettingRepository = appSettingRepository;
    }

    @PostConstruct
    public void init() {
        // 애플리케이션 시작 시 설정 값이 없으면 기본값으로 초기화
        if (appSettingRepository.count() == 0) {
            AppSetting defaultSettings = new AppSetting(1L, 1000000L, 5.0, -5.0);
            appSettingRepository.save(defaultSettings);
        }
    }

    public AppSetting getSettings() {
        return appSettingRepository.findById(1L).orElseThrow(() -> new RuntimeException("App settings not found"));
    }

    public AppSetting updateSettings(AppSetting newSettings) {
        AppSetting existingSettings = getSettings();
        existingSettings.setTotalAmount(newSettings.getTotalAmount());
        existingSettings.setProfitMargin(newSettings.getProfitMargin());
        existingSettings.setLossMargin(newSettings.getLossMargin());
        return appSettingRepository.save(existingSettings);
    }
}
