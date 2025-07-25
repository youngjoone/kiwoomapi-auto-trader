package com.example.kiwoomapi.autotrader.controller;

import com.example.kiwoomapi.autotrader.log.LogEntry;
import com.example.kiwoomapi.autotrader.log.LogEntryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/logs")
public class LogController {

    private final LogEntryRepository logEntryRepository;

    public LogController(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }

    @GetMapping
    public List<LogEntry> getAllLogs() {
        return logEntryRepository.findAll();
    }
}
