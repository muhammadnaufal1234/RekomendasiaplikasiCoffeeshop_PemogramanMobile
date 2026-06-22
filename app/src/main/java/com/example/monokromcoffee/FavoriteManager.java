package com.example.monokromcoffee;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Manajer fitur Favorite.
 * Menyimpan dan memuat daftar coffee shop favorit menggunakan
 * SharedPreferences (serialisasi JSON via Gson).
 *
 * Cara pakai:
 *   FavoriteManager fm = new FavoriteManager(context);
 *   fm.addFavorite(coffeeShop);    // tambah
 *   fm.removeFavorite("Starbucks"); // hapus
 *   fm.isFavorite("Starbucks");    // cek status
 *   fm.getFavorites();             // ambil semua
 */
public class FavoriteManager {

    private static final String PREFS_NAME = "FavoritePrefs";
    private static final String KEY_FAVORITES = "favorites_json";

    private final SharedPreferences prefs;
    private final Gson gson;

    public FavoriteManager(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    // ─── Public API ────────────────────────────────────────────────────────────

    /**
     * Menambahkan coffee shop ke daftar favorit.
     * Jika sudah ada (berdasarkan nama), tidak akan ditambahkan duplikat.
     */
    public void addFavorite(CoffeeShop shop) {
        List<CoffeeShop> list = getFavorites();
        for (CoffeeShop s : list) {
            if (s.getName().equals(shop.getName())) return; // sudah ada
        }
        list.add(shop);
        saveList(list);
    }

    /**
     * Menghapus coffee shop dari daftar favorit berdasarkan nama.
     */
    public void removeFavorite(String shopName) {
        List<CoffeeShop> list = getFavorites();
        list.removeIf(s -> s.getName().equals(shopName));
        saveList(list);
    }

    /**
     * Mengecek apakah coffee shop sudah ada di favorit.
     */
    public boolean isFavorite(String shopName) {
        for (CoffeeShop s : getFavorites()) {
            if (s.getName().equals(shopName)) return true;
        }
        return false;
    }

    /**
     * Toggle: jika sudah favorit → hapus, jika belum → tambah.
     * @return true jika setelah toggle statusnya menjadi favorit, false jika dihapus.
     */
    public boolean toggleFavorite(CoffeeShop shop) {
        if (isFavorite(shop.getName())) {
            removeFavorite(shop.getName());
            return false;
        } else {
            addFavorite(shop);
            return true;
        }
    }

    /**
     * Mengambil seluruh daftar favorit. Mengembalikan list kosong jika belum ada.
     */
    public List<CoffeeShop> getFavorites() {
        String json = prefs.getString(KEY_FAVORITES, null);
        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<List<CoffeeShop>>() {}.getType();
        List<CoffeeShop> list = gson.fromJson(json, type);
        return (list != null) ? list : new ArrayList<>();
    }

    // ─── Private helper ────────────────────────────────────────────────────────

    private void saveList(List<CoffeeShop> list) {
        prefs.edit()
             .putString(KEY_FAVORITES, gson.toJson(list))
             .apply();
    }
}
