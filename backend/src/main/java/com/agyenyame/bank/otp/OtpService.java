package com.agyenyame.bank.otp;

import com.agyenyame.bank.domain.OtpChallenge;
import com.agyenyame.bank.repository.OtpChallengeRepository;
import com.agyenyame.bank.web.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

/**
 * Issues and verifies OTP challenges. The OTP code is hashed (Argon2id) before storage.
 * A challenge carries a JSON payload describing the pending operation (signup or transfer).
 */
@Service
public class OtpService {

    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpChallengeRepository repository;
    private final PasswordEncoder encoder;
    private final SmsSender smsSender;
    private final int ttlSeconds;
    private final int length;
    private final boolean devReturn;

    public OtpService(OtpChallengeRepository repository,
                      PasswordEncoder encoder,
                      SmsSender smsSender,
                      @Value("${app.otp.ttl-seconds}") int ttlSeconds,
                      @Value("${app.otp.length}") int length,
                      @Value("${app.otp.dev-return}") boolean devReturn) {
        this.repository = repository;
        this.encoder = encoder;
        this.smsSender = smsSender;
        this.ttlSeconds = ttlSeconds;
        this.length = length;
        this.devReturn = devReturn;
    }

    public record IssuedOtp(String reference, String devCode) {}

    /** Creates a challenge, sends the OTP, and returns its reference (and the code in dev mode). */
    @Transactional
    public IssuedOtp issue(OtpChallenge.Purpose purpose, String mobileNumber, String payloadJson) {
        String code = randomCode();
        OtpChallenge challenge = new OtpChallenge();
        challenge.setReference(UUID.randomUUID().toString());
        challenge.setPurpose(purpose);
        challenge.setCodeHash(encoder.encode(code));
        challenge.setMobileNumber(mobileNumber);
        challenge.setPayloadJson(payloadJson);
        challenge.setExpiresAt(Instant.now().plusSeconds(ttlSeconds));
        repository.save(challenge);

        smsSender.send(mobileNumber, "Your Agye Nyame Bank verification code is: " + code
                + " (valid for " + ttlSeconds / 60 + " minutes).");

        return new IssuedOtp(challenge.getReference(), devReturn ? code : null);
    }

    /** Verifies the OTP for a reference and returns the challenge payload. Consumes the challenge. */
    @Transactional
    public OtpChallenge verify(String reference, String purpose, String code) {
        OtpChallenge challenge = repository.findByReference(reference)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Invalid or unknown OTP reference"));

        if (challenge.isConsumed()) {
            throw new ApiException(HttpStatus.CONFLICT, "This OTP has already been used");
        }
        if (Instant.now().isAfter(challenge.getExpiresAt())) {
            throw new ApiException(HttpStatus.GONE, "OTP has expired, please request a new one");
        }
        if (!challenge.getPurpose().name().equals(purpose)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "OTP purpose mismatch");
        }
        if (challenge.getAttempts() >= MAX_ATTEMPTS) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Too many incorrect attempts");
        }
        if (!encoder.matches(code, challenge.getCodeHash())) {
            challenge.setAttempts(challenge.getAttempts() + 1);
            repository.save(challenge);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Incorrect OTP code");
        }

        challenge.setConsumed(true);
        repository.save(challenge);
        return challenge;
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
