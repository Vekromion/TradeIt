package edu.uga.cs.tradeit;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ManagementActivity extends AppCompatActivity {
    private TextView textView;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_management);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Toolbar
        Toolbar toolbar = findViewById( R.id.toolbar2);
        setSupportActionBar( toolbar );

        textView = findViewById(R.id.textView3);
        auth = FirebaseAuth.getInstance();

        authListener = a -> {
            FirebaseUser u = a.getCurrentUser();
            textView.setText("User: " + (u != null ? u.getDisplayName() : "not signed in"));
            if (u == null) {
                new UserSignInDialogFragment().show(getSupportFragmentManager(), "SignIn");
            }
        };

        Button btnCat = findViewById(R.id.categories);
        Button btnTra = findViewById(R.id.transactions);

        btnCat.setText("Categories");
        btnTra.setText("Transactions");

        btnCat.setOnClickListener(v -> {
            Intent i = new Intent(this, CategoriesActivity.class);
            i.putExtra(CategoriesActivity.EXTRA_MODE, CategoriesActivity.MODE_CATEGORIES);
            startActivity(i);
        });

        btnTra.setOnClickListener(v -> {
            startActivity(new Intent(this, TransactionsActivity.class));
        });
    }

    // On Start
    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    // On Stop
    @Override
    protected void onStop() {
        super.onStop();
        if (authListener != null) auth.removeAuthStateListener(authListener);
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
        return super.onOptionsItemSelected(item);
    }

}