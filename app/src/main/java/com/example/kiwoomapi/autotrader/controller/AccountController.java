package com.example.kiwoomapi.autotrader.controller;

import com.example.kiwoomapi.autotrader.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class AccountController {

    @Autowired
    private AccountService accountService;

    // 기존 getAccountInfo는 GET 요청으로 유지
    // @GetMapping("/api/account/info")
    // public String getAccountInfo() {
    //     return accountService.getAccountInfo();
    // }

    @PostMapping("/api/account/balance")
    public String getAccountBalance() {
        return accountService.getAccountBalance(null);
    }

    @PostMapping("/api/account/transactions")
    public String getAccountTransactions() {
        return accountService.getAccountTransactions(null, null, null);
    }

    @PostMapping("/api/account/details")
    public String getAccountDetails() {
        return accountService.getAccountDetails(null);
    }
}