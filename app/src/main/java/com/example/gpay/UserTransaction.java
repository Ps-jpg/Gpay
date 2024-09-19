package com.example.gpay;

public class UserTransaction {
    private String from;
    private String to;
    private double amount;

    public UserTransaction() {
        // Required empty constructor for Firestore serialization
    }

    public UserTransaction(String from, String to, double amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "From: " + from + ", To: " + to + ", Amount: $" + amount;
    }
}
