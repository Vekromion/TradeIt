package edu.uga.cs.tradeit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import edu.uga.cs.tradeit.data.FirebaseRefs;
import edu.uga.cs.tradeit.models.Transaction;

public class TransactionsActivity extends AppCompatActivity {
    public RecyclerView rcView;
    //public TransactionsAdapter adapter;
    private ValueEventListener pListener, cListener;
    private final List<Transaction> pendingList = new ArrayList<>();
    private final List<Transaction> completedList = new ArrayList<>();
    private boolean showingPending = true;
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

        getSupportActionBar().setTitle("Transactions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TabLayout tabs = findViewById(R.id.tabLayout);
        rcView = findViewById(R.id.recyclerView);
        rcView.setLayoutManager(new LinearLayoutManager(this));

        //adapter = new TransactionsAdapter();
        //rcView.setAdapter(adapter);

        showingPending = true;

        // Set Default tab
        //adapter.submit(pendingList, true);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (showingPending) {
                    //adapter.submit(pendingList, true);
                } else {
                    //adapter.submit(completedList, true);
                }

            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    @Override
    //protected void onStart() {

    //}

    protected void onStop() {
        super.onStop();
        String uID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        if (pListener != null) {
            FirebaseRefs.pendingByUser(uID).removeEventListener(pListener);
        }
        if (cListener != null) {
            FirebaseRefs.completedByUser(uID).removeEventListener(cListener);
        }
    }

    // class TransactionsAdapter extends RecyclerView.Adapter<TxVH> {
    // }

    private void confirmComplete(Transaction t) {

    }

    static class TxVH extends RecyclerView.ViewHolder {
        TextView title, line2, line3, line4;
        TxVH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.textViewTitle);
            line2 = v.findViewById(R.id.textLine2);
            line3 = v.findViewById(R.id.textLine3);
            line4 = v.findViewById(R.id.textLine4);
        }
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
        if (item.getItemId()==R.id.action_logout) {
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