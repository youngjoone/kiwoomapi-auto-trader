package com.example.kiwoomapi.autotrader.controller;

import com.example.kiwoomapi.autotrader.service.KiwoomTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
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

    @GetMapping("/revoke-token")
    public ResponseEntity<String> revokeToken() {
        try {
            boolean revoked = kiwoomTokenService.revokeAccessToken();
            if (revoked) {
                return ResponseEntity.ok("Token revoked successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to revoke token or no token to revoke.");
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error revoking token: " + e.getMessage());
        }
    }

    @GetMapping("/get-token")
    public ResponseEntity<String> getToken() {
        try {
            String storedToken = kiwoomTokenService.getStoredAccessToken();
            if (storedToken != null && !storedToken.isEmpty()) {
                return ResponseEntity.ok("Current Token: " + storedToken);
            } else {
                // If no token is stored, try to get a new one
                String jsonData = String.format(
                    "{\"grant_type\" : \"client_credentials\",\"appkey\" : \"%s\",\"secretkey\" : \"%s\"}",
                    kiwoomAppkey, kiwoomSecretkey
                );
                String newToken = kiwoomTokenService.getAccessToken(jsonData);
                if (newToken != null) {
                    return ResponseEntity.ok("New Token obtained: " + newToken);
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to obtain token.");
                }
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error obtaining token: " + e.getMessage());
        }
    }
}
