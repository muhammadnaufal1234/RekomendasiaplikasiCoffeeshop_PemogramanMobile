package com.example.monokromcoffee;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.ArrayList;
import java.util.List;

public class CoffeeDetailActivity extends AppCompatActivity {

    private ImageView btnBack, imgBanner;
    private TextView tvShopName, tvDescription;
    private Button btnAllMenu, btnHotCoffee, btnIceCoffee, btnNonCoffee, btnCancel, btnOke;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        // Initialize menu lists
        initializeMenuLists();

        // Load shop data
        loadShopData();

        // Setup listeners
        setupListeners();

        // Set default button state (all shown)
        updateButtonStyles();
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

        // Menu cards
        cardMenu1 = findViewById(R.id.card_menu_1);
        cardMenu2 = findViewById(R.id.card_menu_2);
        cardMenu3 = findViewById(R.id.card_menu_3);
        cardMenu4 = findViewById(R.id.card_menu_4);
        cardMenu5 = findViewById(R.id.card_menu_5);
        cardMenu6 = findViewById(R.id.card_menu_6);
        cardMenu7 = findViewById(R.id.card_menu_7);
        cardMenu8 = findViewById(R.id.card_menu_8);
        cardMenu9 = findViewById(R.id.card_menu_9);

        // Menu items
        imgMenu1 = findViewById(R.id.img_menu_1);
        imgMenu2 = findViewById(R.id.img_menu_2);
        imgMenu3 = findViewById(R.id.img_menu_3);
        imgMenu4 = findViewById(R.id.img_menu_4);
        imgMenu5 = findViewById(R.id.img_menu_5);
        imgMenu6 = findViewById(R.id.img_menu_6);
        imgMenu7 = findViewById(R.id.img_menu_7);
        imgMenu8 = findViewById(R.id.img_menu_8);
        imgMenu9 = findViewById(R.id.img_menu_9);

        tvMenu1 = findViewById(R.id.tv_menu_1);
        tvMenu2 = findViewById(R.id.tv_menu_2);
        tvMenu3 = findViewById(R.id.tv_menu_3);
        tvMenu4 = findViewById(R.id.tv_menu_4);
        tvMenu5 = findViewById(R.id.tv_menu_5);
        tvMenu6 = findViewById(R.id.tv_menu_6);
        tvMenu7 = findViewById(R.id.tv_menu_7);
        tvMenu8 = findViewById(R.id.tv_menu_8);
        tvMenu9 = findViewById(R.id.tv_menu_9);

        tvPrice1 = findViewById(R.id.tv_price_1);
        tvPrice2 = findViewById(R.id.tv_price_2);
        tvPrice3 = findViewById(R.id.tv_price_3);
        tvPrice4 = findViewById(R.id.tv_price_4);
        tvPrice5 = findViewById(R.id.tv_price_5);
        tvPrice6 = findViewById(R.id.tv_price_6);
        tvPrice7 = findViewById(R.id.tv_price_7);
        tvPrice8 = findViewById(R.id.tv_price_8);
        tvPrice9 = findViewById(R.id.tv_price_9);
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
        menuTypes.add("hot");   // Menu 1 - Caramel Latte (Hot)
        menuTypes.add("hot");   // Menu 2 - Kopi Kenangan (Hot)
        menuTypes.add("hot");   // Menu 3 - Mantan Caramel (Hot)
        menuTypes.add("ice");   // Menu 4 - Mocha Coffee (Ice)
        menuTypes.add("ice");   // Menu 5 - Manischoco (Ice)
        menuTypes.add("ice");   // Menu 6 - Avocado Coffee (Ice)
        menuTypes.add("non");   // Menu 7 - Creamy Mancioco (Non Coffee)
        menuTypes.add("non");   // Menu 8 - Latte (Non Coffee)
        menuTypes.add("non");   // Menu 9 - Americano (Non Coffee)
    }

    private void loadShopData() {
        if (shopName == null) {
            finish();
            return;
        }

        tvShopName.setText(shopName);

        switch (shopName) {
            case "Kenangan Coffee":
                loadKenanganData();
                break;
            case "Starbucks Coffee":
                loadStarbucksData();
                break;
            case "Tomoro Coffee":
                loadTomoroData();
                break;
            case "Janji Jiwa Coffee":
                loadJanjiJiwaData();
                break;
            case "Point Coffee":
                loadPointData();
                break;
        }
    }

    private void loadKenanganData() {
        imgBanner.setImageResource(R.mipmap.ic_launcher);
        tvDescription.setText("Kenangan Coffee adalah coffee shop modern dengan konsep grab and go. Menyajikan berbagai jenis kopi berkualitas dengan harga terjangkau. Kenangan Coffee memiliki banyak cabang di Indonesia dan selalu ramai dikunjungi.");

        // HOT COFFEE (Menu 1-3)
        imgMenu1.setImageResource(R.mipmap.ic_launcher);
        tvMenu1.setText("Caramel Latte");
        tvPrice1.setText("20k");

        imgMenu2.setImageResource(R.mipmap.ic_launcher);
        tvMenu2.setText("Kopi Kenangan Mantan");
        tvPrice2.setText("15k");

        imgMenu3.setImageResource(R.mipmap.ic_launcher);
        tvMenu3.setText("Kopi Susu Tetangga");
        tvPrice3.setText("18k");

        // ICE COFFEE (Menu 4-6)
        imgMenu4.setImageResource(R.mipmap.ic_launcher);
        tvMenu4.setText("Es Kopi Susu Kenangan");
        tvPrice4.setText("18k");

        imgMenu5.setImageResource(R.mipmap.ic_launcher);
        tvMenu5.setText("Iced Mixchoco");
        tvPrice5.setText("20k");

        imgMenu6.setImageResource(R.mipmap.ic_launcher);
        tvMenu6.setText("Avocado Coffee");
        tvPrice6.setText("23k");

        // NON COFFEE (Menu 7-9)
        imgMenu7.setImageResource(R.mipmap.ic_launcher);
        tvMenu7.setText("Red Velvet Latte");
        tvPrice7.setText("22k");

        imgMenu8.setImageResource(R.mipmap.ic_launcher);
        tvMenu8.setText("Matcha Latte");
        tvPrice8.setText("21k");

        imgMenu9.setImageResource(R.mipmap.ic_launcher);
        tvMenu9.setText("Chocolate");
        tvPrice9.setText("19k");
    }

    private void loadStarbucksData() {
        imgBanner.setImageResource(R.mipmap.ic_launcher);
        tvDescription.setText("Starbucks adalah jaringan kedai kopi terbesar di dunia yang berasal dari Seattle, Amerika Serikat. Terkenal dengan kualitas kopi premium dan suasana nyaman untuk bekerja atau bersantai. Starbucks menyajikan berbagai varian kopi dan minuman lainnya.");

        // HOT COFFEE (Menu 1-3)
        imgMenu1.setImageResource(R.mipmap.ic_launcher);
        tvMenu1.setText("Caffe Americano");
        tvPrice1.setText("35k");

        imgMenu2.setImageResource(R.mipmap.ic_launcher);
        tvMenu2.setText("Cappuccino");
        tvPrice2.setText("40k");

        imgMenu3.setImageResource(R.mipmap.ic_launcher);
        tvMenu3.setText("Caffe Latte");
        tvPrice3.setText("42k");

        // ICE COFFEE (Menu 4-6)
        imgMenu4.setImageResource(R.mipmap.ic_launcher);
        tvMenu4.setText("Iced Caramel Macchiato");
        tvPrice4.setText("50k");

        imgMenu5.setImageResource(R.mipmap.ic_launcher);
        tvMenu5.setText("Iced Mocha");
        tvPrice5.setText("48k");

        imgMenu6.setImageResource(R.mipmap.ic_launcher);
        tvMenu6.setText("Cold Brew");
        tvPrice6.setText("45k");

        // NON COFFEE (Menu 7-9)
        imgMenu7.setImageResource(R.mipmap.ic_launcher);
        tvMenu7.setText("Green Tea Latte");
        tvPrice7.setText("44k");

        imgMenu8.setImageResource(R.mipmap.ic_launcher);
        tvMenu8.setText("Chocolate Frappuccino");
        tvPrice8.setText("52k");

        imgMenu9.setImageResource(R.mipmap.ic_launcher);
        tvMenu9.setText("Vanilla Bean");
        tvPrice9.setText("46k");
    }

    private void loadTomoroData() {
        imgBanner.setImageResource(R.mipmap.ic_launcher);
        tvDescription.setText("Tomoro Coffee menawarkan kopi berkualitas tinggi dengan harga terjangkau. Fokus pada kualitas biji kopi pilihan dan pelayanan cepat. Tomoro Coffee memiliki konsep modern dan nyaman untuk bersantai atau bekerja.");

        // HOT COFFEE (Menu 1-3)
        imgMenu1.setImageResource(R.mipmap.ic_launcher);
        tvMenu1.setText("Kopi Hitam");
        tvPrice1.setText("12k");

        imgMenu2.setImageResource(R.mipmap.ic_launcher);
        tvMenu2.setText("Espresso");
        tvPrice2.setText("15k");

        imgMenu3.setImageResource(R.mipmap.ic_launcher);
        tvMenu3.setText("Cafe Latte");
        tvPrice3.setText("18k");

        // ICE COFFEE (Menu 4-6)
        imgMenu4.setImageResource(R.mipmap.ic_launcher);
        tvMenu4.setText("Es Kopi Susu Gula Aren");
        tvPrice4.setText("17k");

        imgMenu5.setImageResource(R.mipmap.ic_launcher);
        tvMenu5.setText("Iced Americano");
        tvPrice5.setText("16k");

        imgMenu6.setImageResource(R.mipmap.ic_launcher);
        tvMenu6.setText("Vietnamese Coffee");
        tvPrice6.setText("19k");

        // NON COFFEE (Menu 7-9)
        imgMenu7.setImageResource(R.mipmap.ic_launcher);
        tvMenu7.setText("Teh Tarik");
        tvPrice7.setText("14k");

        imgMenu8.setImageResource(R.mipmap.ic_launcher);
        tvMenu8.setText("Chocolate Milk");
        tvPrice8.setText("16k");

        imgMenu9.setImageResource(R.mipmap.ic_launcher);
        tvMenu9.setText("Fresh Lemon Tea");
        tvPrice9.setText("15k");
    }

    private void loadJanjiJiwaData() {
        imgBanner.setImageResource(R.mipmap.ic_launcher);
        tvDescription.setText("Janji Jiwa adalah brand kopi lokal Indonesia yang populer. Menyediakan berbagai varian kopi dengan cita rasa nusantara yang khas. Janji Jiwa memiliki banyak cabang dan selalu berinovasi dengan menu-menu baru.");

        // HOT COFFEE (Menu 1-3)
        imgMenu1.setImageResource(R.mipmap.ic_launcher);
        tvMenu1.setText("Jiwa Jago");
        tvPrice1.setText("13k");

        imgMenu2.setImageResource(R.mipmap.ic_launcher);
        tvMenu2.setText("Kopi Susu Tetangga");
        tvPrice2.setText("14k");

        imgMenu3.setImageResource(R.mipmap.ic_launcher);
        tvMenu3.setText("Cappuccino Cincau");
        tvPrice3.setText("16k");

        // ICE COFFEE (Menu 4-6)
        imgMenu4.setImageResource(R.mipmap.ic_launcher);
        tvMenu4.setText("Es Kopi Susu Gula Aren");
        tvPrice4.setText("15k");

        imgMenu5.setImageResource(R.mipmap.ic_launcher);
        tvMenu5.setText("Kopi Susu Caramel");
        tvPrice5.setText("18k");

        imgMenu6.setImageResource(R.mipmap.ic_launcher);
        tvMenu6.setText("Es Kopi Alpukat");
        tvPrice6.setText("20k");

        // NON COFFEE (Menu 7-9)
        imgMenu7.setImageResource(R.mipmap.ic_launcher);
        tvMenu7.setText("Taro Latte");
        tvPrice7.setText("17k");

        imgMenu8.setImageResource(R.mipmap.ic_launcher);
        tvMenu8.setText("Red Velvet");
        tvPrice8.setText("19k");

        imgMenu9.setImageResource(R.mipmap.ic_launcher);
        tvMenu9.setText("Thai Tea");
        tvPrice9.setText("16k");
    }

    private void loadPointData() {
        imgBanner.setImageResource(R.mipmap.ic_launcher);
        tvDescription.setText("Point Coffee menawarkan kopi specialty dengan biji kopi pilihan. Dikenal dengan barista profesional dan suasana cozy yang nyaman. Point Coffee fokus pada kualitas dan pengalaman minum kopi yang sempurna.");

        // HOT COFFEE (Menu 1-3)
        imgMenu1.setImageResource(R.mipmap.ic_launcher);
        tvMenu1.setText("Single Origin");
        tvPrice1.setText("28k");

        imgMenu2.setImageResource(R.mipmap.ic_launcher);
        tvMenu2.setText("Flat White");
        tvPrice2.setText("30k");

        imgMenu3.setImageResource(R.mipmap.ic_launcher);
        tvMenu3.setText("Piccolo Latte");
        tvPrice3.setText("25k");

        // ICE COFFEE (Menu 4-6)
        imgMenu4.setImageResource(R.mipmap.ic_launcher);
        tvMenu4.setText("Iced Long Black");
        tvPrice4.setText("27k");

        imgMenu5.setImageResource(R.mipmap.ic_launcher);
        tvMenu5.setText("Iced Latte");
        tvPrice5.setText("29k");

        imgMenu6.setImageResource(R.mipmap.ic_launcher);
        tvMenu6.setText("Affogato");
        tvPrice6.setText("32k");

        // NON COFFEE (Menu 7-9)
        imgMenu7.setImageResource(R.mipmap.ic_launcher);
        tvMenu7.setText("Matcha Premium");
        tvPrice7.setText("30k");

        imgMenu8.setImageResource(R.mipmap.ic_launcher);
        tvMenu8.setText("Hot Chocolate");
        tvPrice8.setText("26k");

        imgMenu9.setImageResource(R.mipmap.ic_launcher);
        tvMenu9.setText("Lemon Yuzu");
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
                finish();
            }
        });

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
        // Reset semua button ke style default
        btnAllMenu.setBackgroundResource(R.drawable.btn_brown);
        btnHotCoffee.setBackgroundResource(R.drawable.btn_brown);
        btnIceCoffee.setBackgroundResource(R.drawable.btn_brown);
        btnNonCoffee.setBackgroundResource(R.drawable.btn_brown);

        btnAllMenu.setTextColor(Color.WHITE);
        btnHotCoffee.setTextColor(Color.WHITE);
        btnIceCoffee.setTextColor(Color.WHITE);
        btnNonCoffee.setTextColor(Color.WHITE);

        // Highlight button yang aktif dengan warna lebih gelap
        if (currentFilter.equals("all")) {
            btnAllMenu.setBackgroundColor(Color.parseColor("#5D4037")); // Dark brown
            btnAllMenu.setTextColor(Color.WHITE);
        } else if (currentFilter.equals("hot")) {
            btnHotCoffee.setBackgroundColor(Color.parseColor("#5D4037")); // Dark brown
            btnHotCoffee.setTextColor(Color.WHITE);
        } else if (currentFilter.equals("ice")) {
            btnIceCoffee.setBackgroundColor(Color.parseColor("#5D4037")); // Dark brown
            btnIceCoffee.setTextColor(Color.WHITE);
        } else if (currentFilter.equals("non")) {
            btnNonCoffee.setBackgroundColor(Color.parseColor("#5D4037")); // Dark brown
            btnNonCoffee.setTextColor(Color.WHITE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}