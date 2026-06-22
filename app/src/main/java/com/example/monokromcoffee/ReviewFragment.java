package com.example.monokromcoffee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Fragment yang menampilkan semua ulasan yang pernah ditulis user.
 *
 * Logika:
 * - Membaca daftar review dari ReviewManager (SharedPreferences via Gson).
 * - Jika kosong → tampilkan empty state yang mengarahkan user ke CoffeeDetailActivity.
 * - Jika ada data → tampilkan RecyclerView dengan ReviewAdapter.
 * - Setiap item bisa dihapus; subtitle jumlah ulasan diperbarui otomatis.
 * - onResume() me-refresh daftar setiap kali fragment kembali aktif
 *   (misalnya setelah kembali dari CoffeeDetailActivity).
 */
public class ReviewFragment extends Fragment {

    private RecyclerView    rvReviews;
    private LinearLayout    layoutEmpty;
    private TextView        tvSubtitle;

    private ReviewManager   reviewManager;
    private ReviewAdapter   adapter;
    private List<Review>    reviewList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvReviews   = view.findViewById(R.id.rv_reviews);
        layoutEmpty = view.findViewById(R.id.layout_review_empty);
        tvSubtitle  = view.findViewById(R.id.tv_review_subtitle);

        reviewManager = new ReviewManager(requireContext());
        rvReviews.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadAndDisplay();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh setiap kali fragment kembali ke layar
        loadAndDisplay();
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    private void loadAndDisplay() {
        reviewList = reviewManager.getReviews();

        if (reviewList.isEmpty()) {
            showEmpty();
        } else {
            showList();
        }
    }

    private void showEmpty() {
        rvReviews.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
        tvSubtitle.setText("0 ulasan ditulis");
    }

    private void showList() {
        layoutEmpty.setVisibility(View.GONE);
        rvReviews.setVisibility(View.VISIBLE);
        updateSubtitle();

        adapter = new ReviewAdapter(
                reviewList,
                reviewManager,
                (deletedReview, position) -> {
                    // Callback setelah item dihapus
                    updateSubtitle();
                    if (reviewList.isEmpty()) showEmpty();
                }
        );
        rvReviews.setAdapter(adapter);
    }

    private void updateSubtitle() {
        int count = reviewList.size();
        tvSubtitle.setText(count + " ulasan ditulis");
    }
}
