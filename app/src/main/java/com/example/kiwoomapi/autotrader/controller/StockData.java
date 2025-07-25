package com.example.kiwoomapi.autotrader.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockData {
    private String stk_cd;
    private String stk_nm;
    private String cur_prc;
    private String pred_pre_sig;
    private String pred_pre;
    private String flu_rt;
    private String trde_qty;
    private String pred_trde_qty_pre_rt;
    private String sel_bid;
    private String buy_bid;
    private String high_pric;
    private String low_pric;
}
