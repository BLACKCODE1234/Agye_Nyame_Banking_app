package com.agyenyame.bank.otp;

/** Abstraction for delivering an OTP. Swap the dev implementation for a real SMS gateway in prod. */
public interface SmsSender {
    void send(String mobileNumber, String message);
}
