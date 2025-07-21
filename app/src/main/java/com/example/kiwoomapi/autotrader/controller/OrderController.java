package com.example.kiwoomapi.autotrader.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    // 실제 주문을 넣는 로직.

    // 전날 상한가 였던 종목을 가져온다.

    // 월화스목금 매일 아침 9시에 장이 열리면 해당종목을 구매한다.

    // 사용자가 임의로 구매할 금액을 정한다
    // 예) 100만원 으로 사용자가 설정한다면  주식종목가격 * 개수 < 100만원 으로 설정

    // 내 계좌에서 가지고있는 종목들의 가격을 실시간으로 확인 후 구매가격에서 +5%나 -5%가 돼면 판매한다.


}
