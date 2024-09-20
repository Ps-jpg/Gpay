package com.example.gpay;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer; // Import MediaPlayer
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText phoneEditText;
    private Button registerButton, loginButton;
    private TextView messageTextView;
    private FrameLayout progressOverlay;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_login);

        ImageView githubIcon = findViewById(R.id.githubIcon);
        // Set OnClickListener to open GitHub profile
        githubIcon.setOnClickListener(v -> {
            String githubUrl = "https://ps-jpg.github.io/Gpay/";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(githubUrl));
            startActivity(intent);
        });


db = FirebaseFirestore.getInstance();

        phoneEditText = findViewById(R.id.phoneEditText);
        registerButton = findViewById(R.id.registerButton);
        loginButton = findViewById(R.id.loginButton);
        messageTextView = findViewById(R.id.messageTextView);
        progressOverlay = findViewById(R.id.progressOverlay);

        mediaPlayer = MediaPlayer.create(this, R.raw.button_click);

        createDummyAccounts();

        registerButton.setOnClickListener(v -> {
            playSound(); // Play sound on button click
            String phone = phoneEditText.getText().toString().trim();
            if (phone.isEmpty()) {
                messageTextView.setText("Please enter phone number");
            } else {
                messageTextView.setText("");
                showLoading(true);
                registerUser(phone);
            }
        });

        loginButton.setOnClickListener(v -> {
            playSound(); // Play sound on button click
            String phone = phoneEditText.getText().toString().trim();
            if (phone.isEmpty()) {
                messageTextView.setText("Please enter phone number");
            } else {
                messageTextView.setText("");
                showLoading(true);
                loginUser(phone);
            }
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

    private void registerUser(String phone) {
        db.collection("users").document(phone)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (!document.exists()) {
                            User user = new User(phone, 0); // Register with zero balance
                            db.collection("users").document(phone).set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        showLoading(false);
                                        messageTextView.setText("User registered successfully.");
                                        phoneEditText.setText("");
                                    })
                                    .addOnFailureListener(e -> {
                                        showLoading(false);
                                        messageTextView.setText("Error registering user: " + e.getMessage());
                                    });
                        } else {
                            showLoading(false);
                            messageTextView.setText("User already exists.");
                        }
                    } else {
                        showLoading(false);
                        messageTextView.setText("Error checking user existence.");
                    }
                });
    }

    private void createDummyAccounts() {
        User user1 = new User("1234567890", 100.0); // User 1 with $100
        User user2 = new User("0987654321", 50.0); // User 2 with $50

        db.collection("users").document(user1.getPhoneNum()).set(user1)
                .addOnSuccessListener(aVoid -> System.out.println("Dummy user 1 created."))
                .addOnFailureListener(e -> System.out.println("Error creating dummy user 1: " + e.getMessage()));

        db.collection("users").document(user2.getPhoneNum()).set(user2)
                .addOnSuccessListener(aVoid -> System.out.println("Dummy user 2 created."))
                .addOnFailureListener(e -> System.out.println("Error creating dummy user 2: " + e.getMessage()));
    }

    private void loginUser(String phone) {
        db.collection("users").document(phone)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            showLoading(false);
                            Intent intent = new Intent(LoginActivity.this, TransferActivity.class);
                            intent.putExtra("phoneNum", phone);
                            startActivity(intent);
                            finish(); // Close LoginActivity
                        } else {
                            showLoading(false);
                            messageTextView.setText("Login failed. User not found.");
                        }
                    } else {
                        showLoading(false);
                        messageTextView.setText("Error logging in. Try again.");
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release(); // Release  resources
            mediaPlayer = null; // To  avoid memory leaks
        }
    }
}
