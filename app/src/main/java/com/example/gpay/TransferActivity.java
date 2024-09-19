package com.example.gpay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TransferActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String currentUserPhone;

    private EditText recipientPhoneEditText, amountEditText;
    private TextView userInfoTextView, welcomeMessageTextView;
    private RecyclerView transactionsRecyclerView;
    private TransactionsAdapter transactionsAdapter;
    private List<UserTransaction> transactionList = new ArrayList<>();

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

        welcomeMessageTextView.setText("Welcome, " + currentUserPhone + "!");
        updateUserInfo();
        setupRecyclerView();

        Button transferButton = findViewById(R.id.transferButton);
        transferButton.setOnClickListener(v -> {
            String recipientPhone = recipientPhoneEditText.getText().toString();
            double amount = Double.parseDouble(amountEditText.getText().toString());
            transferAmount(currentUserPhone, recipientPhone, amount);
        });

        Button addMoneyButton = findViewById(R.id.addMoneyButton);
        addMoneyButton.setOnClickListener(v -> {
            double amountToAdd = Double.parseDouble(amountEditText.getText().toString());
            addMoney(currentUserPhone, amountToAdd);
        });

        Button logoutButton = findViewById(R.id.LogoutButton);
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(TransferActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupRecyclerView() {
        transactionsAdapter = new TransactionsAdapter(transactionList);
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionsRecyclerView.setAdapter(transactionsAdapter);
        loadTransactions();
    }

    private void loadTransactions() {
        db.collection("transactions")
                .whereEqualTo("from", currentUserPhone)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    transactionList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        UserTransaction transaction = document.toObject(UserTransaction.class);
                        transactionList.add(transaction);
                    }
                    transactionsAdapter.notifyDataSetChanged();
                });
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
                            userInfoTextView.setText("Insufficient funds.");
                        }
                    } else {
                        userInfoTextView.setText("Transaction failed. Recipient not found.");
                    }
                    return null;
                }).addOnSuccessListener(aVoid -> {
                    updateUserInfo();
                    loadTransactions(); // Load transactions again
                })
                .addOnFailureListener(e -> userInfoTextView.setText("Transaction failed."));
    }

    private void addMoney(String phone, double amount) {
        DocumentReference userRef = db.collection("users").document(phone);

        db.runTransaction(transaction -> {
            DocumentSnapshot userSnapshot = transaction.get(userRef);
            if (userSnapshot.exists()) {
                double currentBalance = userSnapshot.getDouble("availableAmount");
                transaction.update(userRef, "availableAmount", currentBalance + amount);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            updateUserInfo(); // Update the displayed balance
            loadTransactions(); // Optionally load transactions again
        }).addOnFailureListener(e -> userInfoTextView.setText("Failed to add money."));
    }

    private void updateUserInfo() {
        db.collection("users").document(currentUserPhone)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        double balance = documentSnapshot.getDouble("availableAmount");
                        userInfoTextView.setText("Balance: $" + balance);
                    }
                });
    }
}
