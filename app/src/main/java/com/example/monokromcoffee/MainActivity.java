package com.example.monokromcoffee;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.viewpager2.widget.ViewPager2;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private ImageView btnBack, btnClearSearch;
    private TextView tvUsername, tvNoResults;
    private EditText etSearch;

    // Banner Slider
    private ViewPager2 bannerSlider;
    private LinearLayout dotsIndicator;
    private BannerSliderAdapter sliderAdapter;
    private List<Integer> bannerImages;
    private Handler sliderHandler;
    private Runnable sliderRunnable;

    // Coffee Shop Cards
    private CardView cardKenangan, cardStarbucks, cardTomoro, cardJanjiJiwa, cardPoint;
    private LinearLayout coffeeListContainer;

    // Description Buttons
    private Button btnDescKenangan, btnDescStarbucks, btnDescTomoro, btnDescJanjiJiwa, btnDescPoint;

    // Data untuk Search
    private String username = "Kenangan";
    private List<CardView> allCards;
    private List<String> coffeeNames;
    private int visibleCardsCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_main);

        // Get username from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("username")) {
            username = intent.getStringExtra("username");
        }

        // Initialize views
        initializeViews();

        // Initialize coffee list untuk search
        initializeCoffeeList();

        // Setup banner slider
        setupBannerSlider();

        // Setup search
        setupSearch();

        // Setup listeners
        setupListeners();

        // Display username
        displayUsername();
    }

    private void initializeViews() {
        // Header components
        tvUsername = findViewById(R.id.tv_username);

        // Search components
        etSearch = findViewById(R.id.et_search);
        btnClearSearch = findViewById(R.id.btn_clear_search);
        tvNoResults = findViewById(R.id.tv_no_results);

        // Banner slider
        bannerSlider = findViewById(R.id.banner_slider);
        dotsIndicator = findViewById(R.id.dots_indicator);

        // Coffee list container
        coffeeListContainer = findViewById(R.id.coffee_list_container);

        // Coffee shop cards
        cardKenangan = findViewById(R.id.card_kenangan);
        cardStarbucks = findViewById(R.id.card_starbucks);
        cardTomoro = findViewById(R.id.card_tomoro);
        cardJanjiJiwa = findViewById(R.id.card_janjijiwa);
        cardPoint = findViewById(R.id.card_point);

        // Description buttons
        btnDescKenangan = findViewById(R.id.btn_desc_kenangan);
        btnDescStarbucks = findViewById(R.id.btn_desc_starbucks);
        btnDescTomoro = findViewById(R.id.btn_desc_tomoro);
        btnDescJanjiJiwa = findViewById(R.id.btn_desc_janjijiwa);
        btnDescPoint = findViewById(R.id.btn_desc_point);
    }

    private void initializeCoffeeList() {
        // Simpan semua cards dalam list
        allCards = new ArrayList<>();
        allCards.add(cardKenangan);
        allCards.add(cardStarbucks);
        allCards.add(cardTomoro);
        allCards.add(cardJanjiJiwa);
        allCards.add(cardPoint);

        // Simpan nama coffee shops (lowercase untuk search)
        coffeeNames = new ArrayList<>();
        coffeeNames.add("kenangan coffee");
        coffeeNames.add("starbucks coffee");
        coffeeNames.add("tomoro coffee");
        coffeeNames.add("janji jiwa coffee");
        coffeeNames.add("point coffee");

        visibleCardsCount = allCards.size();
    }

    private void setupBannerSlider() {
        // Add banner images
        bannerImages = new ArrayList<>();
        bannerImages.add(R.mipmap.ic_launcher);
        bannerImages.add(R.mipmap.ic_launcher);
        bannerImages.add(R.mipmap.ic_launcher);

        // Setup adapter
        sliderAdapter = new BannerSliderAdapter(bannerImages);
        bannerSlider.setAdapter(sliderAdapter);

        // Setup dots indicator
        setupDotsIndicator(0);

        // ViewPager2 page change callback
        bannerSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setupDotsIndicator(position);
            }
        });

        // Auto slide banner
        setupAutoSlide();
    }

    private void setupDotsIndicator(int position) {
        dotsIndicator.removeAllViews();

        ImageView[] dots = new ImageView[bannerImages.size()];

        for (int i = 0; i < bannerImages.size(); i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageResource(R.drawable.dot_inactive);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);

            dotsIndicator.addView(dots[i], params);
        }

        if (dots.length > 0) {
            dots[position].setImageResource(R.drawable.dot_active);
        }
    }

    private void setupAutoSlide() {
        sliderHandler = new Handler(Looper.getMainLooper());
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = bannerSlider.getCurrentItem();
                int nextItem = (currentItem + 1) % bannerImages.size();
                bannerSlider.setCurrentItem(nextItem);
                sliderHandler.postDelayed(this, 3000);
            }
        };
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                if (query.length() > 0) {
                    btnClearSearch.setVisibility(View.VISIBLE);
                } else {
                    btnClearSearch.setVisibility(View.GONE);
                }

                filterCoffeeShops(query);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnClearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearch.setText("");
                showAllCoffeeShops();
                hideKeyboard();
                etSearch.clearFocus();
            }
        });
    }

    private void filterCoffeeShops(String query) {
        if (query.isEmpty()) {
            showAllCoffeeShops();
            return;
        }

        String lowerQuery = query.toLowerCase().trim();
        visibleCardsCount = 0;

        for (int i = 0; i < coffeeNames.size(); i++) {
            String coffeeName = coffeeNames.get(i);
            CardView card = allCards.get(i);

            if (coffeeName.contains(lowerQuery)) {
                card.setVisibility(View.VISIBLE);
                visibleCardsCount++;
            } else {
                card.setVisibility(View.GONE);
            }
        }

        updateNoResultsMessage(query);
    }

    private void showAllCoffeeShops() {
        for (CardView card : allCards) {
            card.setVisibility(View.VISIBLE);
        }
        visibleCardsCount = allCards.size();

        if (tvNoResults != null) {
            tvNoResults.setVisibility(View.GONE);
        }
    }

    private void updateNoResultsMessage(String query) {
        if (tvNoResults != null) {
            if (visibleCardsCount == 0) {
                tvNoResults.setText("Tidak ditemukan coffee shop untuk \"" + query + "\"");
                tvNoResults.setVisibility(View.VISIBLE);
            } else {
                tvNoResults.setVisibility(View.GONE);
            }
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void setupListeners() {
        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        // Kenangan Coffee
        if (cardKenangan != null) {
            cardKenangan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openCoffeeDetail("Kenangan Coffee");
                }
            });
        }

        if (btnDescKenangan != null) {
            btnDescKenangan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openCoffeeDetail("Kenangan Coffee");
                }
            });
        }

        // Starbucks Coffee
        if (cardStarbucks != null) {
            cardStarbucks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openCoffeeDetail("Starbucks Coffee");
                }
            });
        }

        if (btnDescStarbucks != null) {
            btnDescStarbucks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openCoffeeDetail("Starbucks Coffee");
                }
            });
        }

        // Tomoro Coffee
        if (cardTomoro != null) {
            cardTomoro.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openCoffeeDetail("Tomoro Coffee");
                }
            });
        }

        if (btnDescTomoro != null) {
            btnDescTomoro.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openCoffeeDetail("Tomoro Coffee");
                }
            });
        }

        // Janji Jiwa Coffee
        if (cardJanjiJiwa != null) {
            cardJanjiJiwa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openCoffeeDetail("Janji Jiwa Coffee");
                }
            });
        }

        if (btnDescJanjiJiwa != null) {
            btnDescJanjiJiwa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openCoffeeDetail("Janji Jiwa Coffee");
                }
            });
        }

        // Point Coffee
        if (cardPoint != null) {
            cardPoint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openCoffeeDetail("Point Coffee");
                }
            });
        }

        if (btnDescPoint != null) {
            btnDescPoint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openCoffeeDetail("Point Coffee");
                }
            });
        }
    }

    private void openCoffeeDetail(String shopName) {
        try {
            Intent intent = new Intent(MainActivity.this, CoffeeDetailActivity.class);
            intent.putExtra("shop_name", shopName);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void displayUsername() {
        if (tvUsername != null) {
            tvUsername.setText(username + "✨");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.postDelayed(sliderRunnable, 3000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}