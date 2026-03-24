package com.example.monokromcoffee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class SettingsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout itemChangePassword, itemTheme, itemLogout;
    private TextView tvThemeStatus;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Terapkan tema sebelum setContentView
        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);

        // Hide Action Bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_settings);

        initializeViews();
        setupListeners();
        updateThemeStatus();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back_settings);
        itemChangePassword = findViewById(R.id.item_change_password);
        itemTheme = findViewById(R.id.item_theme);
        itemLogout = findViewById(R.id.item_logout);
        tvThemeStatus = findViewById(R.id.tv_theme_status);
    }

    private void setupListeners() {
        // Tombol Kembali
        btnBack.setOnClickListener(v -> finish());

        // Ganti Password — tampilkan dialog input
        itemChangePassword.setOnClickListener(v -> {
            android.widget.EditText etNewPass = new android.widget.EditText(this);
            etNewPass.setHint("Password baru");
            etNewPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

            android.widget.EditText etConfirmPass = new android.widget.EditText(this);
            etConfirmPass.setHint("Konfirmasi password baru");
            etConfirmPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

            android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
            layout.setOrientation(android.widget.LinearLayout.VERTICAL);
            layout.setPadding(48, 24, 48, 0);
            layout.addView(etNewPass);
            layout.addView(etConfirmPass);

            new AlertDialog.Builder(this)
                .setTitle("Ganti Password")
                .setView(layout)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String newPass = etNewPass.getText().toString().trim();
                    String confirmPass = etConfirmPass.getText().toString().trim();
                    if (newPass.isEmpty() || confirmPass.isEmpty()) {
                        Toast.makeText(this, "Kolom tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                    } else if (!newPass.equals(confirmPass)) {
                        Toast.makeText(this, "Konfirmasi password tidak cocok!", Toast.LENGTH_SHORT).show();
                    } else if (newPass.length() < 6) {
                        Toast.makeText(this, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show();
                    } else {
                        prefs.edit().putString("user_password", newPass).apply();
                        Toast.makeText(this, "✅ Password berhasil diubah!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
        });

        // Ubah Tema
        itemTheme.setOnClickListener(v -> {
            boolean currentDark = prefs.getBoolean("dark_mode", false);
            boolean newDark = !currentDark;
            prefs.edit().putBoolean("dark_mode", newDark).apply();
            updateThemeStatus();
            AppCompatDelegate.setDefaultNightMode(
                    newDark ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // Logout
        itemLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void updateThemeStatus() {
        boolean isDark = prefs.getBoolean("dark_mode", false);
        if (tvThemeStatus != null) {
            tvThemeStatus.setText(isDark ? "Dark mode" : "Light mode");
        }
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Apakah kamu yakin ingin keluar?")
                .setPositiveButton("Ya, Keluar", (dialog, which) -> performLogout())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void performLogout() {
        // Sign out dari Firebase
        FirebaseAuth.getInstance().signOut();

        // Sign out dari Google (jika user login via Google)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInClient.signOut();

        Toast.makeText(this, "Berhasil logout", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
