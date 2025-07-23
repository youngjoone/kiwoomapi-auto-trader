package com.example.kiwoomapi.autotrader.service;

import com.example.kiwoomapi.autotrader.http.HttpClientService;
import com.example.kiwoomapi.autotrader.log.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KiwoomTokenServiceTest {

    @Mock
    private LogService logService;

    @Mock
    private HttpClientService httpClientService;

    @InjectMocks
    private KiwoomTokenServiceImpl kiwoomTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(kiwoomTokenService, "appKey", "testAppKey");
        ReflectionTestUtils.setField(kiwoomTokenService, "appSecret", "testAppSecret");
        ReflectionTestUtils.setField(kiwoomTokenService, "account", "testAccount");
    }

    @Test
    void getAccessToken_success() throws IOException, InterruptedException {
        // Given
        String mockResponseBody = "{\"token\":\"mockAccessToken\",\"expires_in\":3600}";
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(httpClientService.send(any(HttpRequest.class))).thenReturn(mockResponse);

        String jsonData = "{\"grant_type\":\"client_credentials\",\"appkey\":\"testAppKey\",\"secretkey\":\"testAppSecret\"}";

        // When
        String accessToken = kiwoomTokenService.getAccessToken(jsonData);

        // Then
        assertNotNull(accessToken);
        assertEquals("mockAccessToken", accessToken);
        verify(logService, times(1)).saveLog(anyString(), anyString(), anyString(), eq("SUCCESS"), isNull());
    }

    @Test
    void getAccessToken_apiError() throws IOException, InterruptedException {
        // Given
        String mockResponseBody = "{\"error\":\"invalid_grant\"}";
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(httpClientService.send(any(HttpRequest.class))).thenReturn(mockResponse);

        String jsonData = "{\"grant_type\":\"client_credentials\",\"appkey\":\"testAppKey\",\"secretkey\":\"testAppSecret\"}";

        // When
        String accessToken = kiwoomTokenService.getAccessToken(jsonData);

        // Then
        assertNull(accessToken);
        verify(logService, times(1)).saveLog(anyString(), anyString(), anyString(), eq("ERROR"), anyString());
    }

    @Test
    void revokeAccessToken_success() throws IOException, InterruptedException {
        // Given
        ReflectionTestUtils.setField(kiwoomTokenService, "accessToken", "existingToken");
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"message\":\"Token revoked\"}"); // Mock a non-empty response body
        when(httpClientService.send(any(HttpRequest.class))).thenReturn(mockResponse);

        // When
        boolean revoked = kiwoomTokenService.revokeAccessToken();

        // Then
        assertTrue(revoked);
        assertNull(ReflectionTestUtils.getField(kiwoomTokenService, "accessToken"));

        ArgumentCaptor<String> requestBodyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> responseBodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(logService, times(1)).saveLog(
                eq("revokeAccessToken"),
                requestBodyCaptor.capture(),
                responseBodyCaptor.capture(),
                eq("SUCCESS"),
                isNull()
        );

        System.out.println("Captured Request Body: " + requestBodyCaptor.getValue());
        System.out.println("Captured Response Body: " + responseBodyCaptor.getValue());

        assertEquals("{\"token\":\"existingToken\"}", requestBodyCaptor.getValue());
        assertEquals("{\"message\":\"Token revoked\"}", responseBodyCaptor.getValue());
    }

    @Test
    void revokeAccessToken_noToken() throws IOException, InterruptedException {
        // Given
        ReflectionTestUtils.setField(kiwoomTokenService, "accessToken", null);

        // When
        boolean revoked = kiwoomTokenService.revokeAccessToken();

        // Then
        assertFalse(revoked);
        verify(logService, times(1)).saveLog(eq("revokeAccessToken"), eq(""), eq(""), eq("ERROR"), eq("No token to revoke."));
    }

    @Test
    void getStoredAccessToken_tokenExistsAndValid() throws IOException, InterruptedException {
        // Given
        ReflectionTestUtils.setField(kiwoomTokenService, "accessToken", "validToken");
        ReflectionTestUtils.setField(kiwoomTokenService, "expiresIn", System.currentTimeMillis() + 3600 * 1000);

        // When
        String token = kiwoomTokenService.getStoredAccessToken();

        // Then
        assertEquals("validToken", token);
        verify(httpClientService, never()).send(any(HttpRequest.class)); // Should not call API
    }

    @Test
    void getStoredAccessToken_tokenExpired_renewalSuccess() throws IOException, InterruptedException {
        // Given
        ReflectionTestUtils.setField(kiwoomTokenService, "accessToken", "expiredToken");
        ReflectionTestUtils.setField(kiwoomTokenService, "expiresIn", System.currentTimeMillis() - 1000);

        String mockResponseBody = "{\"token\":\"renewedAccessToken\",\"expires_in\":3600}";
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(httpClientService.send(any(HttpRequest.class))).thenReturn(mockResponse);

        // When
        String token = kiwoomTokenService.getStoredAccessToken();

        // Then
        assertEquals("renewedAccessToken", token);
        verify(httpClientService, times(1)).send(any(HttpRequest.class)); // Should call API for renewal
    }

    @Test
    void getStoredAccessToken_tokenExpired_renewalFailed() throws IOException, InterruptedException {
        // Given
        ReflectionTestUtils.setField(kiwoomTokenService, "accessToken", "expiredToken");
        ReflectionTestUtils.setField(kiwoomTokenService, "expiresIn", System.currentTimeMillis() - 1000);

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(400);
        when(httpClientService.send(any(HttpRequest.class))).thenReturn(mockResponse);

        // When
        String token = kiwoomTokenService.getStoredAccessToken();

        // Then
        assertNull(token);
        verify(httpClientService, times(1)).send(any(HttpRequest.class)); // Should call API for renewal
    }
}