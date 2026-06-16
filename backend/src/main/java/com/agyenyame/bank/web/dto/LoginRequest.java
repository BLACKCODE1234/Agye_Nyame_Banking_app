package com.agyenyame.bank.web.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank public String mobileNumber;
    @NotBlank public String pin;
}
