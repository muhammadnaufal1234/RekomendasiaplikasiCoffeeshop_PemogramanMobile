package com.example.monokromcoffee;

/**
 * Model data untuk satu ulasan (review) coffee shop yang ditulis user.
 */
public class Review {

    private String shopName;     // Nama coffee shop
    private int    logoResId;    // Resource ID logo coffee shop
    private int    rating;       // Bintang 1–5
    private String comment;      // Isi ulasan
    private String date;         // Tanggal ulasan, e.g. "6 Jun 2025"
    private String userName;     // Nama pengguna yang menulis

    public Review() {
        // Diperlukan oleh Gson saat deserialisasi
    }

    public Review(String shopName, int logoResId, int rating,
                  String comment, String date, String userName) {
        this.shopName  = shopName;
        this.logoResId = logoResId;
        this.rating    = rating;
        this.comment   = comment;
        this.date      = date;
        this.userName  = userName;
    }

    // ─── Getters ───────────────────────────────────────────────────────────────

    public String getShopName()  { return shopName; }
    public int    getLogoResId() { return logoResId; }
    public int    getRating()    { return rating; }
    public String getComment()   { return comment; }
    public String getDate()      { return date; }
    public String getUserName()  { return userName; }

    // ─── Setters ───────────────────────────────────────────────────────────────

    public void setShopName(String shopName)   { this.shopName = shopName; }
    public void setLogoResId(int logoResId)    { this.logoResId = logoResId; }
    public void setRating(int rating)          { this.rating = rating; }
    public void setComment(String comment)     { this.comment = comment; }
    public void setDate(String date)           { this.date = date; }
    public void setUserName(String userName)   { this.userName = userName; }
}
