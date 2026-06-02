package com.example.banking.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Account {
    private final String id;
    private final String ownerName;
    private final String mobileNumber;
    private String pin;

    private BigDecimal balance;
    private final List<Transaction> transactions = new ArrayList<>();

    public Account(String id, String ownerName, String mobileNumber, String pin, BigDecimal openingBalance) {
        this.id = id;
        this.ownerName = ownerName;
        this.mobileNumber = mobileNumber;
        this.pin = pin;
        this.balance = openingBalance;
    }

    public String getId() {
        return id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }
}

