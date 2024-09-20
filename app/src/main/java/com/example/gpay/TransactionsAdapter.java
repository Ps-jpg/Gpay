package com.example.gpay;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import java.util.List;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.ViewHolder> {

    private List<UserTransaction> transactions;
    private String currentUserPhone;
    private FirebaseFirestore db;

    public TransactionsAdapter(List<UserTransaction> transactions, String currentUserPhone) {
        this.transactions = transactions;
        this.currentUserPhone = currentUserPhone;
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public TransactionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TransactionsAdapter.ViewHolder holder, int position) {
        UserTransaction transaction = transactions.get(position);

        // Check if money was sent
        if (transaction.getFrom().equals(currentUserPhone)) {
            holder.fromTextView.setText("From: " + transaction.getFrom());
            holder.transactionPartyTextView.setText("To: " + transaction.getTo());
            holder.amountTextView.setText(" ₹" + transaction.getAmount());
            holder.amountTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            holder.transactionIcon.setImageResource(R.drawable.ic_money_sent);
        }
        // Check if money was received
        else if (transaction.getTo().equals(currentUserPhone)) {
            holder.fromTextView.setText("From: " + transaction.getFrom());
            holder.transactionPartyTextView.setText("To: " + transaction.getTo());
            holder.amountTextView.setText("+ ₹" + transaction.getAmount());
            holder.amountTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            holder.transactionIcon.setImageResource(R.drawable.ic_money_received);
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView fromTextView; // Ensure this is declared
        public TextView transactionPartyTextView;
        public TextView amountTextView;
        public ImageView transactionIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            fromTextView = itemView.findViewById(R.id.fromTextView); // Initialize correctly
            transactionPartyTextView = itemView.findViewById(R.id.transactionPartyTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            transactionIcon = itemView.findViewById(R.id.transactionIcon);
        }
    }
}
