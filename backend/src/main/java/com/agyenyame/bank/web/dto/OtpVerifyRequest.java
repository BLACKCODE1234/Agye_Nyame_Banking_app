package com.agyenyame.bank.web.dto;

import jakarta.validation.constraints.NotBlank;

public class OtpVerifyRequest {
    @NotBlank public String reference;
    @NotBlank public String code;
}
