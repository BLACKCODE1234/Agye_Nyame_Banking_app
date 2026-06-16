package com.agyenyame.bank.otp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Development SMS sender: logs the message instead of sending a real SMS. */
@Component
public class LoggingSmsSender implements SmsSender {
    private static final Logger log = LoggerFactory.getLogger(LoggingSmsSender.class);

    @Override
    public void send(String mobileNumber, String message) {
        log.info("[DEV-SMS] to {}: {}", mobileNumber, message);
    }
}
