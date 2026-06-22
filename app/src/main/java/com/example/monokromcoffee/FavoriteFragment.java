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
 * Fragment yang menampilkan daftar coffee shop favorit milik user.
 *
 * Logika:
 * - Membaca daftar favorit dari FavoriteManager (SharedPreferences via Gson).
 * - Jika kosong → tampilkan empty state.
 * - Jika ada data → tampilkan RecyclerView dengan FavoriteCoffeeAdapter.
 * - Setiap item bisa diklik untuk membuka CoffeeDetailActivity.
 * - Tombol hapus (❤) di setiap item menghapus dari daftar tanpa perlu reload.
 * - onResume() dipanggil setiap kali fragment aktif kembali (misalnya setelah
 *   kembali dari CoffeeDetailActivity), sehingga daftar selalu up-to-date.
 */
public class FavoriteFragment extends Fragment {

    private RecyclerView rvFavorites;
    private LinearLayout layoutEmptyState;
    private TextView tvSubtitle;

    private FavoriteManager favoriteManager;
    private FavoriteCoffeeAdapter adapter;
    private List<CoffeeShop> favoriteList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFavorites    = view.findViewById(R.id.rv_favorites);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        tvSubtitle     = view.findViewById(R.id.tv_fav_subtitle);

        favoriteManager = new FavoriteManager(requireContext());

        // Setup RecyclerView
        rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadAndDisplayFavorites();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh setiap kali fragment kembali ke foreground
        // (misalnya: user kembali dari CoffeeDetailActivity setelah toggle favorite)
        loadAndDisplayFavorites();
    }

    // ─── Private helper ────────────────────────────────────────────────────────

    private void loadAndDisplayFavorites() {
        favoriteList = favoriteManager.getFavorites();

        if (favoriteList.isEmpty()) {
            showEmptyState();
        } else {
            showFavoriteList();
        }
    }

    private void showEmptyState() {
        rvFavorites.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
        tvSubtitle.setText("0 coffee shop tersimpan");
    }

    private void showFavoriteList() {
        layoutEmptyState.setVisibility(View.GONE);
        rvFavorites.setVisibility(View.VISIBLE);

        updateSubtitle();

        adapter = new FavoriteCoffeeAdapter(
                favoriteList,
                favoriteManager,
                removedShop -> {
                    // Callback setelah item dihapus oleh adapter
                    updateSubtitle();
                    if (favoriteList.isEmpty()) {
                        showEmptyState();
                    }
                }
        );
        rvFavorites.setAdapter(adapter);
    }

    private void updateSubtitle() {
        int count = favoriteList.size();
        tvSubtitle.setText(count + " coffee shop tersimpan");
    }
}
