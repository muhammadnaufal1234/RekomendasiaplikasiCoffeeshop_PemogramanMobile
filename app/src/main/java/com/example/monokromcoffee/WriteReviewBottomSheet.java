package com.example.monokromcoffee;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * BottomSheetDialogFragment untuk menulis ulasan coffee shop.
 *
 * Cara pakai dari CoffeeDetailActivity:
 *   WriteReviewBottomSheet.newInstance(shopName, logoResId)
 *       .show(getSupportFragmentManager(), "WriteReview");
 *
 * Setelah berhasil disimpan, callback OnReviewSubmittedListener dipanggil.
 */
public class WriteReviewBottomSheet extends BottomSheetDialogFragment {

    // ─── Arguments keys ────────────────────────────────────────────────────────
    private static final String ARG_SHOP_NAME = "shop_name";
    private static final String ARG_LOGO_RES  = "logo_res";

    // ─── State ─────────────────────────────────────────────────────────────────
    private int selectedRating = 0; // 0 = belum pilih
    private final int[] starIds = {
        R.id.star_sel_1, R.id.star_sel_2, R.id.star_sel_3,
        R.id.star_sel_4, R.id.star_sel_5
    };
    private ImageView[] starViews;

    // ─── Callback ──────────────────────────────────────────────────────────────
    public interface OnReviewSubmittedListener {
        void onSubmitted(Review review);
    }
    private OnReviewSubmittedListener listener;

    // ─── Factory ───────────────────────────────────────────────────────────────

    public static WriteReviewBottomSheet newInstance(String shopName, int logoResId) {
        WriteReviewBottomSheet sheet = new WriteReviewBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_SHOP_NAME, shopName);
        args.putInt(ARG_LOGO_RES, logoResId);
        sheet.setArguments(args);
        return sheet;
    }

    // ─── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnReviewSubmittedListener) {
            listener = (OnReviewSubmittedListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String shopName = getArguments() != null ? getArguments().getString(ARG_SHOP_NAME, "") : "";
        int    logoRes  = getArguments() != null ? getArguments().getInt(ARG_LOGO_RES, 0) : 0;

        // Bind views
        TextView  tvShopLabel  = view.findViewById(R.id.tv_review_sheet_shop);
        EditText  etUsername   = view.findViewById(R.id.et_review_username);
        EditText  etComment    = view.findViewById(R.id.et_review_comment);
        Button    btnSubmit    = view.findViewById(R.id.btn_submit_review);
        Button    btnCancel    = view.findViewById(R.id.btn_cancel_review);

        tvShopLabel.setText(shopName);

        // Cari nama default (dari login / settings)
        String defaultUserName = "Pengguna";
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            defaultUserName = user.getDisplayName();
        } else {
            // Fallback: coba dari SharedPreferences
            android.content.SharedPreferences prefs = requireContext()
                    .getSharedPreferences("Settings", android.content.Context.MODE_PRIVATE);
            String savedName = prefs.getString("user_name", "");
            if (!savedName.isEmpty()) defaultUserName = savedName;
        }

        // Inisialisasi bintang
        starViews = new ImageView[5];
        for (int i = 0; i < 5; i++) {
            starViews[i] = view.findViewById(starIds[i]);
            final int starIndex = i + 1; // 1-based
            starViews[i].setOnClickListener(v -> {
                selectedRating = starIndex;
                updateStarUI();
            });
        }

        // Jika sudah pernah review shop ini → pre-fill data lama
        ReviewManager rm = new ReviewManager(requireContext());
        Review existing = rm.getReviewForShop(shopName);
        if (existing != null) {
            selectedRating = existing.getRating();
            updateStarUI();
            etComment.setText(existing.getComment());
            if (existing.getUserName() != null && !existing.getUserName().isEmpty()) {
                defaultUserName = existing.getUserName();
            }
        }

        // Set nama di EditText
        etUsername.setText(defaultUserName);

        btnSubmit.setOnClickListener(v -> {
            String inputName = etUsername.getText().toString().trim();
            String comment   = etComment.getText().toString().trim();

            if (inputName.isEmpty()) {
                etUsername.setError("Nama tidak boleh kosong");
                etUsername.requestFocus();
                return;
            }
            if (selectedRating == 0) {
                Toast.makeText(requireContext(), "Beri rating terlebih dahulu!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (comment.isEmpty()) {
                etComment.setError("Komentar tidak boleh kosong");
                etComment.requestFocus();
                return;
            }

            // Format tanggal sekarang
            String date = new SimpleDateFormat("d MMM yyyy", new Locale("id", "ID"))
                    .format(new Date());

            // Buat objek Review — pakai inputName yang diisi manual oleh user
            Review review = new Review(shopName, logoRes, selectedRating, comment, date, inputName);

            // Simpan ke ReviewManager
            rm.addReview(review);



            Toast.makeText(requireContext(), "Ulasan berhasil disimpan! ☕", Toast.LENGTH_SHORT).show();

            // Panggil callback ke Activity
            if (listener != null) listener.onSubmitted(review);

            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new BottomSheetDialog(requireContext(), R.style.Theme_MonokromCoffee);
    }

    // ─── Private helper ────────────────────────────────────────────────────────

    /** Perbarui tampilan 5 bintang sesuai selectedRating */
    private void updateStarUI() {
        int activeColor   = Color.parseColor("#8B4513");
        int inactiveColor = Color.parseColor("#E0E0E0");
        for (int i = 0; i < starViews.length; i++) {
            starViews[i].setColorFilter(i < selectedRating ? activeColor : inactiveColor);
        }
    }
}
