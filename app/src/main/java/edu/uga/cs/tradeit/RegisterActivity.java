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
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Activity that allows for new users to register
 */
public class RegisterActivity extends AppCompatActivity {

    private TextView textView;
    private EditText emailView;
    private EditText usernameView;
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
        usernameView = findViewById(R.id.editTextText3);
        passwordView = findViewById(R.id.editTextTextPassword2);
        registerButton = findViewById(R.id.button2);
        textView = findViewById(R.id.textView2);
        firebaseAuth = FirebaseAuth.getInstance();
        registerButton.setOnClickListener(new RegisterButtonClickListener());

        textView.setText("Register");
    }
    // Deals with when the register button is clicked
    private class RegisterButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            final String email = emailView.getText().toString().trim();
            final String password = passwordView.getText().toString();
            final String displayName = usernameView.getText().toString().trim();

            boolean ok = true;
            // Requires displayname / username
            if (TextUtils.isEmpty(displayName)) {
                usernameView.setError("Display name required");
                ok = false;
            }
            // Validates correct email format
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailView.setError("Enter a valid email");
                ok = false;
            }
            // Requires a password
            if (TextUtils.isEmpty(password)) {
                passwordView.setError("Password required");
                ok = false;
            }
            if (!ok) {
                return;
            }
            // firebase user creation
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(RegisterActivity.this, (Task<AuthResult> task) -> {
                        if (task.isSuccessful()) {
                            Log.d("Register", "createUserWithEmail: Success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            Toast.makeText(getApplicationContext(),
                                    "Registered user: " + email, Toast.LENGTH_SHORT).show();
                            // Sets Display Name
                            if (user != null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(displayName).build();
                                user.updateProfile(profileUpdates).addOnCompleteListener(t2 -> {
                                    if (t2.isSuccessful()) {
                                        Log.d("Register", "Display Successful");
                                    }
                                });
                            }
                            Toast.makeText(getApplicationContext(), "Registered user: " + email, Toast.LENGTH_SHORT).show();

                            // Moves to main screen
                            startActivity(new Intent(RegisterActivity.this, ManagementActivity.class));
                            finish();
                        } else {
                            Log.w("Register", "createUserWithEmail: failure", task.getException());
                            Exception e = task.getException();
                            // Error checkers for register use story
                            if (e instanceof FirebaseAuthUserCollisionException) {
                                emailView.setError("Email already in use");
                                Toast.makeText(RegisterActivity.this,
                                        "Email already in use",
                                        Toast.LENGTH_LONG).show();
                            } else if (e instanceof FirebaseNetworkException) {
                                Toast.makeText(RegisterActivity.this,
                                        "Network error. Please try again",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(RegisterActivity.this,
                                        "Registration failed",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }
}