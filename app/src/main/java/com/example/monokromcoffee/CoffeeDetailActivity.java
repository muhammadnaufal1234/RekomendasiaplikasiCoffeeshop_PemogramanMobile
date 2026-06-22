package com.example.monokromcoffee;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.ArrayList;
import java.util.List;

public class CoffeeDetailActivity extends AppCompatActivity
        implements WriteReviewBottomSheet.OnReviewSubmittedListener {

    private ImageView btnBack, imgBanner;
    private TextView tvShopName, tvDescription, tvRating, tvReviewsCount, tvAddressSummary, tvAvgPrice;
    private Button btnAllMenu, btnHotCoffee, btnIceCoffee, btnNonCoffee, btnCancel, btnOke, btnFavoriteDetail;
    private Button btnWriteReview;

    // Section ulasan di halaman detail
    private LinearLayout containerReviewsDetail, layoutNoReview;
    private TextView tvReviewCountDetail;

    // Menu cards
    private CardView cardMenu1, cardMenu2, cardMenu3, cardMenu4, cardMenu5, cardMenu6, cardMenu7, cardMenu8, cardMenu9;

    // Menu images
    private ImageView imgMenu1, imgMenu2, imgMenu3, imgMenu4, imgMenu5, imgMenu6, imgMenu7, imgMenu8, imgMenu9;
    private TextView tvMenu1, tvMenu2, tvMenu3, tvMenu4, tvMenu5, tvMenu6, tvMenu7, tvMenu8, tvMenu9;
    private TextView tvPrice1, tvPrice2, tvPrice3, tvPrice4, tvPrice5, tvPrice6, tvPrice7, tvPrice8, tvPrice9;

    private String shopName;

    // Filter state
    private String currentFilter = "all"; // "all", "hot", "ice", "non"

    // List untuk menyimpan menu cards
    private List<CardView> allMenuCards;
    private List<String> menuTypes; // "hot" atau "ice"

    private int themeColor = Color.parseColor("#8B4513");
    private int themeTextColor = Color.WHITE;

    // Favorite
    private FavoriteManager favoriteManager;
    private boolean isFavorited = false;

    // Review
    private ReviewManager reviewManager;

    // Data per shop (dibutuhkan untuk menyimpan ke favorit & review)
    private String shopRating = "";
    private String shopAvgPrice = "";
    private String shopAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load theme from SharedPreferences
        android.content.SharedPreferences sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        if (isDarkMode) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_coffee_detail);

        // Get shop name from intent
        shopName = getIntent().getStringExtra("shop_name");

        // Initialize views
        initializeViews();

        // Initialize Favorite Manager
        favoriteManager = new FavoriteManager(this);

        // Initialize Review Manager
        reviewManager = new ReviewManager(this);

        // Initialize menu lists
        initializeMenuLists();

        // Load shop data
        loadShopData();

        // Setup listeners
        setupListeners();

        // Set default button state (all shown)
        updateButtonStyles();

        // Perbarui tampilan tombol favorit sesuai status tersimpan
        refreshFavoriteButton();

        // Perbarui tampilan tombol review sesuai status tersimpan
        refreshReviewButton();

        // Tampilkan ulasan untuk coffee shop ini
        renderReviewsOnDetail();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh ulasan setiap kali Activity kembali ke foreground
        renderReviewsOnDetail();
        refreshReviewButton();
        refreshFavoriteButton();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back_detail);
        imgBanner = findViewById(R.id.img_shop_photo);
        tvShopName = findViewById(R.id.tv_shop_name);
        tvDescription = findViewById(R.id.tv_description);
        btnAllMenu = findViewById(R.id.btn_all_menu);
        btnHotCoffee = findViewById(R.id.btn_hot_coffee);
        btnIceCoffee = findViewById(R.id.btn_ice_coffee);
        btnNonCoffee = findViewById(R.id.btn_non_coffee);
        btnCancel = findViewById(R.id.btn_cancel);
        btnOke = findViewById(R.id.btn_oke);
        btnFavoriteDetail = findViewById(R.id.btn_favorite_detail);
        btnWriteReview = findViewById(R.id.btn_write_review);

        // Section ulasan
        containerReviewsDetail = findViewById(R.id.container_reviews_detail);
        layoutNoReview         = findViewById(R.id.layout_no_review);
        tvReviewCountDetail    = findViewById(R.id.tv_review_count_detail);

        // Menu cards (using include)
        cardMenu1 = findViewById(R.id.card_menu_1);
        cardMenu2 = findViewById(R.id.card_menu_2);
        cardMenu3 = findViewById(R.id.card_menu_3);
        cardMenu4 = findViewById(R.id.card_menu_4);
        cardMenu5 = findViewById(R.id.card_menu_5);
        cardMenu6 = findViewById(R.id.card_menu_6);
        cardMenu7 = findViewById(R.id.card_menu_7);
        cardMenu8 = findViewById(R.id.card_menu_8);
        cardMenu9 = findViewById(R.id.card_menu_9);

        // Menu items inside cards
        imgMenu1 = cardMenu1.findViewById(R.id.img_menu);
        imgMenu2 = cardMenu2.findViewById(R.id.img_menu);
        imgMenu3 = cardMenu3.findViewById(R.id.img_menu);
        imgMenu4 = cardMenu4.findViewById(R.id.img_menu);
        imgMenu5 = cardMenu5.findViewById(R.id.img_menu);
        imgMenu6 = cardMenu6.findViewById(R.id.img_menu);
        imgMenu7 = cardMenu7.findViewById(R.id.img_menu);
        imgMenu8 = cardMenu8.findViewById(R.id.img_menu);
        imgMenu9 = cardMenu9.findViewById(R.id.img_menu);

        tvMenu1 = cardMenu1.findViewById(R.id.tv_menu_name);
        tvMenu2 = cardMenu2.findViewById(R.id.tv_menu_name);
        tvMenu3 = cardMenu3.findViewById(R.id.tv_menu_name);
        tvMenu4 = cardMenu4.findViewById(R.id.tv_menu_name);
        tvMenu5 = cardMenu5.findViewById(R.id.tv_menu_name);
        tvMenu6 = cardMenu6.findViewById(R.id.tv_menu_name);
        tvMenu7 = cardMenu7.findViewById(R.id.tv_menu_name);
        tvMenu8 = cardMenu8.findViewById(R.id.tv_menu_name);
        tvMenu9 = cardMenu9.findViewById(R.id.tv_menu_name);

        tvPrice1 = cardMenu1.findViewById(R.id.tv_menu_price);
        tvPrice2 = cardMenu2.findViewById(R.id.tv_menu_price);
        tvPrice3 = cardMenu3.findViewById(R.id.tv_menu_price);
        tvPrice4 = cardMenu4.findViewById(R.id.tv_menu_price);
        tvPrice5 = cardMenu5.findViewById(R.id.tv_menu_price);
        tvPrice6 = cardMenu6.findViewById(R.id.tv_menu_price);
        tvPrice7 = cardMenu7.findViewById(R.id.tv_menu_price);
        tvPrice8 = cardMenu8.findViewById(R.id.tv_menu_price);
        tvPrice9 = cardMenu9.findViewById(R.id.tv_menu_price);

        tvRating = findViewById(R.id.tv_rating);
        tvReviewsCount = findViewById(R.id.tv_reviews_count);
        tvAddressSummary = findViewById(R.id.tv_address_summary);
        tvAvgPrice = findViewById(R.id.tv_avg_price);
    }

    private void initializeMenuLists() {
        allMenuCards = new ArrayList<>();
        allMenuCards.add(cardMenu1);
        allMenuCards.add(cardMenu2);
        allMenuCards.add(cardMenu3);
        allMenuCards.add(cardMenu4);
        allMenuCards.add(cardMenu5);
        allMenuCards.add(cardMenu6);
        allMenuCards.add(cardMenu7);
        allMenuCards.add(cardMenu8);
        allMenuCards.add(cardMenu9);

        // Definisikan tipe menu (hot, ice, atau non)
        // 3 Hot + 3 Ice + 3 Non = 9 menu
        menuTypes = new ArrayList<>();
        menuTypes.add("hot"); // Menu 1 - Caramel Latte (Hot)
        menuTypes.add("hot"); // Menu 2 - Kopi Kenangan (Hot)
        menuTypes.add("hot"); // Menu 3 - Mantan Caramel (Hot)
        menuTypes.add("ice"); // Menu 4 - Mocha Coffee (Ice)
        menuTypes.add("ice"); // Menu 5 - Manischoco (Ice)
        menuTypes.add("ice"); // Menu 6 - Avocado Coffee (Ice)
        menuTypes.add("non"); // Menu 7 - Creamy Mancioco (Non Coffee)
        menuTypes.add("non"); // Menu 8 - Latte (Non Coffee)
        menuTypes.add("non"); // Menu 9 - Americano (Non Coffee)
    }

    private void loadShopData() {
        if (shopName == null) {
            finish();
            return;
        }

        tvShopName.setText(shopName);

        switch (shopName) {
            case "Kenangan Coffee":
                themeColor = Color.parseColor("#8B4513");
                themeTextColor = Color.WHITE;
                loadKenanganData();
                break;
            case "Starbucks Coffee":
                themeColor = Color.parseColor("#00704A");
                themeTextColor = Color.WHITE;
                loadStarbucksData();
                break;
            case "Tomoro Coffee":
                themeColor = Color.parseColor("#FF6B00");
                themeTextColor = Color.WHITE;
                loadTomoroData();
                break;
            case "Janji Jiwa Coffee":
                themeColor = Color.parseColor("#E6C280");
                themeTextColor = Color.parseColor("#4E342E");
                loadJanjiJiwaData();
                break;
            case "Point Coffee":
                themeColor = Color.parseColor("#00A859");
                themeTextColor = Color.WHITE;
                loadPointData();
                break;
        }

        if (btnOke != null) {
            btnOke.setBackgroundTintList(ColorStateList.valueOf(themeColor));
            btnOke.setTextColor(themeTextColor);
        }
        if (btnCancel != null) {
            btnCancel.setTextColor(themeColor);
        }
    }

    private void loadKenanganData() {
        imgBanner.setImageResource(R.drawable.logo_kopikenangan);
        tvDescription.setText(
                "Kenangan Coffee adalah coffee shop modern dengan konsep grab and go. Menyajikan berbagai jenis kopi berkualitas dengan harga terjangkau. Kenangan Coffee memiliki banyak cabang di Indonesia dan selalu ramai dikunjungi.");
        tvRating.setText(" 4.8");
        tvReviewsCount.setText(" (2.5k ulasan)");
        tvAddressSummary.setText("Jl. Senopati No. 34, Jakarta Selatan");
        tvAvgPrice.setText("Avg. Price: Rp 15k - 35k");
        shopRating   = "4.8";
        shopAvgPrice = "Rp 15k - 35k";
        shopAddress  = "Jl. Senopati No. 34, Jakarta Selatan";

        // HOT COFFEE (Menu 1-3)
        imgMenu1.setImageResource(R.drawable.kenangan_americanohot);
        tvMenu1.setText("Milo kenangan");
        tvPrice1.setText("20k");

        imgMenu2.setImageResource(R.drawable.kenangan_lattehot);
        tvMenu2.setText("Caffe Latte");
        tvPrice2.setText("15k");

        imgMenu3.setImageResource(R.drawable.kenangan_carramelhot);
        tvMenu3.setText("Carramel latte");
        tvPrice3.setText("18k");

        // ICE COFFEE (Menu 4-6)
        imgMenu4.setImageResource(R.drawable.kenangan_cappucino);
        tvMenu4.setText("Cappucino Coffee");
        tvPrice4.setText("18k");

        imgMenu5.setImageResource(R.drawable.kenangan_caffelatte);
        tvMenu5.setText("Caffe Latte");
        tvPrice5.setText("20k");

        imgMenu6.setImageResource(R.drawable.kenangan_hazelnut);
        tvMenu6.setText("Hazelnut Coffee");
        tvPrice6.setText("23k");

        // NON COFFEE (Menu 7-9)
        imgMenu7.setImageResource(R.drawable.kenagan_milo);
        tvMenu7.setText("Kenangan Milo");
        tvPrice7.setText("22k");

        imgMenu8.setImageResource(R.drawable.cokelat_kenangan);
        tvMenu8.setText("Chocolate Kenangan");
        tvPrice8.setText("21k");

        imgMenu9.setImageResource(R.drawable.kenangan_matcha);
        tvMenu9.setText("Matcha Kenangan");
        tvPrice9.setText("19k");
    }

    private void loadStarbucksData() {
        imgBanner.setImageResource(R.drawable.logo_starbuck);
        tvDescription.setText(
                "Starbucks adalah jaringan kedai kopi terbesar di dunia yang berasal dari Seattle, Amerika Serikat. Terkenal dengan kualitas kopi premium dan suasana nyaman untuk bekerja atau bersantai. Starbucks menyajikan berbagai varian kopi dan minuman lainnya.");
        tvRating.setText(" 4.9");
        tvReviewsCount.setText(" (5.2k ulasan)");
        tvAddressSummary.setText("Grand Indonesia, Jakarta Pusat");
        tvAvgPrice.setText("Avg. Price: Rp 45k - 80k");
        shopRating   = "4.9";
        shopAvgPrice = "Rp 45k - 80k";
        shopAddress  = "Grand Indonesia, Jakarta Pusat";

        // HOT COFFEE (Menu 1-3)
        imgMenu1.setImageResource(R.drawable.starbuck_hotcappucino);
        tvMenu1.setText("Coffe cappucino");
        tvPrice1.setText("35k");

        imgMenu2.setImageResource(R.drawable.starbuck_hotcaramel);
        tvMenu2.setText("Coffe Caramel");
        tvPrice2.setText("40k");

        imgMenu3.setImageResource(R.drawable.starbuck_hotmachiato);
        tvMenu3.setText("Salted Machiato");
        tvPrice3.setText("42k");

        // ICE COFFEE (Menu 4-6)
        imgMenu4.setImageResource(R.drawable.starbuck_pistachio);
        tvMenu4.setText("Coffe Pistachio");
        tvPrice4.setText("50k");

        imgMenu5.setImageResource(R.drawable.starbuck_cappucino);
        tvMenu5.setText("Coffe Cappucino");
        tvPrice5.setText("48k");

        imgMenu6.setImageResource(R.drawable.starbuck_salted);
        tvMenu6.setText("Coffe Salted Caramel");
        tvPrice6.setText("45k");

        // NON COFFEE (Menu 7-9)
        imgMenu7.setImageResource(R.drawable.starbuck_blackberrysmoothie);
        tvMenu7.setText("blackberry smoothie");
        tvPrice7.setText("44k");

        imgMenu8.setImageResource(R.drawable.starbuck_cokelat);
        tvMenu8.setText("Chocolate");
        tvPrice8.setText("52k");

        imgMenu9.setImageResource(R.drawable.starbuck_matcha);
        tvMenu9.setText("Matcha Latte");
        tvPrice9.setText("46k");
    }

    private void loadTomoroData() {
        imgBanner.setImageResource(R.drawable.logo_tomoro);
        tvDescription.setText(
                "Tomoro Coffee menawarkan kopi berkualitas tinggi dengan harga terjangkau. Fokus pada kualitas biji kopi pilihan dan pelayanan cepat. Tomoro Coffee memiliki konsep modern dan nyaman untuk bersantai atau bekerja.");
        tvRating.setText(" 4.7");
        tvReviewsCount.setText(" (1.8k ulasan)");
        tvAddressSummary.setText("Jl. Merdeka Barat No. 12, Jakarta Pusat");
        tvAvgPrice.setText("Avg. Price: Rp 12k - 25k");
        shopRating   = "4.7";
        shopAvgPrice = "Rp 12k - 25k";
        shopAddress  = "Jl. Merdeka Barat No. 12, Jakarta Pusat";

        // HOT COFFEE (Menu 1-3)
        imgMenu1.setImageResource(R.drawable.tomoro_hotamericano);
        tvMenu1.setText("Kopi Hitam");
        tvPrice1.setText("12k");

        imgMenu2.setImageResource(R.drawable.tomoro_hotcaramel);
        tvMenu2.setText("Caramel Coffe");
        tvPrice2.setText("15k");

        imgMenu3.setImageResource(R.drawable.tomoro_hotlatte);
        tvMenu3.setText("Coffee Latte");
        tvPrice3.setText("18k");

        // ICE COFFEE (Menu 4-6)
        imgMenu4.setImageResource(R.drawable.tomoro_kopisusu);
        tvMenu4.setText("Es Kopi Susu Gula Aren");
        tvPrice4.setText("17k");

        imgMenu5.setImageResource(R.drawable.tomoro_americano);
        tvMenu5.setText("Iced Americano");
        tvPrice5.setText("16k");

        imgMenu6.setImageResource(R.drawable.tomoro_caramel);
        tvMenu6.setText("Vietnamese Coffee");
        tvPrice6.setText("19k");

        // NON COFFEE (Menu 7-9)
        imgMenu7.setImageResource(R.drawable.tomoro_cokelatjelly);
        tvMenu7.setText("Chocolate Milk");
        tvPrice7.setText("14k");

        imgMenu8.setImageResource(R.drawable.tomoro_pawsome_orange_smoothie);
        tvMenu8.setText("Orange Smoothie");
        tvPrice8.setText("16k");

        imgMenu9.setImageResource(R.drawable.tomoro_matchalatte);
        tvMenu9.setText("Matcha Latte");
        tvPrice9.setText("15k");
    }

    private void loadJanjiJiwaData() {
        imgBanner.setImageResource(R.drawable.logo_janjijiwa);
        tvDescription.setText(
                "Janji Jiwa adalah brand kopi lokal Indonesia yang populer. Menyediakan berbagai varian kopi dengan cita rasa nusantara yang khas. Janji Jiwa memiliki banyak cabang dan selalu berinovasi dengan menu-menu baru.");
        tvRating.setText(" 4.6");
        tvReviewsCount.setText(" (3.1k ulasan)");
        tvAddressSummary.setText("Pondok Indah Mall, Jakarta Selatan");
        tvAvgPrice.setText("Avg. Price: Rp 15k - 30k");
        shopRating   = "4.6";
        shopAvgPrice = "Rp 15k - 30k";
        shopAddress  = "Pondok Indah Mall, Jakarta Selatan";

        // HOT COFFEE (Menu 1-3)
        imgMenu1.setImageResource(R.drawable.janjijiwa_hothazelnut);
        tvMenu1.setText("Hazelnut Latte");
        tvPrice1.setText("13k");

        imgMenu2.setImageResource(R.drawable.janjijiwa_americano);
        tvMenu2.setText("Americano Coffee");
        tvPrice2.setText("14k");

        imgMenu3.setImageResource(R.drawable.janjijiwa_caramel);
        tvMenu3.setText("Caramel Latte");
        tvPrice3.setText("16k");

        // ICE COFFEE (Menu 4-6)
        imgMenu4.setImageResource(R.drawable.janjijiwa_cappucino);
        tvMenu4.setText("Cappucino Latte");
        tvPrice4.setText("15k");

        imgMenu5.setImageResource(R.drawable.janjijiwa_caffelatte);
        tvMenu5.setText("Coffee Latte");
        tvPrice5.setText("18k");

        imgMenu6.setImageResource(R.drawable.janjijiwa_kopisusu);
        tvMenu6.setText("Es Kopi Susu");
        tvPrice6.setText("20k");

        // NON COFFEE (Menu 7-9)
        imgMenu7.setImageResource(R.drawable.janjijiwa_creammatcha);
        tvMenu7.setText("Matcha Latte");
        tvPrice7.setText("17k");

        imgMenu8.setImageResource(R.drawable.janjijiwa_redvelvet);
        tvMenu8.setText("Red Velvet");
        tvPrice8.setText("19k");

        imgMenu9.setImageResource(R.drawable.janjijiwa_cokelat);
        tvMenu9.setText("Chocolate");
        tvPrice9.setText("16k");
    }

    private void loadPointData() {
        imgBanner.setImageResource(R.drawable.logo_point);
        tvDescription.setText(
                "Point Coffee menawarkan kopi specialty dengan biji kopi pilihan. Dikenal dengan barista profesional dan suasana cozy yang nyaman. Point Coffee fokus pada kualitas dan pengalaman minum kopi yang sempurna.");
        tvRating.setText(" 4.8");
        tvReviewsCount.setText(" (2.2k ulasan)");
        tvAddressSummary.setText("Jl. Wahid Hasyim No. 50, Jakarta Pusat");
        tvAvgPrice.setText("Avg. Price: Rp 20k - 40k");
        shopRating   = "4.8";
        shopAvgPrice = "Rp 20k - 40k";
        shopAddress  = "Jl. Wahid Hasyim No. 50, Jakarta Pusat";

        // HOT COFFEE (Menu 1-3)
        imgMenu1.setImageResource(R.drawable.point_hotcappucino);
        tvMenu1.setText("Cappucino Latte");
        tvPrice1.setText("28k");

        imgMenu2.setImageResource(R.drawable.point_hotmarshmallowscoffee);
        tvMenu2.setText("Marshmallow Coffee");
        tvPrice2.setText("30k");

        imgMenu3.setImageResource(R.drawable.point_hotcaramel);
        tvMenu3.setText("Caramel Latte");
        tvPrice3.setText("25k");

        // ICE COFFEE (Menu 4-6)
        imgMenu4.setImageResource(R.drawable.point_caffelatte);
        tvMenu4.setText("Coffee Latte");
        tvPrice4.setText("27k");

        imgMenu5.setImageResource(R.drawable.point_caramel);
        tvMenu5.setText("Caramel Latte");
        tvPrice5.setText("29k");

        imgMenu6.setImageResource(R.drawable.point_palmsugar);
        tvMenu6.setText("Palm Sugar");
        tvPrice6.setText("32k");

        // NON COFFEE (Menu 7-9)
        imgMenu7.setImageResource(R.drawable.point_matcha);
        tvMenu7.setText("Matcha Premium");
        tvPrice7.setText("30k");

        imgMenu8.setImageResource(R.drawable.point_creamandcookies);
        tvMenu8.setText("Cream Cookies");
        tvPrice8.setText("26k");

        imgMenu9.setImageResource(R.drawable.point_creamoreomatcha);
        tvMenu9.setText("Cream Oreo Matcha");
        tvPrice9.setText("28k");
    }

    private void setupListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnOke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Maps Activity
                android.content.Intent intent = new android.content.Intent(CoffeeDetailActivity.this,
                        MapsActivity.class);
                intent.putExtra("shop_name", shopName);

                // We can pass a specific dummy image here based on shopName to show inside the
                // map view
                int imageResId = R.drawable.banner_enjoy;
                String lat = "-6.200000";
                String lng = "106.816666";
                String address = "Pusat Kota";

                if (shopName.equals("Kenangan Coffee")) {
                    imageResId = R.drawable.logo_kopikenangan;
                    lat = "-6.324831";
                    lng = "107.143229";
                    address = "Chadstone Mall Cikarang, Jl. Raya Cikarang - Cibarusah, Bekasi";
                } else if (shopName.equals("Starbucks Coffee")) {
                    imageResId = R.drawable.logo_starbuck;
                    lat = "-6.342360";
                    lng = "107.135450";
                    address = "Citywalk Lippo Cikarang, Jl. MH. Thamrin, Cibatu, Cikarang Selatan, Bekasi";
                } else if (shopName.equals("Tomoro Coffee")) {
                    imageResId = R.drawable.logo_tomoro;
                    lat = "-6.284242";
                    lng = "107.170664";
                    address = "Ruko Hollywood Plaza, Jl. Hollywood Boulevard, Jababeka, Cikarang, Bekasi";
                } else if (shopName.equals("Janji Jiwa Coffee")) {
                    imageResId = R.drawable.logo_janjijiwa;
                    lat = "-6.287232";
                    lng = "107.161129";
                    address = "Jl. Kasuari Raya, Jababeka II, Cikarang Baru, Bekasi";
                } else if (shopName.equals("Point Coffee")) {
                    imageResId = R.drawable.logo_point;
                    lat = "-6.331448";
                    lng = "107.140952";
                    address = "Ruko Easton Commercial Center, Jl. Easton, Cikarang Selatan, Bekasi";
                }

                intent.putExtra("shop_image", imageResId);
                intent.putExtra("shop_lat", lat);
                intent.putExtra("shop_lng", lng);
                intent.putExtra("shop_address", address);

                startActivity(intent);
            }
        });

        btnFavoriteDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CoffeeShop shop = buildCoffeeShop();
                isFavorited = favoriteManager.toggleFavorite(shop);
                refreshFavoriteButton();
                String msg = isFavorited
                        ? shopName + " ditambahkan ke favorit ♥"
                        : shopName + " dihapus dari favorit";
                Toast.makeText(CoffeeDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        // Tulis Review button
        if (btnWriteReview != null) {
            btnWriteReview.setOnClickListener(v -> {
                int logoRes;
                switch (shopName) {
                    case "Starbucks Coffee":  logoRes = R.drawable.logo_starbuck;    break;
                    case "Tomoro Coffee":     logoRes = R.drawable.logo_tomoro;       break;
                    case "Janji Jiwa Coffee": logoRes = R.drawable.logo_janjijiwa;   break;
                    case "Point Coffee":      logoRes = R.drawable.logo_point;        break;
                    default:                  logoRes = R.drawable.logo_kopikenangan; break;
                }
                WriteReviewBottomSheet.newInstance(shopName, logoRes)
                        .show(getSupportFragmentManager(), "WriteReview");
            });
        }

        // All Menu button
        btnAllMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFilter = "all";
                showAllMenus(); // Tampilkan semua menu
                updateButtonStyles();
            }
        });

        // Hot Coffee button
        btnHotCoffee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFilter = "hot";
                filterMenus("hot"); // Filter hanya menu hot
                updateButtonStyles();
            }
        });

        // Ice Coffee button
        btnIceCoffee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFilter = "ice";
                filterMenus("ice"); // Filter hanya menu ice
                updateButtonStyles();
            }
        });

        // Non Coffee button
        btnNonCoffee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFilter = "non";
                filterMenus("non"); // Filter hanya menu non coffee
                updateButtonStyles();
            }
        });
    }

    private void filterMenus(String type) {
        for (int i = 0; i < allMenuCards.size(); i++) {
            CardView card = allMenuCards.get(i);
            String menuType = menuTypes.get(i);

            if (menuType.equals(type)) {
                card.setVisibility(View.VISIBLE);
            } else {
                card.setVisibility(View.GONE);
            }
        }
    }

    private void showAllMenus() {
        for (CardView card : allMenuCards) {
            card.setVisibility(View.VISIBLE);
        }
    }

    private void updateButtonStyles() {
        // Reset semua button ke inactive (outline)
        btnAllMenu.setBackgroundResource(R.drawable.btn_pill_inactive);
        btnHotCoffee.setBackgroundResource(R.drawable.btn_pill_inactive);
        btnIceCoffee.setBackgroundResource(R.drawable.btn_pill_inactive);
        btnNonCoffee.setBackgroundResource(R.drawable.btn_pill_inactive);

        // Hapus background tint saat inactive
        btnAllMenu.setBackgroundTintList(null);
        btnHotCoffee.setBackgroundTintList(null);
        btnIceCoffee.setBackgroundTintList(null);
        btnNonCoffee.setBackgroundTintList(null);

        btnAllMenu.setTextColor(themeColor);
        btnHotCoffee.setTextColor(themeColor);
        btnIceCoffee.setTextColor(themeColor);
        btnNonCoffee.setTextColor(themeColor);

        // Highlight button yang aktif (filled pill)
        if (currentFilter.equals("all")) {
            btnAllMenu.setBackgroundResource(R.drawable.btn_pill_active);
            btnAllMenu.setBackgroundTintList(ColorStateList.valueOf(themeColor));
            btnAllMenu.setTextColor(themeTextColor);
        } else if (currentFilter.equals("hot")) {
            btnHotCoffee.setBackgroundResource(R.drawable.btn_pill_active);
            btnHotCoffee.setBackgroundTintList(ColorStateList.valueOf(themeColor));
            btnHotCoffee.setTextColor(themeTextColor);
        } else if (currentFilter.equals("ice")) {
            btnIceCoffee.setBackgroundResource(R.drawable.btn_pill_active);
            btnIceCoffee.setBackgroundTintList(ColorStateList.valueOf(themeColor));
            btnIceCoffee.setTextColor(themeTextColor);
        } else if (currentFilter.equals("non")) {
            btnNonCoffee.setBackgroundResource(R.drawable.btn_pill_active);
            btnNonCoffee.setBackgroundTintList(ColorStateList.valueOf(themeColor));
            btnNonCoffee.setTextColor(themeTextColor);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /** Callback dari WriteReviewBottomSheet setelah review berhasil disimpan */
    @Override
    public void onSubmitted(Review review) {
        refreshReviewButton();
        renderReviewsOnDetail(); // langsung tampilkan ulasan baru
        Toast.makeText(this, "Ulasan tersimpan! ☕", Toast.LENGTH_SHORT).show();
    }

    /**
     * Membangun objek CoffeeShop dari data yang sudah dimuat di loadShopData().
     */
    private CoffeeShop buildCoffeeShop() {
        int logoRes;
        switch (shopName) {
            case "Starbucks Coffee":  logoRes = R.drawable.logo_starbuck;    break;
            case "Tomoro Coffee":     logoRes = R.drawable.logo_tomoro;       break;
            case "Janji Jiwa Coffee": logoRes = R.drawable.logo_janjijiwa;   break;
            case "Point Coffee":      logoRes = R.drawable.logo_point;        break;
            default:                  logoRes = R.drawable.logo_kopikenangan; break;
        }
        return new CoffeeShop(shopName, logoRes, shopRating, shopAvgPrice, shopAddress);
    }

    /**
     * Perbarui tampilan tombol Favorite:
     * - sudah favorit → solid themeColor, teks "Favorit ♥"
     * - belum         → outline, teks "Favorit"
     */
    private void refreshFavoriteButton() {
        if (btnFavoriteDetail == null) return;
        isFavorited = favoriteManager.isFavorite(shopName);
        if (isFavorited) {
            btnFavoriteDetail.setBackgroundTintList(ColorStateList.valueOf(themeColor));
            btnFavoriteDetail.setTextColor(themeTextColor);
            btnFavoriteDetail.setText("Favorit ♥");
        } else {
            btnFavoriteDetail.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            btnFavoriteDetail.setTextColor(themeColor);
            btnFavoriteDetail.setText("Favorit");
            btnFavoriteDetail.setBackgroundResource(R.drawable.btn_pill_inactive);
        }
    }

    /**
     * Perbarui tampilan tombol Tulis Review:
     * - sudah pernah review → "Edit Ulasan ✏"
     * - belum               → "Tulis Ulasan"
     */
    private void refreshReviewButton() {
        if (btnWriteReview == null) return;
        boolean hasReview = reviewManager.hasReviewedShop(shopName);
        btnWriteReview.setText(hasReview ? "Edit Ulasan ✏" : "Tulis Ulasan");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Render ulasan di halaman detail (dinamis ke LinearLayout)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Memuat dan menampilkan semua ulasan untuk coffee shop ini secara dinamis
     * ke dalam containerReviewsDetail (LinearLayout di dalam ScrollView).
     */
    private void renderReviewsOnDetail() {
        if (containerReviewsDetail == null || shopName == null) return;

        // Hanya tampilkan ulasan untuk shop ini
        List<Review> allReviews = reviewManager.getReviews();
        List<Review> shopReviews = new ArrayList<>();
        for (Review r : allReviews) {
            if (r.getShopName().equals(shopName)) {
                shopReviews.add(r);
            }
        }

        // Bersihkan container sebelum render ulang
        containerReviewsDetail.removeAllViews();

        if (shopReviews.isEmpty()) {
            containerReviewsDetail.setVisibility(View.GONE);
            layoutNoReview.setVisibility(View.VISIBLE);
            tvReviewCountDetail.setText("");
        } else {
            layoutNoReview.setVisibility(View.GONE);
            containerReviewsDetail.setVisibility(View.VISIBLE);
            tvReviewCountDetail.setText(shopReviews.size() + " ulasan");

            for (Review review : shopReviews) {
                View card = buildReviewCardView(review);
                containerReviewsDetail.addView(card);
            }
        }
    }

    /**
     * Membuat tampilan card untuk satu ulasan secara programmatic.
     */
    private View buildReviewCardView(Review review) {
        // Inflate item_review.xml
        View card = LayoutInflater.from(this)
                .inflate(R.layout.item_review, containerReviewsDetail, false);

        // Isi data
        ImageView imgLogo    = card.findViewById(R.id.img_review_logo);
        TextView  tvUsername = card.findViewById(R.id.tv_review_username);
        TextView  tvShop     = card.findViewById(R.id.tv_review_shop_name);
        TextView  tvComment  = card.findViewById(R.id.tv_review_comment);
        TextView  tvDate     = card.findViewById(R.id.tv_review_date);
        ImageView btnDelete  = card.findViewById(R.id.btn_delete_review);
        ImageView star1      = card.findViewById(R.id.star_1);
        ImageView star2      = card.findViewById(R.id.star_2);
        ImageView star3      = card.findViewById(R.id.star_3);
        ImageView star4      = card.findViewById(R.id.star_4);
        ImageView star5      = card.findViewById(R.id.star_5);

        // Pakai avatar placeholder (logo shop redundan — kita sudah di halaman shop itu)
        imgLogo.setImageResource(R.drawable.ic_avatar_placeholder);
        imgLogo.setColorFilter(getResources().getColor(R.color.light_brown, getTheme()));

        // Nama user (bold, primer)
        tvUsername.setText(review.getUserName() != null && !review.getUserName().isEmpty()
                ? review.getUserName() : "Pengguna");
        // Nama coffee shop disembunyikan di halaman detail (sudah tahu shop-nya)
        tvShop.setVisibility(View.GONE);

        tvComment.setText(review.getComment());
        tvDate.setText(review.getDate());


        // Render bintang
        int activeColor   = Color.parseColor("#8B4513");
        int inactiveColor = Color.parseColor("#E0E0E0");
        ImageView[] stars = {star1, star2, star3, star4, star5};
        for (int i = 0; i < stars.length; i++) {
            stars[i].setColorFilter(i < review.getRating() ? activeColor : inactiveColor);
        }

        // Tombol hapus → hapus ulasan dan refresh tampilan
        btnDelete.setOnClickListener(v -> {
            reviewManager.removeReviewForShop(review.getShopName());
            renderReviewsOnDetail();
            refreshReviewButton();
            Toast.makeText(this, "Ulasan dihapus", Toast.LENGTH_SHORT).show();
        });

        // Margin bawah antar card
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);

        return card;
    }
}