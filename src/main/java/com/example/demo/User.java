package com.example.demo;

public class User { 
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String hashedPin;
    private String accountNumber;

    public User(String firstName, String lastName, String mobileNumber, String hashedPin) { 
            this.firstName = firstName; 
            this.lastName = lastName;
            this.mobileNumber = mobileNumber;
             this.hashedPin = hashedPin; 
             this.accountNumber = "ACC" + System.currentTimeMillis(); }


              public String getFirstName() { return firstName; }
               public String getLastName() { return lastName; }
                public String getMobileNumber() { return mobileNumber; }
                 public String getHashedPin() { return hashedPin; } 
                 public String getAccountNumber() { return accountNumber; } 

                 public void setHashedPin(String hashedPin) {
                     this.hashedPin = hashedPin;

                  }
                 }