package com.example.gpay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import android.media.MediaPlayer;

public class TransferActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String currentUserPhone;

    private EditText recipientPhoneEditText, amountEditText;
    private TextView userInfoTextView, welcomeMessageTextView, messageTextView;
    private RecyclerView transactionsRecyclerView;
    private TransactionsAdapter transactionsAdapter;
    private List<UserTransaction> transactionList = new ArrayList<>();
    private MediaPlayer mediaPlayer;

    private FrameLayout progressOverlay;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        db = FirebaseFirestore.getInstance();
        currentUserPhone = getIntent().getStringExtra("phoneNum");

        recipientPhoneEditText = findViewById(R.id.recipientPhoneEditText);
        amountEditText = findViewById(R.id.amountEditText);
        userInfoTextView = findViewById(R.id.userInfoTextView);
        welcomeMessageTextView = findViewById(R.id.welcomeMessageTextView);
        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView);
        messageTextView = findViewById(R.id.messageTextView);

        progressOverlay = findViewById(R.id.progressOverlay);
        progressBar = findViewById(R.id.progressBar);
        mediaPlayer = MediaPlayer.create(this, R.raw.button_click);

        welcomeMessageTextView.setText("Welcome, " + currentUserPhone + "!");
        updateUserInfo();
        setupRecyclerView();

        Button transferButton = findViewById(R.id.transferButton);
        transferButton.setOnClickListener(v -> {
            playSound();
            String recipientPhone = recipientPhoneEditText.getText().toString().trim();
            double amount = getAmountFromInput();
            if (recipientPhone.isEmpty()) {
                messageTextView.setText("Please enter recipient phone number.");
            } else if (amount > 0) {
                messageTextView.setText("");
                showLoading(true);
                transferAmount(currentUserPhone, recipientPhone, amount);
            } else {
                messageTextView.setText("Please enter a valid transfer amount.");
            }
        });

        Button addMoneyButton = findViewById(R.id.addMoneyButton);
        addMoneyButton.setOnClickListener(v -> {
            playSound();
            double amountToAdd = getAmountFromInput();
            if (amountToAdd > 0) {
                messageTextView.setText("");
                showLoading(true);
                addMoney(currentUserPhone, amountToAdd);
            } else {
                messageTextView.setText("Please enter a valid amount to add.");
            }
        });

        Button logoutButton = findViewById(R.id.LogoutButton);
        logoutButton.setOnClickListener(v -> {
            playSound();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(TransferActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
    private void playSound() {
        if (mediaPlayer != null) {
            mediaPlayer.start(); // Start playing the sound
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            progressOverlay.setVisibility(View.VISIBLE);
        } else {
            progressOverlay.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        transactionsAdapter = new TransactionsAdapter(transactionList, currentUserPhone);
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionsRecyclerView.setAdapter(transactionsAdapter);
        loadTransactions();
    }

    private void loadTransactions() {
        // Clear previous transactions
        transactionList.clear();

        // Create two queries: one for sent transactions and one for received transactions
        db.collection("transactions")
                .whereEqualTo("from", currentUserPhone)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        UserTransaction transaction = document.toObject(UserTransaction.class);
                        transactionList.add(transaction); // Add sent transactions
                    }
                    // After fetching sent transactions, load received transactions
                    loadReceivedTransactions();
                })
                .addOnFailureListener(e -> messageTextView.setText("Failed to load transactions."));
    }

    private void loadReceivedTransactions() {
        db.collection("transactions")
                .whereEqualTo("to", currentUserPhone) // Fetch received transactions
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        UserTransaction transaction = document.toObject(UserTransaction.class);
                        transactionList.add(transaction); // Add received transactions
                    }
                    // Notify adapter after loading both sent and received transactions
                    transactionsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> messageTextView.setText("Failed to load received transactions."));
    }


    private void transferAmount(String senderPhone, String recipientPhone, double amount) {
        DocumentReference senderRef = db.collection("users").document(senderPhone);
        DocumentReference recipientRef = db.collection("users").document(recipientPhone);

        db.runTransaction(transaction -> {
                    DocumentSnapshot senderSnapshot = transaction.get(senderRef);
                    DocumentSnapshot recipientSnapshot = transaction.get(recipientRef);

                    if (senderSnapshot.exists() && recipientSnapshot.exists()) {
                        double senderBalance = senderSnapshot.getDouble("availableAmount");
                        double recipientBalance = recipientSnapshot.getDouble("availableAmount");

                        if (senderBalance >= amount) {
                            transaction.update(senderRef, "availableAmount", senderBalance - amount);
                            transaction.update(recipientRef, "availableAmount", recipientBalance + amount);

                            // Store the transaction in Firestore
                            UserTransaction newTransaction = new UserTransaction(senderPhone, recipientPhone, amount);
                            db.collection("transactions").add(newTransaction); // Save transaction
                        } else {
                            try {
                                throw new Exception("Insufficient funds.");
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } else {
                        try {
                            throw new Exception("Transaction failed. User not found.");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return null;
                }).addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    updateUserInfo();
                    loadTransactions(); // Load transactions again
                    recipientPhoneEditText.setText("");
                    amountEditText.setText("");
                    // Success
                Toast.makeText(TransferActivity.this, "Transfer successful!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            showLoading(false);

            // failure message
            Toast.makeText(TransferActivity.this, "Transaction failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void addMoney(String phone, double amount) {
        DocumentReference userRef = db.collection("users").document(phone);

        db.runTransaction(transaction -> {
            DocumentSnapshot userSnapshot = transaction.get(userRef);
            if (userSnapshot.exists()) {
                double currentBalance = userSnapshot.getDouble("availableAmount");
                transaction.update(userRef, "availableAmount", currentBalance + amount);

                // Optionally, add this as a transaction
                UserTransaction newTransaction = new UserTransaction("System", phone, amount);
                db.collection("transactions").add(newTransaction);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            showLoading(false);
            updateUserInfo(); // Update the displayed balance
            loadTransactions(); // Load transactions again
            amountEditText.setText("");
            messageTextView.setText("Money added successfully.");
        }).addOnFailureListener(e -> {
            showLoading(false);
            messageTextView.setText("Failed to add money: " + e.getMessage());
        });
    }

    private double getAmountFromInput() {
        String amountString = amountEditText.getText().toString().trim();
        try {
            return Double.parseDouble(amountString);
        } catch (NumberFormatException e) {
            return 0; // Return 0 if parsing fails
        }
    }

    private void updateUserInfo() {
        db.collection("users").document(currentUserPhone)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        double balance = documentSnapshot.getDouble("availableAmount");
                        userInfoTextView.setText("Balance: ₹" + balance);
                    }
                }).addOnFailureListener(e -> userInfoTextView.setText("Failed to fetch user info."));
    }
}



