package com.agyenyame.bank.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * A pending, OTP-gated operation. Two kinds are supported:
 *  - SIGNUP: holds the new user's details until OTP is verified, then the account is created.
 *  - TRANSFER: holds a pending transfer until OTP is verified, then the transfer executes.
 */
@Entity
@Table(name = "otp_challenges")
public class OtpChallenge {

    public enum Purpose { SIGNUP, TRANSFER }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Opaque reference handed to the client to continue the flow. */
    @Column(nullable = false, unique = true)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Purpose purpose;

    /** Argon2id hash of the OTP code. Never store plaintext. */
    @Column(nullable = false)
    private String codeHash;

    /** Destination of the OTP (mobile number). */
    @Column(nullable = false)
    private String mobileNumber;

    /** JSON payload describing the pending operation. */
    @Lob
    @Column(nullable = false)
    private String payloadJson;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean consumed = false;

    private int attempts = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public Purpose getPurpose() { return purpose; }
    public void setPurpose(Purpose purpose) { this.purpose = purpose; }
    public String getCodeHash() { return codeHash; }
    public void setCodeHash(String codeHash) { this.codeHash = codeHash; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public boolean isConsumed() { return consumed; }
    public void setConsumed(boolean consumed) { this.consumed = consumed; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
}
