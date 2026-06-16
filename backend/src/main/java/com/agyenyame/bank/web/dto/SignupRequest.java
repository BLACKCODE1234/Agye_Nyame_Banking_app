package com.agyenyame.bank.web.dto;

import jakarta.validation.constraints.*;

public class SignupRequest {
    @NotBlank public String firstName;
    @NotBlank public String lastName;
    @NotBlank @Email public String email;
    @NotBlank @Pattern(regexp = "\\d{7,15}", message = "must be 7-15 digits") public String mobileNumber;
    @NotBlank @Pattern(regexp = "\\d{4,6}", message = "must be 4-6 digits") public String pin;
    @NotBlank public String confirmPin;
}
