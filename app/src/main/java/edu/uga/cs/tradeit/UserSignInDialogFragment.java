package edu.uga.cs.tradeit;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class UserSignInDialogFragment extends DialogFragment {
    private EditText emailView;
    private EditText passwordView;

    public UserSignInDialogFragment() { /* required empty ctor */ }


    public interface SignInDialogListener {
        void signIn( String email, String password );
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v1 = LayoutInflater.from(getContext()).inflate(R.layout.signin_dialog, null);
        emailView = v1.findViewById(R.id.editTextText);
        passwordView = v1.findViewById(R.id.editTextTextPassword);

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
                .setTitle("Sign In")
                .setView(v1)
                .setNegativeButton(android.R.string.cancel, (d, w) -> d.dismiss())
                .setPositiveButton(android.R.string.ok, null)
                .create();
        dialog.setOnShowListener(d -> {
            int accent = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.teal_700);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(accent);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(accent);

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Sign In");

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String email = emailView.getText().toString().trim();
                String pass = passwordView.getText().toString();

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailView.setError("Invalid email");
                    return;
                }
                if (TextUtils.isEmpty(pass)) {
                    passwordView.setError("Password required");
                    return;
                }
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                com.google.firebase.auth.FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(email, pass)
                        .addOnSuccessListener(r -> {
                            if (getActivity() instanceof UserSignInDialogFragment.SignInDialogListener) {
                                ((UserSignInDialogFragment.SignInDialogListener) getActivity()).signIn(email, pass);
                            }
                            dismiss();
                        })
                        .addOnFailureListener(e -> {
                            if (e instanceof com.google.firebase.FirebaseNetworkException) {
                                passwordView.setError("Network error. Check your internet connection and try again.");
                            } else {
                                passwordView.setError("Sign-in failed: " + e.getMessage());
                            }
                            passwordView.requestFocus();
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        });
            });
        });
        return dialog;
    }
}