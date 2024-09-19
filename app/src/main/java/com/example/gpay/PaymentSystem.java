package com.example.gpay;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class PaymentSystem {
    private HashMap<String, User> users = new HashMap<>();
    private ArrayList<UserTransaction> transactions = new ArrayList<>();
    private FirebaseFirestore db;

    public PaymentSystem() {
        db = FirebaseFirestore.getInstance();
    }

    public void registerUser(String phoneNum, double initialAmount) {
        if (!users.containsKey(phoneNum)) {
            User newUser = new User(phoneNum, initialAmount);
            users.put(phoneNum, newUser);

            // Save user data to Firestore
            db.collection("users").document(phoneNum)
                    .set(newUser)
                    .addOnSuccessListener(aVoid -> {
                        System.out.println("User registered successfully in Firestore.");
                    })
                    .addOnFailureListener(e -> {
                        System.err.println("Error registering user: " + e.getMessage());
                    });
        } else {
            System.out.println("User already exists.");
        }
    }

    public void loadUser(String phoneNum) {
        db.collection("users").document(phoneNum).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            User user = document.toObject(User.class);
                            users.put(phoneNum, user);
                        } else {
                            System.out.println("User not found in Firestore.");
                        }
                    } else {
                        System.err.println("Error loading user: " + task.getException());
                    }
                });
    }

    public User login(String phoneNum) {
        loadUser(phoneNum); // Load user from Firestore
        return users.get(phoneNum);
    }

    public boolean transferAmount(String senderPhone, String recipientPhone, double amount) {
        User sender = login(senderPhone);
        User recipient = login(recipientPhone);

        if (sender != null && recipient != null) {
            if (amount <= sender.getAvailableAmount()) {
                sender.setAvailableAmount(sender.getAvailableAmount() - amount);
                recipient.setAvailableAmount(recipient.getAvailableAmount() + amount);
                UserTransaction transaction = new UserTransaction(senderPhone, recipientPhone, amount);
                transactions.add(transaction);

                // Update users in Firestore
                updateUser(sender);
                updateUser(recipient);
                // Store transaction in Firestore
                saveTransaction(transaction);

                System.out.println("Transaction successful!");
                return true;
            } else {
                System.out.println("Insufficient funds.");
                return false;
            }
        } else {
            System.out.println("Transaction failed.");
            return false;
        }
    }

    public void updateUser(User user) {
        db.collection("users").document(user.getPhoneNum())
                .set(user, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    System.out.println("User updated successfully in Firestore.");
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error updating user: " + e.getMessage());
                });
    }

    public void addBalance(String phoneNum, double amount) {
        User user = login(phoneNum);
        if (user != null) {
            user.setAvailableAmount(user.getAvailableAmount() + amount);
            updateUser(user);
            System.out.println("Added $" + amount + " to " + phoneNum + "'s account.");
        }
    }

    public void displayUserInfo(User user) {
        if (user != null) {
            System.out.println("User: " + user.getPhoneNum() + ", Available Amount: $" + user.getAvailableAmount());
            for (UserTransaction transaction : transactions) {
                if (transaction.getFrom().equals(user.getPhoneNum()) ||
                        transaction.getTo().equals(user.getPhoneNum())) {
                    System.out.println(transaction);
                }
            }
        } else {
            System.out.println("User not found.");
        }
    }

    private void saveTransaction(UserTransaction transaction) {
        db.collection("transactions").add(transaction)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Transaction saved successfully.");
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error saving transaction: " + e.getMessage());
                });
    }
}
