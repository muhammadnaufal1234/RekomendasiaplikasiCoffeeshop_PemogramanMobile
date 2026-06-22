package com.example.monokromcoffee;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class SettingsFragment extends Fragment {

    private static final String KEY_PROFILE_PHOTO_PATH = "profile_photo_path";
    private static final String PHOTO_FILE_NAME = "profile_photo.jpg";

    private LinearLayout itemChangePassword, itemAbout, itemMyFavorites;
    private LinearLayout itemSwitchAccount, itemLogout, itemDeleteAccount;
    private SwitchMaterial switchTheme;
    private TextView tvProfileName, tvProfileEmail;
    private ImageView ivProfilePhoto;
    private FrameLayout flAvatarContainer;
    private SharedPreferences prefs;

    // Launcher untuk membuka galeri
    private final ActivityResultLauncher<String> pickImageLauncher =
        registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                savePhotoLocally(uri);
            }
        });

    // Launcher untuk minta permission galeri
    private final ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                openGallery();
            } else {
                Toast.makeText(requireContext(),
                    "Izin akses galeri diperlukan untuk upload foto profil.",
                    Toast.LENGTH_SHORT).show();
            }
        });

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
        tvProfileName  = view.findViewById(R.id.tv_profile_name);
        tvProfileEmail = view.findViewById(R.id.tv_profile_email);
        ivProfilePhoto = view.findViewById(R.id.iv_profile_photo);
        flAvatarContainer = view.findViewById(R.id.fl_avatar_container);

        itemMyFavorites   = view.findViewById(R.id.item_my_favorites);
        itemChangePassword = view.findViewById(R.id.item_change_password);
        switchTheme       = view.findViewById(R.id.switch_theme);
        itemAbout         = view.findViewById(R.id.item_about);
        itemSwitchAccount = view.findViewById(R.id.item_switch_account);
        itemLogout        = view.findViewById(R.id.item_logout);
        itemDeleteAccount = view.findViewById(R.id.item_delete_account);

        boolean isDark = prefs.getBoolean("dark_mode", false);
        switchTheme.setChecked(isDark);
    }

    private void loadUserProfile() {
        // Nama & email dari Firebase Auth
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name  = user.getDisplayName();
            String email = user.getEmail();
            if (name  != null && !name.isEmpty())  tvProfileName.setText(name);
            if (email != null && !email.isEmpty()) tvProfileEmail.setText(email);
        }

        // Load foto dari local storage (SharedPreferences menyimpan path-nya)
        String savedPath = prefs.getString(KEY_PROFILE_PHOTO_PATH, null);
        if (savedPath != null) {
            File photoFile = new File(savedPath);
            if (photoFile.exists()) {
                loadPhotoFromFile(photoFile);
            } else {
                // File tidak ada, hapus path yang tersimpan
                prefs.edit().remove(KEY_PROFILE_PHOTO_PATH).apply();
            }
        }
    }

    /** Load foto dari File menggunakan Glide dengan CircleCrop */
    private void loadPhotoFromFile(File file) {
        if (getContext() == null) return;
        ivProfilePhoto.setColorFilter(null);
        ivProfilePhoto.setPadding(0, 0, 0, 0);
        Glide.with(getContext())
            .load(file)
            .transform(new CircleCrop())
            .placeholder(R.drawable.ic_avatar_placeholder)
            .error(R.drawable.ic_avatar_placeholder)
            .into(ivProfilePhoto);
    }

    private void setupListeners() {
        // Tap avatar → tampilkan opsi foto
        flAvatarContainer.setOnClickListener(v -> showPhotoOptions());

        // Menu Favorite
        itemMyFavorites.setOnClickListener(v -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_favorite);
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
                    String newPass     = etNewPass.getText().toString().trim();
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
                                    Toast.makeText(requireContext(), "✅ Password berhasil diubah!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(requireContext(), "Gagal mengubah password. Silakan login ulang.", Toast.LENGTH_LONG).show();
                                }
                            });
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
                isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // Tentang Aplikasi
        itemAbout.setOnClickListener(v ->
            new AlertDialog.Builder(requireContext())
                .setTitle("Tentang Aplikasi")
                .setMessage("Monokrom Coffee\nVersi 1.1.0\n\nAplikasi rekomendasi coffee shop terbaik untuk Anda.")
                .setPositiveButton("Tutup", null)
                .show()
        );

        // Pindah Akun
        itemSwitchAccount.setOnClickListener(v -> showSwitchAccountConfirmation());

        // Logout
        if (itemLogout != null) {
            itemLogout.setOnClickListener(v -> showLogoutConfirmation());
        }

        // Hapus Akun
        itemDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmation());
    }

    // ===================== FOTO PROFIL (LOCAL STORAGE) =====================

    /** Dialog pilihan: Pilih Foto / Hapus Foto */
    private void showPhotoOptions() {
        String savedPath = prefs.getString(KEY_PROFILE_PHOTO_PATH, null);
        boolean hasPhoto = savedPath != null && new File(savedPath).exists();

        String[] options = hasPhoto
            ? new String[]{"📷 Pilih dari Galeri", "🗑 Hapus Foto Profil"}
            : new String[]{"📷 Pilih dari Galeri"};

        new AlertDialog.Builder(requireContext())
            .setTitle("Foto Profil")
            .setItems(options, (dialog, which) -> {
                if (which == 0) checkPermissionAndOpenGallery();
                else if (which == 1) confirmDeletePhoto();
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    /** Cek permission lalu buka galeri */
    private void checkPermissionAndOpenGallery() {
        String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            ? Manifest.permission.READ_MEDIA_IMAGES
            : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(requireContext(), permission)
                == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void openGallery() {
        pickImageLauncher.launch("image/*");
    }

    /**
     * Simpan foto yang dipilih ke internal storage app,
     * lalu simpan path-nya ke SharedPreferences.
     */
    private void savePhotoLocally(Uri imageUri) {
        if (getContext() == null) return;

        try {
            // Buka stream dari URI galeri
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Toast.makeText(requireContext(), "Gagal membaca gambar.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Decode & compress bitmap (max 512x512 agar hemat storage)
            Bitmap original = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            Bitmap resized = resizeBitmap(original, 512);

            // Simpan ke file internal storage
            File photoFile = new File(requireContext().getFilesDir(), PHOTO_FILE_NAME);
            FileOutputStream fos = new FileOutputStream(photoFile);
            resized.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.close();

            // Simpan path ke SharedPreferences
            prefs.edit().putString(KEY_PROFILE_PHOTO_PATH, photoFile.getAbsolutePath()).apply();

            // Tampilkan di UI
            loadPhotoFromFile(photoFile);
            Toast.makeText(requireContext(), "✅ Foto profil berhasil diperbarui!", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Toast.makeText(requireContext(), "Gagal menyimpan foto: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /** Resize bitmap agar tidak melebihi maxSize pixel */
    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width <= maxSize && height <= maxSize) return bitmap;

        float ratio = (float) maxSize / Math.max(width, height);
        int newWidth  = Math.round(width  * ratio);
        int newHeight = Math.round(height * ratio);
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    /** Konfirmasi hapus foto */
    private void confirmDeletePhoto() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Hapus Foto Profil")
            .setMessage("Foto profil akan dihapus dan diganti avatar default. Lanjutkan?")
            .setPositiveButton("Ya, Hapus", (dialog, which) -> deleteLocalPhoto())
            .setNegativeButton("Batal", null)
            .show();
    }

    /** Hapus file foto dari internal storage & bersihkan SharedPreferences */
    private void deleteLocalPhoto() {
        String savedPath = prefs.getString(KEY_PROFILE_PHOTO_PATH, null);
        if (savedPath != null) {
            File photoFile = new File(savedPath);
            if (photoFile.exists()) photoFile.delete();
            prefs.edit().remove(KEY_PROFILE_PHOTO_PATH).apply();
        }

        // Reset UI ke avatar default
        ivProfilePhoto.setImageResource(R.drawable.ic_avatar_placeholder);
        ivProfilePhoto.setColorFilter(
            ContextCompat.getColor(requireContext(), R.color.text_secondary));
        ivProfilePhoto.setPadding(48, 48, 48, 48);

        Toast.makeText(requireContext(), "Foto profil berhasil dihapus.", Toast.LENGTH_SHORT).show();
    }

    // ===================== ACCOUNT ACTIONS =====================

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
            .setMessage("PEMBERITAHUAN: Kamu akan menghapus akun ini secara permanen. Tindakan ini tidak bisa dibatalkan!\n\nLanjutkan?")
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
                    performLogout(false);
                } else {
                    Toast.makeText(requireContext(), "Gagal menghapus akun. Mohon relogin dan coba lagi.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void performLogout() { performLogout(true); }

    private void performLogout(boolean showToast) {
        FirebaseAuth.getInstance().signOut();
        if (showToast) Toast.makeText(requireContext(), "Berhasil keluar", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
