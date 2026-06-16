package com.agyenyame.bank.web.dto;

/** Response returned when an OTP challenge is issued. devCode is only populated in dev mode. */
public class OtpIssuedResponse {
    public String reference;
    public String message;
    public String devCode;

    public OtpIssuedResponse(String reference, String message, String devCode) {
        this.reference = reference;
        this.message = message;
        this.devCode = devCode;
    }
}
