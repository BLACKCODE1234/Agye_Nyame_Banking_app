package com.example.banking.service;

import com.example.banking.model.Account;
import com.example.banking.model.Transaction;
import com.example.banking.model.TransactionType;
import com.example.banking.util.InputValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BankingService {
    private final Map<String, Account> accountsById = new ConcurrentHashMap<>();
    private final Map<String, Account> accountsByMobile = new ConcurrentHashMap<>();

    public BankingService() {
        // Demo data (in-memory only).
        addAccount(new Account("1001", "Alice", "9876543210", "1234", new BigDecimal("2500.00")));
        addAccount(new Account("1002", "Bob", "9123456780", "4321", new BigDecimal("1800.00")));
    }

    private void addAccount(Account account) {
        accountsById.put(account.getId(), account);
        accountsByMobile.put(account.getMobileNumber(), account);
    }

    public Account authenticate(String mobileNumber, String pin) {
        String normalizedMobile = InputValidator.requireMobile(mobileNumber);
        String normalizedPin = InputValidator.requirePin(pin);

        Account account = accountsByMobile.get(normalizedMobile);
        if (account == null || !account.getPin().equals(normalizedPin)) {
            throw new InvalidCredentialsException("Invalid mobile number or PIN.");
        }
        return account;
    }

    public void resetPin(String mobileNumber, String accountId, String newPin, String confirmPin) {
        String normalizedMobile = InputValidator.requireMobile(mobileNumber);
        String normalizedPin = InputValidator.requirePin(newPin);
        String normalizedConfirm = InputValidator.requirePin(confirmPin);

        if (!normalizedPin.equals(normalizedConfirm)) {
            throw new InvalidCredentialsException("New PIN and confirmation do not match.");
        }

        Account account = accountsByMobile.get(normalizedMobile);
        if (account == null || !account.getId().equals(accountId == null ? "" : accountId.trim())) {
            throw new InvalidCredentialsException("Mobile number and account ID do not match.");
        }

        account.setPin(normalizedPin);
    }

    public Account getAccount(String accountId) {
        Account account = accountsById.get(accountId);
        if (account == null) {
            throw new InvalidCredentialsException("Account not found.");
        }
        return account;
    }

    public Account deposit(String accountId, BigDecimal amount) {
        Account account = getAccount(accountId);
        BigDecimal normalized = normalizeAmount(amount);

        account.setBalance(account.getBalance().add(normalized));
        account.addTransaction(new Transaction(
                TransactionType.DEPOSIT,
                normalized,
                Instant.now(),
                "Deposit"
        ));
        return account;
    }

    public Account withdraw(String accountId, BigDecimal amount) {
        Account account = getAccount(accountId);
        BigDecimal normalized = normalizeAmount(amount);

        if (account.getBalance().compareTo(normalized) < 0) {
            throw new InsufficientFundsException("Insufficient funds.");
        }

        account.setBalance(account.getBalance().subtract(normalized));
        account.addTransaction(new Transaction(
                TransactionType.WITHDRAWAL,
                normalized,
                Instant.now(),
                "Withdrawal"
        ));
        return account;
    }

    public List<Transaction> getRecentTransactions(String accountId, int limit) {
        Account account = getAccount(accountId);

        List<Transaction> copy = new ArrayList<>(account.getTransactions());
        copy.sort(Comparator.comparing(Transaction::getTimestamp).reversed());
        if (limit <= 0 || copy.size() <= limit) {
            return copy;
        }
        return copy.subList(0, limit);
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidAmountException("Amount is required.");
        }

        // Use 2 decimal places for this demo.
        BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);
        if (normalized.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than 0.");
        }
        return normalized;
    }

    public static class BankingException extends RuntimeException {
        public BankingException(String message) {
            super(message);
        }
    }

    public static class InvalidCredentialsException extends BankingException {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }

    public static class InsufficientFundsException extends BankingException {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }

    public static class InvalidAmountException extends BankingException {
        public InvalidAmountException(String message) {
            super(message);
        }
    }
}

