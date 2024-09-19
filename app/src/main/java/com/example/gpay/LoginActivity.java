package com.example.gpay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText phoneEditText;
    private Button registerButton, loginButton;
    private TextView messageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        phoneEditText = findViewById(R.id.phoneEditText);
        registerButton = findViewById(R.id.registerButton);
        loginButton = findViewById(R.id.loginButton);
        messageTextView = findViewById(R.id.messageTextView);

        createDummyAccounts();

        registerButton.setOnClickListener(v -> {
            String phone = phoneEditText.getText().toString();
            registerUser(phone);
        });

        loginButton.setOnClickListener(v -> {
            String phone = phoneEditText.getText().toString();
            loginUser(phone);
        });
    }

    private void registerUser(String phone) {
        db.collection("users").document(phone)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (!document.exists()) {
                            User user = new User(phone, 0); // Register with zero balance
                            db.collection("users").document(phone).set(user)
                                    .addOnSuccessListener(aVoid -> messageTextView.setText("User registered successfully."))
                                    .addOnFailureListener(e -> messageTextView.setText("Error registering user: " + e.getMessage()));
                        } else {
                            messageTextView.setText("User already exists.");
                        }
                    } else {
                        messageTextView.setText("Error checking user existence.");
                    }
                });
    }

    private void createDummyAccounts() {
        User user1 = new User("1234567890", 100.0); // User 1 with $100
        User user2 = new User("0987654321", 50.0); // User 2 with $50

        db.collection("users").document(user1.getPhoneNum()).set(user1)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Dummy user 1 created.");
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error creating dummy user 1: " + e.getMessage());
                });

        db.collection("users").document(user2.getPhoneNum()).set(user2)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Dummy user 2 created.");
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error creating dummy user 2: " + e.getMessage());
                });
    }


    private void loginUser(String phone) {
        db.collection("users").document(phone)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Intent intent = new Intent(LoginActivity.this, TransferActivity.class);
                            intent.putExtra("phoneNum", phone);
                            startActivity(intent);
                            finish(); // Close LoginActivity
                        } else {
                            messageTextView.setText("Login failed. User not found.");
                        }
                    } else {
                        messageTextView.setText("Error logging in. Try again.");
                    }
                });
    }
}
