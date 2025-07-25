package com.example.kiwoomapi.autotrader.service;

import com.example.kiwoomapi.autotrader.controller.StockData;
import com.example.kiwoomapi.autotrader.http.HttpClientService;
import com.example.kiwoomapi.autotrader.log.LogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StockServiceTest {

    @Mock
    private KiwoomTokenService kiwoomTokenService;

    @Mock
    private LogService logService;

    @Mock
    private HttpClientService httpClientService;

    @InjectMocks
    private StockServiceImpl stockService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(stockService, "apiHost", "http://test-api.kiwoom.com");
        ReflectionTestUtils.setField(stockService, "objectMapper", objectMapper);
    }

    @Test
    void fetchAndStorePreviousDayUpperLimitStocks_success() throws IOException, InterruptedException {
        // Given
        when(kiwoomTokenService.getStoredAccessToken()).thenReturn("testAccessToken");
        when(kiwoomTokenService.getAppKey()).thenReturn("testAppKey");
        when(kiwoomTokenService.getAppSecret()).thenReturn("testAppSecret");

        String mockResponseBody = "{\"updown_pric\":[{\"stk_cd\":\"000020\",\"stk_nm\":\"동화약품\"},{\"stk_cd\":\"000040\",\"stk_nm\":\"S-Oil\"}]}";
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(httpClientService.send(any(HttpRequest.class))).thenReturn(mockResponse);

        // When
        stockService.fetchAndStorePreviousDayUpperLimitStocks();

        // Then
        List<StockData> upperLimitStocks = stockService.getUpperLimitStocks();
        assertNotNull(upperLimitStocks);
        assertEquals(2, upperLimitStocks.size());
        List<String> stockCodes = upperLimitStocks.stream().map(StockData::getStk_cd).collect(Collectors.toList());
        assertTrue(stockCodes.contains("000020"));
        assertTrue(stockCodes.contains("000040"));
        verify(logService, times(1)).saveLog(anyString(), anyString(), anyString(), eq("SUCCESS"), isNull());
    }

    @Test
    void fetchAndStorePreviousDayUpperLimitStocks_noAccessToken() {
        // Given
        when(kiwoomTokenService.getStoredAccessToken()).thenReturn(null);

        // When & Then
        IOException thrown = assertThrows(IOException.class, () -> {
            stockService.fetchAndStorePreviousDayUpperLimitStocks();
        });
        assertEquals("Access token not available.", thrown.getMessage());
        verify(logService, times(1)).saveLog(anyString(), anyString(), anyString(), eq("ERROR"), anyString());
    }

    @Test
    void fetchAndStorePreviousDayUpperLimitStocks_apiError() throws IOException, InterruptedException {
        // Given
        when(kiwoomTokenService.getStoredAccessToken()).thenReturn("testAccessToken");
        when(kiwoomTokenService.getAppKey()).thenReturn("testAppKey");
        when(kiwoomTokenService.getAppSecret()).thenReturn("testAppSecret");

        String mockResponseBody = "{\"error\":\"API Error Message\"}";
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(httpClientService.send(any(HttpRequest.class))).thenReturn(mockResponse);

        // When & Then
        IOException thrown = assertThrows(IOException.class, () -> {
            stockService.fetchAndStorePreviousDayUpperLimitStocks();
        });
        assertTrue(thrown.getMessage().contains("Failed to fetch upper limit stocks. Status code: 500, Response:"));
        verify(logService, times(1)).saveLog(anyString(), anyString(), anyString(), eq("ERROR"), anyString());
    }

    @Test
    void getUpperLimitStocks_returnsCurrentStocks() throws IOException, InterruptedException {
        // Given
        when(kiwoomTokenService.getStoredAccessToken()).thenReturn("testAccessToken");
        when(kiwoomTokenService.getAppKey()).thenReturn("testAppKey");
        when(kiwoomTokenService.getAppSecret()).thenReturn("testAppSecret");

        String mockResponseBody = "{\"updown_pric\":[{\"stk_cd\":\"000020\",\"stk_nm\":\"동화약품\"}]}";
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(httpClientService.send(any(HttpRequest.class))).thenReturn(mockResponse);

        stockService.fetchAndStorePreviousDayUpperLimitStocks(); // Populate stocks

        // When
        List<StockData> stockData = stockService.getUpperLimitStocks();

        // Then
        assertNotNull(stockData);
        assertFalse(stockData.isEmpty());
        assertEquals("000020", stockData.get(0).getStk_cd());
    }
}