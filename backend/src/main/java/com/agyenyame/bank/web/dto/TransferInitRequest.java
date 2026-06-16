package com.agyenyame.bank.web.dto;

import java.math.BigDecimal;

public class TransferInitRequest {
    public String transferMobileNumber;
    public BigDecimal amount;
    public String pin;
}
