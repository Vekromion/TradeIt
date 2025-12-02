package edu.uga.cs.tradeit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import edu.uga.cs.tradeit.models.Transaction;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.VH> {

    public interface OnRowAction {
        void onTransactionClicked(Transaction t);
    }

    private final Context context;
    private int mode;
    private final OnRowAction OR;

    private List<Transaction> pendingList = new ArrayList<>();
    private List<Transaction> completedList = new ArrayList<>();

    public TransactionsAdapter(Context context, int mode) {
        this.context = context;
        this.mode = mode;
        this.OR = (OnRowAction) context;
    }

    public void submitPending(List<Transaction> list) {
        pendingList = list;
        if (mode == TransactionsActivity.MODE_PENDING) {
            notifyDataSetChanged();
        }
    }

    public void submitCompleted(List<Transaction> list) {
        completedList = list;
        if (mode == TransactionsActivity.MODE_COMPLETED) {
            notifyDataSetChanged();
        }
    }

    public void setMode(int mode) {
        this.mode = mode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler, parent, false);
        return new VH(row);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionsAdapter.VH holder, int position) {
        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        if (mode == TransactionsActivity.MODE_PENDING) {
            Transaction t = pendingList.get(position);
            boolean isBuyer = currentUserId.equals(t.buyerUserID);
            boolean isSeller = currentUserId.equals(t.sellerUserID);

            // CHANGE 6: Handle case where buyer hasn't bid yet
            String otherParty;
            if (t.buyerUserID == null) {
                otherParty = "No bids yet";
            } else {
                otherParty = isBuyer ? t.sellerName : t.buyerName;
            }

            holder.title.setText(t.itemName);

            String priceStr = t.itemIsFree ? "FREE" :
                    String.format(Locale.US, "$%.2f", t.itemPrice / 100.0);
            holder.line2.setText(priceStr);

            // CHANGE 6: Show appropriate role message
            String role;
            if (t.buyerUserID == null) {
                role = "Waiting for buyer: ";
            } else {
                role = isBuyer ? "Buying from: " : "Selling to: ";
            }
            holder.line3.setText(role + otherParty);

            boolean myConfirmed = isBuyer ? t.buyerConfirmed : t.sellerConfirmed;
            boolean otherConfirmed = isBuyer ? t.sellerConfirmed : t.buyerConfirmed;

            String status;
            if (t.buyerUserID == null) {
                status = "Waiting for a buyer to bid";
            } else if (myConfirmed && otherConfirmed) {
                status = "Both confirmed - completing...";
            } else if (myConfirmed) {
                status = "You confirmed - waiting for other party";
            } else if (otherConfirmed) {
                status = isSeller ? "Buyer bid placed - tap to accept" : "Seller waiting - you already bid";
            } else {
                status = isSeller ? "Waiting for buyer bid" : "Tap to place bid";
            }
            holder.line4.setText(status);

            holder.itemView.setOnClickListener(v -> OR.onTransactionClicked(t));
        } else {
            Transaction t = completedList.get(position);
            boolean isBuyer = currentUserId.equals(t.buyerUserID);
            String otherParty = isBuyer ? t.sellerName : t.buyerName;

            holder.title.setText(t.itemName);

            String priceStr = t.itemIsFree ? "FREE" :
                    String.format(Locale.US, "$%.2f", t.itemPrice / 100.0);
            holder.line2.setText(priceStr);

            String role = isBuyer ? "Bought from: " : "Sold to: ";
            holder.line3.setText(role + otherParty);

            String dateStr = DateFormat.getDateTimeInstance().format(new Date(t.completedAt));
            holder.line4.setText("Completed on " + dateStr);

            holder.itemView.setOnClickListener(null); // No click for completed
        }
    }

    @Override
    public int getItemCount() {
        return mode == TransactionsActivity.MODE_PENDING ? pendingList.size() : completedList.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, line2, line3, line4;
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.textViewTitle);
            line2 = v.findViewById(R.id.textLine2);
            line3 = v.findViewById(R.id.textLine3);
            line4 = v.findViewById(R.id.textLine4);
        }
    }
}