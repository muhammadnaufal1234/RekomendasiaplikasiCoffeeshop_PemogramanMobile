package com.example.monokromcoffee;

/**
 * Model data untuk satu coffee shop.
 * Digunakan untuk menyimpan/memuat data favorit via SharedPreferences (JSON).
 */
public class CoffeeShop {

    private String name;        // Nama coffee shop, e.g. "Kenangan Coffee"
    private int logoResId;      // Resource ID drawable logo
    private String rating;      // e.g. "4.8"
    private String avgPrice;    // e.g. "Rp 15k - 35k"
    private String address;     // Alamat ringkas

    public CoffeeShop() {
        // Required empty constructor untuk deserialisasi Gson
    }

    public CoffeeShop(String name, int logoResId, String rating, String avgPrice, String address) {
        this.name = name;
        this.logoResId = logoResId;
        this.rating = rating;
        this.avgPrice = avgPrice;
        this.address = address;
    }

    // ─── Getters ───────────────────────────────────────────────────────────────

    public String getName()     { return name; }
    public int    getLogoResId(){ return logoResId; }
    public String getRating()   { return rating; }
    public String getAvgPrice() { return avgPrice; }
    public String getAddress()  { return address; }

    // ─── Setters ───────────────────────────────────────────────────────────────

    public void setName(String name)         { this.name = name; }
    public void setLogoResId(int logoResId)  { this.logoResId = logoResId; }
    public void setRating(String rating)     { this.rating = rating; }
    public void setAvgPrice(String avgPrice) { this.avgPrice = avgPrice; }
    public void setAddress(String address)   { this.address = address; }
}
