package com.example.flexifitapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etName, etAddress, etEmail, etUsername, etPassword;
    private TextInputLayout tilPassword;
    private MaterialButton signupButton;
    private TextView loginRedirectText;

    private static final String PREF_NAME = "flexifit_prefs";
    private static final String KEY_DARK_MODE = "dark_mode";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 🌙 Load theme
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(KEY_DARK_MODE, false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Views
        etName     = findViewById(R.id.signup_name);
        etAddress  = findViewById(R.id.signup_address);
        etEmail    = findViewById(R.id.signup_email);
        etUsername = findViewById(R.id.signup_username);
        etPassword = findViewById(R.id.signup_password);
        tilPassword = findViewById(R.id.tilpass);

        signupButton      = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        signupButton.setOnClickListener(v -> {
            if (!validateName() | !validateAddress() | !validateEmail()
                    | !validateUsername() | !validatePassword()) {
                return;
            }
            checkIfUsernameExists();
        });

        loginRedirectText.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    // ------------------------------------------------------------
    // 🔵 0. CHECK USERNAME DUPLICATION BEFORE SIGNUP
    // ------------------------------------------------------------
    private void checkIfUsernameExists() {
        String username = safeText(etUsername);

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users");

        Query checkUser = ref.orderByChild("username").equalTo(username);

        signupButton.setEnabled(false);

        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    signupButton.setEnabled(true);
                    etUsername.setError("Username already taken");
                    etUsername.requestFocus();
                    return;
                }

                // WALA pang gumagamit ng username → proceed with account creation
                createAccount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                signupButton.setEnabled(true);
                Toast.makeText(SignupActivity.this,
                        "Database error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ------------------------------------------------------------
    // 🔵 1. CREATE ACCOUNT WITH FIREBASE AUTH
    // ------------------------------------------------------------
    private void createAccount() {
        String name     = safeText(etName);
        String address  = safeText(etAddress);
        String email    = safeText(etEmail);
        String username = safeText(etUsername);
        String password = safeText(etPassword);

        // 1️⃣ Create Firebase Auth account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    signupButton.setEnabled(true);

                    if (!task.isSuccessful()) {
                        Toast.makeText(
                                SignupActivity.this,
                                "Sign up failed: " +
                                        (task.getException() != null ? task.getException().getMessage() : ""),
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        Toast.makeText(SignupActivity.this,
                                "User not available. Try again.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 2️⃣ Send email verification WITH listener
                    user.sendEmailVerification()
                            .addOnCompleteListener(verifyTask -> {

                                if (!verifyTask.isSuccessful()) {
                                    // ❌ Hindi na-send ang email
                                    Toast.makeText(
                                            SignupActivity.this,
                                            "Failed to send verification email: " +
                                                    (verifyTask.getException() != null
                                                            ? verifyTask.getException().getMessage()
                                                            : ""),
                                            Toast.LENGTH_LONG
                                    ).show();
                                    signupButton.setEnabled(true);
                                    return; // huwag ituloy pag wala talagang email na na-send
                                }

                                // ✅ Umabot dito = verification email SENT successfully
                                String uid = user.getUid();

                                // 3️⃣ Save profile sa /users/{uid}
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("uid", uid);
                                userData.put("name", name);
                                userData.put("address", address);
                                userData.put("email", email);
                                userData.put("username", username);
                                userData.put("surveyCompleted", false);
                                userData.put("createdAt", System.currentTimeMillis());

                                FirebaseDatabase.getInstance()
                                        .getReference("users")
                                        .child(uid)
                                        .setValue(userData)
                                        .addOnCompleteListener(saveTask -> {
                                            if (saveTask.isSuccessful()) {
                                                Toast.makeText(
                                                        SignupActivity.this,
                                                        "Account created! Verification link sent to your email.",
                                                        Toast.LENGTH_LONG
                                                ).show();

                                                Intent intent = new Intent(SignupActivity.this, EmailVerificationActivity.class);
                                                intent.putExtra("email", email);
                                                startActivity(intent);
                                                finish();

                                            } else {
                                                Toast.makeText(SignupActivity.this,
                                                        "Failed to save profile. Try again.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            });
                });
    }

    // ------------------------------------------------------------
    // VALIDATIONS
    // ------------------------------------------------------------

    private boolean validateName() {
        String val = safeText(etName);
        if (val.isEmpty()) {
            etName.setError("Name cannot be empty");
            return false;
        } else if (val.length() < 2) {
            etName.setError("Name is too short");
            return false;
        } else {
            etName.setError(null);
            return true;
        }
    }

    private boolean validateAddress() {
        String val = safeText(etAddress);
        if (val.isEmpty()) {
            etAddress.setError("Address cannot be empty");
            return false;
        } else {
            etAddress.setError(null);
            return true;
        }
    }

    private boolean validateEmail() {
        String val = safeText(etEmail);
        if (val.isEmpty()) {
            etEmail.setError("Email cannot be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(val).matches()) {
            etEmail.setError("Invalid email format");
            return false;
        } else {
            etEmail.setError(null);
            return true;
        }
    }

    private boolean validateUsername() {
        String val = safeText(etUsername);
        if (val.isEmpty()) {
            etUsername.setError("Username cannot be empty");
            return false;
        } else if (val.contains(" ")) {
            etUsername.setError("Username cannot have spaces");
            return false;
        } else if (val.length() < 2) {
            etUsername.setError("Username must be at least 2 characters");
            return false;
        } else {
            etUsername.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String val = safeText(etPassword);
        if (val.isEmpty()) {
            tilPassword.setError("Password cannot be empty");
            return false;
        } else if (val.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            return false;
        } else {
            tilPassword.setError(null);
            return true;
        }
    }

    private String safeText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    // 🌙 Theme menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_theme_switcher, menu);

        MenuItem item = menu.findItem(R.id.action_toggle_theme);
        boolean isDark = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .getBoolean(KEY_DARK_MODE, false);

        item.setIcon(ContextCompat.getDrawable(
                this,
                isDark ? R.drawable.ic_sun : R.drawable.ic_moon
        ));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_theme) {
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            boolean current = prefs.getBoolean(KEY_DARK_MODE, false);
            boolean newMode = !current;

            prefs.edit().putBoolean(KEY_DARK_MODE, newMode).apply();

            AppCompatDelegate.setDefaultNightMode(
                    newMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );

            recreate();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
