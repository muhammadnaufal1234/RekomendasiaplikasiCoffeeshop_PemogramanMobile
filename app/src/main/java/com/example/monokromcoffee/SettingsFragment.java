package com.example.monokromcoffee;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SettingsFragment extends Fragment {

    private LinearLayout itemChangePassword, itemAbout, itemMyFavorites;
    private LinearLayout itemSwitchAccount, itemLogout, itemDeleteAccount;
    private SwitchMaterial switchTheme;
    private TextView tvProfileName, tvProfileEmail;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        prefs = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        
        initializeViews(view);
        loadUserProfile();
        setupListeners();
        
        return view;
    }

    private void initializeViews(View view) {
        tvProfileName = view.findViewById(R.id.tv_profile_name);
        tvProfileEmail = view.findViewById(R.id.tv_profile_email);
        
        itemMyFavorites = view.findViewById(R.id.item_my_favorites);
        itemChangePassword = view.findViewById(R.id.item_change_password);
        
        switchTheme = view.findViewById(R.id.switch_theme);
        
        itemAbout = view.findViewById(R.id.item_about);
        itemSwitchAccount = view.findViewById(R.id.item_switch_account);
        itemLogout = view.findViewById(R.id.item_logout);
        itemDeleteAccount = view.findViewById(R.id.item_delete_account);

        // Init theme toggle state
        boolean isDark = prefs.getBoolean("dark_mode", false);
        switchTheme.setChecked(isDark);
    }

    private void loadUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            
            if (name != null && !name.isEmpty()) {
                tvProfileName.setText(name);
            }
            if (email != null && !email.isEmpty()) {
                tvProfileEmail.setText(email);
            }
        }
    }

    private void setupListeners() {
        // Menu Favorite
        itemMyFavorites.setOnClickListener(v -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_favorite);
            }
        });

        // Ganti Password
        itemChangePassword.setOnClickListener(v -> {
            android.widget.EditText etNewPass = new android.widget.EditText(requireContext());
            etNewPass.setHint("Password baru");
            etNewPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

            android.widget.EditText etConfirmPass = new android.widget.EditText(requireContext());
            etConfirmPass.setHint("Konfirmasi password baru");
            etConfirmPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

            android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
            layout.setOrientation(android.widget.LinearLayout.VERTICAL);
            layout.setPadding(48, 24, 48, 0);
            layout.addView(etNewPass);
            layout.addView(etConfirmPass);

            new AlertDialog.Builder(requireContext())
                .setTitle("Ganti Password")
                .setView(layout)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String newPass = etNewPass.getText().toString().trim();
                    String confirmPass = etConfirmPass.getText().toString().trim();
                    if (newPass.isEmpty() || confirmPass.isEmpty()) {
                        Toast.makeText(requireContext(), "Kolom tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                    } else if (!newPass.equals(confirmPass)) {
                        Toast.makeText(requireContext(), "Konfirmasi password tidak cocok!", Toast.LENGTH_SHORT).show();
                    } else if (newPass.length() < 6) {
                        Toast.makeText(requireContext(), "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show();
                    } else {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            user.updatePassword(newPass).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    prefs.edit().putString("user_password", newPass).apply();
                                    Toast.makeText(requireContext(), "✅ Password berhasil diubah!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(requireContext(), "Gagal mengubah password. Silakan login ulang.", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            prefs.edit().putString("user_password", newPass).apply();
                            Toast.makeText(requireContext(), "✅ Password lokal berhasil diubah!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
        });

        // Toggle Theme
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // Tentang Aplikasi
        itemAbout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("Tentang Aplikasi")
                .setMessage("Monokrom Coffee\n" +
                        "Versi 1.1.0\n\n" +
                        "Aplikasi rekomendasi coffee shop terbaik untuk Anda. Temukan tempat ngopi favorit dengan suasana yang nyaman dan kopi berkualitas.")
                .setPositiveButton("Tutup", null)
                .show();
        });

        // Pindah Akun / Switch Account
        itemSwitchAccount.setOnClickListener(v -> showSwitchAccountConfirmation());

        // Logout
        if(itemLogout != null) {
            itemLogout.setOnClickListener(v -> showLogoutConfirmation());
        }

        // Hapus Akun / Delete Account
        itemDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmation());
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Keluar Akun")
                .setMessage("Apakah kamu yakin ingin keluar?")
                .setPositiveButton("Ya, Keluar", (dialog, which) -> performLogout())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showSwitchAccountConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Pindah Akun")
                .setMessage("Kamu akan keluar dari sesi ini. Lanjutkan pindah akun?")
                .setPositiveButton("Ya, Keluar", (dialog, which) -> performLogout())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showDeleteAccountConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hapus Akun Permanen")
                .setMessage("PEMBERITAHUAN: Kamu akan menghapus akun ini secara permanen dari database. Tindakan ini tidak bisa dibatalkan!\n\nLanjutkan?")
                .setPositiveButton("Ya, HAPUS", (dialog, which) -> performDeleteAccount())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void performDeleteAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(requireContext(), "Akun berhasil dihapus", Toast.LENGTH_LONG).show();
                    
                    // Call logout to clear google sessions if any
                    performLogout(false);
                } else {
                    Toast.makeText(requireContext(), "Gagal menghapus akun. Mohon relogin dan coba lagi.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void performLogout() {
        performLogout(true);
    }

    private void performLogout(boolean showToast) {
        // Sign out from Firebase (also clears Google session linked via Firebase)
        FirebaseAuth.getInstance().signOut();

        if (showToast) {
            Toast.makeText(requireContext(), "Berhasil keluar", Toast.LENGTH_SHORT).show();
        }
        
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
