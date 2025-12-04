package edu.uga.cs.tradeit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Main Activity, holds the sign in and register buttons
 */
public class MainActivity extends AppCompatActivity
        implements UserSignInDialogFragment.SignInDialogListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Button btnSignIn = findViewById(R.id.signIn);
        Button btnRegister = findViewById(R.id.register);

        btnSignIn.setOnClickListener(v ->
                new UserSignInDialogFragment()
                        .show(getSupportFragmentManager(), "SignIn"));

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    @Override
    public void signIn(String email, String password) {
        startActivity(new Intent(this, ManagementActivity.class));
        finish();
    }
}