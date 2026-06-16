package com.agyenyame.bank.service;

import com.agyenyame.bank.domain.*;
import com.agyenyame.bank.repository.TransactionRepository;
import com.agyenyame.bank.repository.UserRepository;
import com.agyenyame.bank.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder encoder;

    public AccountService(UserRepository userRepository,
                          TransactionRepository transactionRepository,
                          PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.encoder = encoder;
    }

    public User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public void verifyPin(User user, String pin) {
        if (pin == null || !encoder.matches(pin, user.getPinHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Incorrect PIN");
        }
    }

    private void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }
    }

    @Transactional
    public Transaction deposit(Long userId, BigDecimal amount) {
        requirePositive(amount);
        User user = requireUser(userId);
        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);
        return record(user, TransactionType.DEPOSIT, amount, null);
    }

    @Transactional
    public Transaction withdraw(Long userId, BigDecimal amount, String pin) {
        requirePositive(amount);
        User user = requireUser(userId);
        verifyPin(user, pin);
        if (user.getBalance().compareTo(amount) < 0) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Insufficient funds");
        }
        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);
        return record(user, TransactionType.WITHDRAWAL, amount, null);
    }

    /**
     * Executes a transfer. PIN re-verification is expected to have happened at OTP-init time;
     * this method performs the atomic debit/credit.
     */
    @Transactional
    public Transaction transfer(Long userId, String recipientMobile, BigDecimal amount) {
        requirePositive(amount);
        User sender = requireUser(userId);
        if (sender.getMobileNumber().equals(recipientMobile)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot transfer to your own account");
        }
        User recipient = userRepository.findByMobileNumber(recipientMobile)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Recipient not found"));
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Insufficient funds");
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        recipient.setBalance(recipient.getBalance().add(amount));
        userRepository.save(sender);
        userRepository.save(recipient);

        record(recipient, TransactionType.TRANSFER_IN, amount, sender.getMobileNumber());
        return record(sender, TransactionType.TRANSFER_OUT, amount, recipient.getMobileNumber());
    }

    public List<Transaction> history(Long userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    private Transaction record(User user, TransactionType type, BigDecimal amount, String counterparty) {
        Transaction tx = new Transaction();
        tx.setUserId(user.getId());
        tx.setType(type);
        tx.setAmount(amount);
        tx.setCounterpartyMobile(counterparty);
        tx.setResultingBalance(user.getBalance());
        return transactionRepository.save(tx);
    }
}
