package com.example.kiwoomapi.autotrader;

import com.example.kiwoomapi.autotrader.service.KiwoomTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class App {

    @Value("${kiwoom.api.appkey}")
    private String kiwoomAppkey;

    @Value("${kiwoom.api.secretkey}")
    private String kiwoomSecretkey;

    @Autowired
    private KiwoomTokenService kiwoomTokenService;

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
        };
    }
}
