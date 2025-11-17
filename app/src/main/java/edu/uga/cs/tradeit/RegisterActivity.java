package edu.uga.cs.tradeit;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private TextView textView;
    private EditText emailView;
    private EditText passwordView;
    private Button registerButton;

    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        emailView = findViewById(R.id.editTextText2);
        passwordView = findViewById(R.id.editTextTextPassword2);
        registerButton = findViewById(R.id.button2);
        textView = findViewById(R.id.textView2);
        firebaseAuth = FirebaseAuth.getInstance();
        registerButton.setOnClickListener(new RegisterButtonClickListener());

        textView.setText("Register");
    }
    private class RegisterButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            final String email = emailView.getText().toString().trim();
            final String password = passwordView.getText().toString();

            boolean ok = true;
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailView.setError("Enter a valid email");
                ok = false;
            }
            if (TextUtils.isEmpty(password)) {
                passwordView.setError("Password required");
                ok = false;
            }
            if (!ok) {
                return;
            }
            setBusy(true);

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(RegisterActivity.this, (Task<AuthResult> task) -> {
                        if (task.isSuccessful()) {
                            Log.d("Register", "createUserWithEmail: Success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            Toast.makeText(getApplicationContext(),
                                    "Registered user: " + email, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, ManagementActivity.class));
                            finish();
                        } else {
                            Log.w("Register", "createUserWithEmail: failure", task.getException());
                            Exception e = task.getException();
                            if (e instanceof FirebaseAuthUserCollisionException) {
                                emailView.setError("Email already in use");
                                Toast.makeText(getApplicationContext(), "Email already registered", Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (e instanceof FirebaseNetworkException) {
                                Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_LONG).show();
                                return;
                            }
                            Toast.makeText(getApplicationContext(), "Registration failed: ", Toast.LENGTH_LONG).show();
                        }
                    });
        }

        private void setBusy(boolean b) {
            registerButton.setEnabled(!b);
            registerButton.setText(b ? "Registering…" : "Register");
            emailView.setEnabled(!b);
            passwordView.setEnabled(!b);
        }
    }
}