package com.example.kiwoomapi.autotrader.log;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LogServiceImpl implements LogService {

    private final LogEntryRepository logEntryRepository;

    public LogServiceImpl(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }

    @Override
    public void saveLog(String apiId, String request, String response, String status, String errorMessage) {
        LogEntry logEntry = new LogEntry();
        logEntry.setTimestamp(LocalDateTime.now());
        logEntry.setApiId(apiId);
        logEntry.setRequest(request);
        logEntry.setResponse(response);
        logEntry.setStatus(status);
        logEntry.setErrorMessage(errorMessage);
        logEntryRepository.save(logEntry);
    }
}
