package com.example.kiwoomapi.autotrader.controller;

import com.example.kiwoomapi.autotrader.service.KiwoomTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*") // 개발 환경에서 모든 출처 허용
public class KiwoomAuthController {

    @Value("${kiwoom.api.appkey}")
    private String kiwoomAppkey;

    @Value("${kiwoom.api.secretkey}")
    private String kiwoomSecretkey;

    @Autowired
    private KiwoomTokenService kiwoomTokenService;

    @GetMapping("/api/hello")
    public String hello() {
        return "Hello from Backend!";
    }

    @GetMapping("/api/revoke-token")
    public String revokeToken() {
        boolean revoked = kiwoomTokenService.revokeAccessToken();
        if (revoked) {
            return "Token revoked successfully.";
        } else {
            return "Failed to revoke token or no token to revoke.";
        }
    }

    @GetMapping("/api/get-token")
    public String getToken() {
        String storedToken = kiwoomTokenService.getStoredAccessToken();
        if (storedToken != null && !storedToken.isEmpty()) {
            return "Current Token: " + storedToken;
        } else {
            // If no token is stored, try to get a new one
            String jsonData = String.format(
                "{\"grant_type\" : \"client_credentials\",\"appkey\" : \"%s\",\"secretkey\" : \"%s\"}",
                kiwoomAppkey, kiwoomSecretkey
            );
            String newToken = kiwoomTokenService.getAccessToken(jsonData);
            if (newToken != null) {
                return "New Token obtained: " + newToken;
            } else {
                return "Failed to obtain token.";
            }
        }
    }
}
