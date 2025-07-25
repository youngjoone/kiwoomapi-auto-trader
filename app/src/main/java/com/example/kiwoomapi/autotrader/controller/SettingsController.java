package com.example.kiwoomapi.autotrader.controller;

import com.example.kiwoomapi.autotrader.model.AppSetting;
import com.example.kiwoomapi.autotrader.service.SettingsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public AppSetting getSettings() {
        return settingsService.getSettings();
    }

    @PutMapping
    public AppSetting updateSettings(@RequestBody AppSetting newSettings) {
        return settingsService.updateSettings(newSettings);
    }
}
