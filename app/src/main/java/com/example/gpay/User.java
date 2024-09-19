package com.example.gpay;

public class User {
    private String phoneNum;
    private double availableAmount;

    public User() {
        // Required empty constructor for Firestore serialization
    }

    public User(String phoneNum, double availableAmount) {
        this.phoneNum = phoneNum;
        this.availableAmount = availableAmount;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public double getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(double availableAmount) {
        this.availableAmount = availableAmount;
    }
}
