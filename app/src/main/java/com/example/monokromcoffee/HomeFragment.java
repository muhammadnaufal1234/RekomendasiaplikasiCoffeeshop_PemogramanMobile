package com.example.monokromcoffee;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    // UI Components
    private ImageView btnClearSearch;
    private TextView tvUsername, tvNoResults;
    private SharedPreferences prefs;
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
    private String username = "monokrom coffee";
    private List<CardView> allCards;
    private List<String> coffeeNames;
    private int visibleCardsCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        prefs = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);

        // Get username from activity intent if available
        if (requireActivity().getIntent() != null && requireActivity().getIntent().hasExtra("username")) {
            username = requireActivity().getIntent().getStringExtra("username");
        }

        initializeViews(view);
        initializeCoffeeList();
        setupBannerSlider(view);
        setupSearch();
        setupListeners();
        displayUsername();

        return view;
    }

    private void initializeViews(View view) {
        tvUsername = view.findViewById(R.id.tv_username);

        etSearch = view.findViewById(R.id.et_search);
        btnClearSearch = view.findViewById(R.id.btn_clear_search);
        tvNoResults = view.findViewById(R.id.tv_no_results);

        bannerSlider = view.findViewById(R.id.banner_slider);
        dotsIndicator = view.findViewById(R.id.dots_indicator);

        coffeeListContainer = view.findViewById(R.id.coffee_list_container);

        cardKenangan = view.findViewById(R.id.card_kenangan);
        cardStarbucks = view.findViewById(R.id.card_starbucks);
        cardTomoro = view.findViewById(R.id.card_tomoro);
        cardJanjiJiwa = view.findViewById(R.id.card_janjijiwa);
        cardPoint = view.findViewById(R.id.card_point);

        btnDescKenangan = view.findViewById(R.id.btn_desc_kenangan);
        btnDescStarbucks = view.findViewById(R.id.btn_desc_starbucks);
        btnDescTomoro = view.findViewById(R.id.btn_desc_tomoro);
        btnDescJanjiJiwa = view.findViewById(R.id.btn_desc_janjijiwa);
        btnDescPoint = view.findViewById(R.id.btn_desc_point);
    }

    private void initializeCoffeeList() {
        allCards = new ArrayList<>();
        allCards.add(cardKenangan);
        allCards.add(cardStarbucks);
        allCards.add(cardTomoro);
        allCards.add(cardJanjiJiwa);
        allCards.add(cardPoint);

        coffeeNames = new ArrayList<>();
        coffeeNames.add("kenangan coffee");
        coffeeNames.add("starbucks coffee");
        coffeeNames.add("tomoro coffee");
        coffeeNames.add("janji jiwa coffee");
        coffeeNames.add("point coffee");

        visibleCardsCount = allCards.size();
    }

    private void setupBannerSlider(View view) {
        bannerImages = new ArrayList<>();
        bannerImages.add(R.drawable.banner_enjoy);
        bannerImages.add(R.drawable.banner_hello);
        bannerImages.add(R.drawable.banner_today);

        sliderAdapter = new BannerSliderAdapter(bannerImages);
        bannerSlider.setAdapter(sliderAdapter);

        setupDotsIndicator(0);

        bannerSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setupDotsIndicator(position);
            }
        });

        setupAutoSlide();
    }

    private void setupDotsIndicator(int position) {
        if (dotsIndicator == null || requireContext() == null) return;
        dotsIndicator.removeAllViews();

        ImageView[] dots = new ImageView[bannerImages.size()];

        for (int i = 0; i < bannerImages.size(); i++) {
            dots[i] = new ImageView(requireContext());
            dots[i].setImageResource(R.drawable.dot_inactive);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
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
                if (bannerSlider != null) {
                    int currentItem = bannerSlider.getCurrentItem();
                    int nextItem = (currentItem + 1) % bannerImages.size();
                    bannerSlider.setCurrentItem(nextItem);
                    sliderHandler.postDelayed(this, 3000);
                }
            }
        };
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    private void setupSearch() {
        if (etSearch == null) return;
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

        if (btnClearSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                etSearch.setText("");
                showAllCoffeeShops();
                hideKeyboard();
                etSearch.clearFocus();
            });
        }
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
            if (card != null) card.setVisibility(View.VISIBLE);
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
        if (requireActivity().getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(requireActivity().getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    private void setupListeners() {
        if (cardKenangan != null) cardKenangan.setOnClickListener(v -> openCoffeeDetail("Kenangan Coffee"));
        if (btnDescKenangan != null) btnDescKenangan.setOnClickListener(v -> openCoffeeDetail("Kenangan Coffee"));

        if (cardStarbucks != null) cardStarbucks.setOnClickListener(v -> openCoffeeDetail("Starbucks Coffee"));
        if (btnDescStarbucks != null) btnDescStarbucks.setOnClickListener(v -> openCoffeeDetail("Starbucks Coffee"));

        if (cardTomoro != null) cardTomoro.setOnClickListener(v -> openCoffeeDetail("Tomoro Coffee"));
        if (btnDescTomoro != null) btnDescTomoro.setOnClickListener(v -> openCoffeeDetail("Tomoro Coffee"));

        if (cardJanjiJiwa != null) cardJanjiJiwa.setOnClickListener(v -> openCoffeeDetail("Janji Jiwa Coffee"));
        if (btnDescJanjiJiwa != null) btnDescJanjiJiwa.setOnClickListener(v -> openCoffeeDetail("Janji Jiwa Coffee"));

        if (cardPoint != null) cardPoint.setOnClickListener(v -> openCoffeeDetail("Point Coffee"));
        if (btnDescPoint != null) btnDescPoint.setOnClickListener(v -> openCoffeeDetail("Point Coffee"));
    }

    private void openCoffeeDetail(String shopName) {
        try {
            Intent intent = new Intent(requireActivity(), CoffeeDetailActivity.class);
            intent.putExtra("shop_name", shopName);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void displayUsername() {
        if (tvUsername != null) {
            tvUsername.setText(username);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.postDelayed(sliderRunnable, 3000);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }
}
