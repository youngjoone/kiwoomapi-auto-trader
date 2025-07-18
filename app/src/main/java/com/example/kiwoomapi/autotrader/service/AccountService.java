package com.example.kiwoomapi.autotrader.service;

public interface AccountService {
    String getAccountInfo(); // 기존 메서드
    String getAccountBalance(String accountNumber); // fn_kt00018
    String getAccountTransactions(String accountNumber, String startDate, String endDate); // fn_kt00004
    String getAccountDetails(String accountNumber); // fn_ka10085
}