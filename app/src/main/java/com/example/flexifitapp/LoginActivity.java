package com.example.flexifitapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    // UI
    private EditText loginUsername, loginPassword;
    private Button loginButton;
    private TextView signupRedirectText;

    // Theme prefs
    private static final String PREF_NAME = "flexifit_prefs";
    private static final String KEY_DARK_MODE = "dark_mode";

    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 🌙 Theme
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isDark = sharedPreferences.getBoolean(KEY_DARK_MODE, false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        // 🔁 AUTO LOGIN kung may current user na verified na
        FirebaseUser current = mAuth.getCurrentUser();
        if (current != null && current.isEmailVerified()) {
            checkSurveyStatus(current);
            return;
        }

        setupUi();
    }

    private void setupUi() {
        setContentView(R.layout.activity_login);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        loginUsername = findViewById(R.id.login_username);
        loginPassword = findViewById(R.id.login_password);
        loginButton   = findViewById(R.id.login_button);
        signupRedirectText = findViewById(R.id.signupRedirectText);

        loginButton.setOnClickListener(v -> {
            if (!validateUsername() || !validatePassword()) return;
            findEmailAndLogin();
        });

        signupRedirectText.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });
    }

    // ------------------ VALIDATION ------------------

    private boolean validateUsername() {
        String val = loginUsername.getText().toString().trim();
        if (val.isEmpty()) {
            loginUsername.setError("Username cannot be empty");
            return false;
        }
        loginUsername.setError(null);
        return true;
    }

    private boolean validatePassword() {
        String val = loginPassword.getText().toString().trim();
        if (val.isEmpty()) {
            loginPassword.setError("Password cannot be empty");
            return false;
        }
        loginPassword.setError(null);
        return true;
    }

    // ------------------ STEP 1: username → email ------------------

    private void findEmailAndLogin() {
        String username = loginUsername.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users");

        Query query = ref.orderByChild("username").equalTo(username);

        loginButton.setEnabled(false);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    loginButton.setEnabled(true);
                    loginUsername.setError("Username not found");
                    loginUsername.requestFocus();
                    return;
                }

                // may at least 1 user na may ganyang username
                DataSnapshot userSnap = snapshot.getChildren().iterator().next();

                String email = userSnap.child("email").getValue(String.class);

                if (email == null || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    loginButton.setEnabled(true);
                    Toast.makeText(LoginActivity.this,
                            "Invalid email associated with this user.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                signInWithEmail(email, password);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loginButton.setEnabled(true);
                Toast.makeText(LoginActivity.this,
                        "Database error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ------------------ STEP 2: FirebaseAuth email login ------------------

    private void signInWithEmail(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    loginButton.setEnabled(true);

                    if (!task.isSuccessful()) {
                        loginPassword.setError("Incorrect password");
                        loginPassword.requestFocus();
                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        Toast.makeText(this,
                                "Auth error. Try again.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!user.isEmailVerified()) {
                        Toast.makeText(this,
                                "Please verify your email first.",
                                Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        return;
                    }

                    checkSurveyStatus(user);
                });
    }

    // ------------------ STEP 3: surveyCompleted check ------------------

    private void checkSurveyStatus(FirebaseUser user) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid());

        ref.child("surveyCompleted")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        Boolean value = snap.getValue(Boolean.class);
                        boolean done = value != null && value;

                        if (done) {
                            goToMain();
                        } else {
                            goToSurvey();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(LoginActivity.this,
                                "Database error: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        setupUi();
                    }
                });
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToSurvey() {
        Intent intent = new Intent(this, SurveyStep1Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ------------------ THEME MENU ------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_theme_switcher, menu);

        MenuItem item = menu.findItem(R.id.action_toggle_theme);
        boolean isDark = sharedPreferences.getBoolean(KEY_DARK_MODE, false);

        item.setIcon(ContextCompat.getDrawable(
                this,
                isDark ? R.drawable.ic_sun : R.drawable.ic_moon
        ));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_theme) {
            boolean isDark = sharedPreferences.getBoolean(KEY_DARK_MODE, false);
            boolean newDark = !isDark;

            sharedPreferences.edit().putBoolean(KEY_DARK_MODE, newDark).apply();

            AppCompatDelegate.setDefaultNightMode(
                    newDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );

            recreate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
