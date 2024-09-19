//package com.example.gpay;
//
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import androidx.appcompat.app.AppCompatActivity;
//
//public class MainActivity extends AppCompatActivity {
//
//    private PaymentSystem paymentSystem;
//    private User currentUser;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        paymentSystem = new PaymentSystem();
//
//        // Register users for testing
//        paymentSystem.registerUser("1234567890", 100.00);
//        paymentSystem.registerUser("0987654321", 50.00);
//
//        EditText phoneEditText = findViewById(R.id.phoneEditText);
//        EditText amountEditText = findViewById(R.id.amountEditText);
//
//        Button registerButton = findViewById(R.id.registerButton);
//        Button loginButton = findViewById(R.id.loginButton);
//        Button transferButton = findViewById(R.id.transferButton);
//
//        TextView userInfoTextView = findViewById(R.id.userInfoTextView);
//
//        registerButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String phone = phoneEditText.getText().toString();
//                paymentSystem.registerUser(phone, 0); // Register with zero balance for testing
//                phoneEditText.setText("");
//            }
//        });
//
//        loginButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String phone = phoneEditText.getText().toString();
//                currentUser = paymentSystem.login(phone);
//
//                if (currentUser != null) {
//                    userInfoTextView.setText("Logged in as: " + currentUser.getPhoneNum() +
//                            "\nBalance: $" + currentUser.getAvailableAmount());
//                } else {
//                    userInfoTextView.setText("Login failed. User not found.");
//                }
//
//                phoneEditText.setText("");
//            }
//        });
//
//        transferButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (currentUser != null) {
//                    String recipientPhone = phoneEditText.getText().toString();
//                    double amount = Double.parseDouble(amountEditText.getText().toString());
//
//                    paymentSystem.transferAmount(currentUser.getPhoneNum(), recipientPhone, amount);
//
//                    // Update displayed info after transfer
//                    userInfoTextView.setText("Logged in as: " + currentUser.getPhoneNum() +
//                            "\nBalance: $" + currentUser.getAvailableAmount());
//
//                    amountEditText.setText("");
//                    phoneEditText.setText("");
//                } else {
//                    userInfoTextView.setText("Please log in first.");
//                }
//            }
//        });
//    }
//}
