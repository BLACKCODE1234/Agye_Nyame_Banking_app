package com.agyenyame.bank.web.dto;

import java.math.BigDecimal;

public class AmountRequest {
    public BigDecimal amount;
    public String pin; // required for withdraw
}
