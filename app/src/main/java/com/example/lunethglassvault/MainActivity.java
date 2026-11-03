package com.example.lunethglassvault;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private CheckBox rememberme;
    private Button signinbtn;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // auto check login for saved prefs
        SharedPreferences sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        if (sharedPref.getBoolean("REMEMBER_ME", false) && sharedPref.getBoolean("IS_LOGGED_IN", false)) {
            startActivity(new Intent(MainActivity.this, QuickShiftHubActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        realm = Realm.getDefaultInstance();

        prePopulateSampleData();

        //
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        rememberme = findViewById(R.id.rememberme);
        signinbtn = findViewById(R.id.signinbtn);

        // prefilled
        if (sharedPref.getBoolean("REMEMBER_ME", false)) {
            etUsername.setText(sharedPref.getString("SAVED_USERNAME", ""));
            rememberme.setChecked(true);
        }

        // Actions
        signinbtn.setOnClickListener(v -> performSignIn());
    }

    private void performSignIn() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both Username and Password", Toast.LENGTH_SHORT).show();
            return;
        }

        // admin login details, admin is undeletable
        if (username.equalsIgnoreCase("admin") && password.equals("admin123")) {
            Toast.makeText(this, "Welcome, System Admin!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, AdminActivity.class));
            return;
        }

        // user check
        User user = realm.where(User.class)
                .equalTo("username", username)
                .equalTo("password", password)
                .findFirst();

        if (user != null) {
            SharedPreferences sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();

            editor.putString("USER_UUID", user.getUuid());
            editor.putString("USER_NAME", user.getFullName() != null ? user.getFullName() : user.getUsername());
            editor.putBoolean("IS_LOGGED_IN", true);

            boolean shouldRemember = rememberme.isChecked();
            editor.putBoolean("REMEMBER_ME", shouldRemember);
            if (shouldRemember) {
                editor.putString("SAVED_USERNAME", username);
            } else {
                editor.remove("SAVED_USERNAME");
            }
            editor.apply();

            Toast.makeText(this, "Welcome, " + username + "!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, QuickShiftHubActivity.class));
            finish();

        } else {
            // Reject unknown/unregistered credentials
            Toast.makeText(this, "Invalid credentials! Please contact System Admin.", Toast.LENGTH_LONG).show();
        }
    }

    private void prePopulateSampleData() {
        if (realm.where(FacturaItem.class).count() == 0) {
            realm.executeTransaction(r -> {
                r.insertOrUpdate(new FacturaItem("GLS-001", "FAC-2026-01", "RCR Crystal", "Crystal Wine Glass 350ml", 350.00, "", 45));
                r.insertOrUpdate(new FacturaItem("GLS-002", "FAC-2026-01", "RCR Crystal", "Champagne Flute 200ml", 420.00, "", 30));
                r.insertOrUpdate(new FacturaItem("GLS-003", "FAC-2026-02", "Bormioli Rocco", "Whiskey Tumbler Set", 1250.00, "", 20));
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}