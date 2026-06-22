package com.example.monokromcoffee;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Manajer fitur Review.
 * Menyimpan dan memuat daftar review menggunakan SharedPreferences (JSON via Gson).
 *
 * Cara pakai:
 *   ReviewManager rm = new ReviewManager(context);
 *   rm.addReview(review);           // tambah
 *   rm.removeReview(index);         // hapus berdasarkan posisi
 *   rm.getReviews();                // ambil semua
 *   rm.getReviewsForShop("Sbux");   // filter per coffee shop
 *   rm.hasReviewedShop("Sbux");     // cek sudah review atau belum
 */
public class ReviewManager {

    private static final String PREFS_NAME   = "ReviewPrefs";
    private static final String KEY_REVIEWS  = "reviews_json";

    private final SharedPreferences prefs;
    private final Gson gson;

    public ReviewManager(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    // ─── Public API ────────────────────────────────────────────────────────────

    /**
     * Menambahkan review baru.
     * Satu user hanya boleh punya SATU review per coffee shop.
     * Jika sudah pernah review shop yang sama → update (replace).
     */
    public void addReview(Review review) {
        List<Review> list = getReviews();

        // Update jika sudah ada review untuk shop yang sama
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getShopName().equals(review.getShopName())) {
                list.set(i, review); // replace
                saveList(list);
                return;
            }
        }
        // Tambahkan baru jika belum pernah review
        list.add(review);
        saveList(list);
    }

    /**
     * Menghapus review berdasarkan indeks list.
     */
    public void removeReview(int index) {
        List<Review> list = getReviews();
        if (index >= 0 && index < list.size()) {
            list.remove(index);
            saveList(list);
        }
    }

    /**
     * Menghapus review berdasarkan nama coffee shop.
     */
    public void removeReviewForShop(String shopName) {
        List<Review> list = getReviews();
        list.removeIf(r -> r.getShopName().equals(shopName));
        saveList(list);
    }

    /**
     * Mengambil semua review. Mengembalikan list kosong jika belum ada.
     */
    public List<Review> getReviews() {
        String json = prefs.getString(KEY_REVIEWS, null);
        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<List<Review>>() {}.getType();
        List<Review> list = gson.fromJson(json, type);
        return (list != null) ? list : new ArrayList<>();
    }

    /**
     * Mengambil review untuk coffee shop tertentu (null jika belum ada).
     */
    public Review getReviewForShop(String shopName) {
        for (Review r : getReviews()) {
            if (r.getShopName().equals(shopName)) return r;
        }
        return null;
    }

    /**
     * Cek apakah user sudah pernah menulis review untuk shop tertentu.
     */
    public boolean hasReviewedShop(String shopName) {
        return getReviewForShop(shopName) != null;
    }

    // ─── Private helper ────────────────────────────────────────────────────────

    private void saveList(List<Review> list) {
        prefs.edit()
             .putString(KEY_REVIEWS, gson.toJson(list))
             .apply();
    }
}
