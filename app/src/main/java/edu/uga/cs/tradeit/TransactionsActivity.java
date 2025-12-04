package edu.uga.cs.tradeit;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.uga.cs.tradeit.data.FirebaseRefs;
import edu.uga.cs.tradeit.models.Transaction;

/**
 * Displays transactions, split into pending and complete
 */
public class TransactionsActivity extends AppCompatActivity implements TransactionsAdapter.OnRowAction {

    // Tab index for pending and compelted
    public static final int MODE_PENDING = 0;
    public static final int MODE_COMPLETED = 1;

    private int mode;
    private RecyclerView recyclerView;
    private TransactionsAdapter adapter;
    private ValueEventListener pendingListener, completedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transactions);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);

        // Back and Home on Toolbar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Transactions");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mode = MODE_PENDING;

        adapter = new TransactionsAdapter(this, mode);
        recyclerView.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabLayout);

        // Set default tab
        tabLayout.selectTab(tabLayout.getTabAt(0));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            // Changes tab position
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mode = tab.getPosition();
                adapter.setMode(mode);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    // Connects Page to Firebase
    @Override
    protected void onStart() {
        super.onStart();

        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Load pending transactions
        pendingListener = FirebaseRefs.pendingByUser(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                List<String> txIds = new ArrayList<>();
                for (DataSnapshot child : snap.getChildren()) {
                    txIds.add(child.getKey());
                }
                loadTransactionDetails(txIds, true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                toast("Error loading pending transactions");
            }
        });

        // Load completed transactions
        completedListener = FirebaseRefs.completedByUser(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                List<String> txIds = new ArrayList<>();
                for (DataSnapshot child : snap.getChildren()) {
                    txIds.add(child.getKey());
                }
                loadTransactionDetails(txIds, false);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                toast("Error loading completed transactions");
            }
        });
    }
    // Given the ids, load teh details
    private void loadTransactionDetails(List<String> txIds, boolean isPending) {
        if (txIds.isEmpty()) {
            if (isPending) {
                adapter.submitPending(new ArrayList<>());
            } else {
                adapter.submitCompleted(new ArrayList<>());
            }
            return;
        }

        List<Transaction> tempList = new ArrayList<>();
        final int[] count = {0};

        for (String txId : txIds) {
            FirebaseRefs.transactions().child(txId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Transaction t = snapshot.getValue(Transaction.class);
                    if (t != null) {
                        tempList.add(t);
                    }
                    count[0]++;

                    if (count[0] == txIds.size()) {

                        // Sort by date, most recent first
                        if (isPending) {
                            adapter.submitPending(tempList);
                        } else {
                            adapter.submitCompleted(tempList);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    count[0]++;
                }
            });
        }
    }

    // Detaches firebase listener when no longer active
    @Override
    protected void onStop() {
        super.onStop();
        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        if (pendingListener != null) {
            FirebaseRefs.pendingByUser(currentUserId).removeEventListener(pendingListener);
        }
        if (completedListener != null) {
            FirebaseRefs.completedByUser(currentUserId).removeEventListener(completedListener);
        }
    }

    // Callback methods from TransactionsAdapter
    @Override
    public void onTransactionClicked(Transaction t) {
        // Only allow confirmation for pending transactions
        if (mode == MODE_PENDING) {
            showConfirmDialog(t);
        }
    }

    private void showConfirmDialog(Transaction t) {
        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // If buyer is null, waits
        if (t.buyerUserID == null) {
            toast("Waiting for a buyer to place a bid");
            return;
        }

        boolean isBuyer = currentUserId.equals(t.buyerUserID);
        boolean myConfirmed = isBuyer ? t.buyerConfirmed : t.sellerConfirmed;
        boolean otherConfirmed = isBuyer ? t.sellerConfirmed : t.buyerConfirmed;

        if (myConfirmed) {
            toast(otherConfirmed ? "Both parties have confirmed!" : "Waiting for other party to confirm");
            return;
        }

        String dialogMessage = isBuyer ?
                "Have you completed this exchange?" :
                "Accept this bid and confirm exchange?";

        new AlertDialog.Builder(this)
                .setTitle(isBuyer ? "Confirm Transaction" : "Accept Bid")
                .setMessage(dialogMessage)
                .setPositiveButton(isBuyer ? "Confirm" : "Accept", (dialog, which) -> confirmTransaction(t, isBuyer))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmTransaction(Transaction t, boolean isBuyer) {
        String field = isBuyer ? "buyerConfirmed" : "sellerConfirmed";

        FirebaseRefs.transactions().child(t.id).child(field).setValue(true)
                .addOnSuccessListener(aVoid -> {
                    // Check if both confirmed
                    FirebaseRefs.transactions().child(t.id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Transaction updated = snapshot.getValue(Transaction.class);
                            if (updated != null && updated.buyerConfirmed && updated.sellerConfirmed) {
                                // Mark as completed and att completion time
                                updated.status = "completed";
                                updated.completedAt = System.currentTimeMillis();

                                FirebaseRefs.transactions().child(t.id).setValue(updated)
                                        .addOnSuccessListener(aVoid2 -> {
                                            // Move to completed lists
                                            FirebaseRefs.completedByUser(updated.buyerUserID).child(t.id).setValue(true);
                                            FirebaseRefs.completedByUser(updated.sellerUserID).child(t.id).setValue(true);

                                            // Remove from pending
                                            FirebaseRefs.pendingByUser(updated.buyerUserID).child(t.id).removeValue();
                                            FirebaseRefs.pendingByUser(updated.sellerUserID).child(t.id).removeValue();

                                            // Remove item from category list
                                            FirebaseRefs.itemsByCategory(updated.categoryID).child(updated.itemId).removeValue()
                                                    .addOnSuccessListener(aVoid3 -> {
                                                        // Decrement category item count
                                                        FirebaseRefs.categories().child(updated.categoryID)
                                                                .child("itemCount")
                                                                .runTransaction(new com.google.firebase.database.Transaction.Handler() {
                                                                    @NonNull
                                                                    @Override
                                                                    public com.google.firebase.database.Transaction.Result doTransaction(@NonNull com.google.firebase.database.MutableData data) {
                                                                        Integer count = data.getValue(Integer.class);
                                                                        if (count != null && count > 0) {
                                                                            data.setValue(count - 1);
                                                                        }
                                                                        return com.google.firebase.database.Transaction.success(data);
                                                                    }

                                                                    @Override
                                                                    public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                                                                        if (committed) {
                                                                            toast("Transaction completed!");
                                                                        }
                                                                    }
                                                                });
                                                    });
                                        })
                                        .addOnFailureListener(e -> toast("Failed to complete: " + e.getMessage()));
                            } else {
                                toast("Confirmed! Waiting for other party.");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            toast("Error checking status");
                        }
                    });
                })
                .addOnFailureListener(e -> toast("Failed to confirm: " + e.getMessage()));
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_LONG).show();
    }

    // Runs when options menu is created
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_overflow, menu);
        return true;
    }

    // Runs when logout in options is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Sign out
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        // Home
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}