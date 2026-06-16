package com.agyenyame.bank.web;

import com.agyenyame.bank.domain.OtpChallenge;
import com.agyenyame.bank.domain.Transaction;
import com.agyenyame.bank.domain.User;
import com.agyenyame.bank.otp.OtpService;
import com.agyenyame.bank.service.AccountService;
import com.agyenyame.bank.web.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Account operations. All require a valid JWT.
 * Transfers are OTP-gated:
 *   POST /api/account/transfer        -> verifies PIN/funds, issues an OTP, returns a reference
 *   POST /api/account/transfer/verify -> verifies the OTP, then executes the transfer
 */
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;
    private final OtpService otpService;
    private final ObjectMapper mapper = new ObjectMapper();

    public AccountController(AccountService accountService, OtpService otpService) {
        this.accountService = accountService;
        this.otpService = otpService;
    }

    @GetMapping("/balance")
    public Map<String, Object> balance() {
        User user = CurrentUser.get();
        return Map.of("balance", accountService.requireUser(user.getId()).getBalance());
    }

    @PostMapping("/deposit")
    public Transaction deposit(@RequestBody AmountRequest req) {
        User user = CurrentUser.get();
        return accountService.deposit(user.getId(), req.amount);
    }

    @PostMapping("/withdraw")
    public Transaction withdraw(@RequestBody AmountRequest req) {
        User user = CurrentUser.get();
        return accountService.withdraw(user.getId(), req.amount, req.pin);
    }

    @PostMapping("/transfer")
    public OtpIssuedResponse transferInit(@RequestBody TransferInitRequest req) throws Exception {
        User user = CurrentUser.get();
        // Validate up-front: PIN, amount, recipient existence and funds, before issuing the OTP.
        accountService.verifyPin(accountService.requireUser(user.getId()), req.pin);
        if (req.amount == null || req.amount.signum() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }
        if (req.transferMobileNumber == null || req.transferMobileNumber.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Recipient mobile number is required");
        }
        Map<String, String> payload = Map.of(
                "userId", String.valueOf(user.getId()),
                "recipient", req.transferMobileNumber,
                "amount", req.amount.toPlainString());
        var issued = otpService.issue(OtpChallenge.Purpose.TRANSFER, user.getMobileNumber(),
                mapper.writeValueAsString(payload));
        return new OtpIssuedResponse(issued.reference(),
                "OTP sent to " + user.getMobileNumber() + ". Verify it to complete the transfer.",
                issued.devCode());
    }

    @PostMapping("/transfer/verify")
    public Transaction transferVerify(@Valid @RequestBody OtpVerifyRequest req) throws Exception {
        User user = CurrentUser.get();
        OtpChallenge challenge = otpService.verify(req.reference, "TRANSFER", req.code);
        Map<String, String> payload = mapper.readValue(challenge.getPayloadJson(), Map.class);
        // Ensure the verifying user owns this challenge.
        if (!String.valueOf(user.getId()).equals(payload.get("userId"))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This OTP does not belong to you");
        }
        return accountService.transfer(user.getId(), payload.get("recipient"),
                new BigDecimal(payload.get("amount")));
    }

    @GetMapping("/history")
    public java.util.List<Transaction> history() {
        User user = CurrentUser.get();
        return accountService.history(user.getId());
    }
}
