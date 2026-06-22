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

    // Filter Chips
    private TextView chipSemua, chipTerdekat, chipRating, chipPopuler, chipBaru;
    private String activeChip = "semua";

    // Rekomendasi Menu Cards
    private CardView cardMenuCaramel, cardMenuKopiSusu, cardMenuOatLatte;

    // Data untuk Search
    private String username = "monokrom coffee";
    private List<CardView> allCards;
    private List<String> coffeeNames;
    // Rating per card (sesuai urutan allCards)
    private final double[] coffeeRatings = {4.9, 5.0, 3.9, 4.8, 4.6};
    private int visibleCardsCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        prefs = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);

        if (requireActivity().getIntent() != null && requireActivity().getIntent().hasExtra("username")) {
            username = requireActivity().getIntent().getStringExtra("username");
        }

        initializeViews(view);
        initializeCoffeeList();
        setupBannerSlider(view);
        setupSearch();
        setupChips();
        setupListeners();
        displayUsername();

        return view;
    }

    private void initializeViews(View view) {
        tvUsername    = view.findViewById(R.id.tv_username);
        etSearch      = view.findViewById(R.id.et_search);
        btnClearSearch = view.findViewById(R.id.btn_clear_search);
        tvNoResults   = view.findViewById(R.id.tv_no_results);

        bannerSlider  = view.findViewById(R.id.banner_slider);
        dotsIndicator = view.findViewById(R.id.dots_indicator);
        coffeeListContainer = view.findViewById(R.id.coffee_list_container);

        // Coffee shop cards
        cardKenangan  = view.findViewById(R.id.card_kenangan);
        cardStarbucks = view.findViewById(R.id.card_starbucks);
        cardTomoro    = view.findViewById(R.id.card_tomoro);
        cardJanjiJiwa = view.findViewById(R.id.card_janjijiwa);
        cardPoint     = view.findViewById(R.id.card_point);

        // Description buttons
        btnDescKenangan  = view.findViewById(R.id.btn_desc_kenangan);
        btnDescStarbucks = view.findViewById(R.id.btn_desc_starbucks);
        btnDescTomoro    = view.findViewById(R.id.btn_desc_tomoro);
        btnDescJanjiJiwa = view.findViewById(R.id.btn_desc_janjijiwa);
        btnDescPoint     = view.findViewById(R.id.btn_desc_point);

        // Filter chips
        chipSemua   = view.findViewById(R.id.chip_semua);
        chipTerdekat = view.findViewById(R.id.chip_terdekat);
        chipRating  = view.findViewById(R.id.chip_rating);
        chipPopuler = view.findViewById(R.id.chip_populer);
        chipBaru    = view.findViewById(R.id.chip_baru);

        // Rekomendasi menu cards
        cardMenuCaramel  = view.findViewById(R.id.card_menu_caramel);
        cardMenuKopiSusu = view.findViewById(R.id.card_menu_kopisusu);
        cardMenuOatLatte = view.findViewById(R.id.card_menu_oatlatte);
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
        if (dotsIndicator == null || getContext() == null) return;
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
        if (dots.length > 0) dots[position].setImageResource(R.drawable.dot_active);
    }

    private void setupAutoSlide() {
        sliderHandler = new Handler(Looper.getMainLooper());
        sliderRunnable = () -> {
            if (bannerSlider != null) {
                int next = (bannerSlider.getCurrentItem() + 1) % bannerImages.size();
                bannerSlider.setCurrentItem(next);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        };
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    private void setupSearch() {
        if (etSearch == null) return;
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                btnClearSearch.setVisibility(query.length() > 0 ? View.VISIBLE : View.GONE);
                filterCoffeeShops(query);
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

    // ===================== FILTER CHIPS =====================

    private void setupChips() {
        if (chipSemua != null)    chipSemua.setOnClickListener(v -> applyChipFilter("semua"));
        if (chipTerdekat != null) chipTerdekat.setOnClickListener(v -> applyChipFilter("terdekat"));
        if (chipRating != null)   chipRating.setOnClickListener(v -> applyChipFilter("rating"));
        if (chipPopuler != null)  chipPopuler.setOnClickListener(v -> applyChipFilter("populer"));
        if (chipBaru != null)     chipBaru.setOnClickListener(v -> applyChipFilter("baru"));
    }

    private void applyChipFilter(String chip) {
        activeChip = chip;
        updateChipStyles();

        // Reset search
        if (etSearch != null) etSearch.setText("");

        switch (chip) {
            case "semua":
                // Tampilkan semua
                showAllCoffeeShops();
                break;

            case "terdekat":
                // Urutan terdekat: Kenangan, JanjiJiwa, Tomoro, Point, Starbucks
                setCardsOrder(new int[]{0, 3, 2, 4, 1});
                showAllCoffeeShops();
                Toast.makeText(requireContext(), "📍 Urutan berdasarkan jarak terdekat", Toast.LENGTH_SHORT).show();
                break;

            case "rating":
                // Tampilkan hanya rating >= 4.8: Starbucks(5.0), Kenangan(4.9), JanjiJiwa(4.8)
                for (int i = 0; i < allCards.size(); i++) {
                    allCards.get(i).setVisibility(coffeeRatings[i] >= 4.8 ? View.VISIBLE : View.GONE);
                }
                updateNoResultsAfterFilter();
                break;

            case "populer":
                // Populer: Starbucks, Kenangan, JanjiJiwa (paling banyak ulasan)
                setCardsOrder(new int[]{1, 0, 3, 2, 4});
                showAllCoffeeShops();
                Toast.makeText(requireContext(), "🔥 Urutan berdasarkan popularitas", Toast.LENGTH_SHORT).show();
                break;

            case "baru":
                // Baru: Tomoro & Point (brand lebih baru)
                for (int i = 0; i < allCards.size(); i++) {
                    boolean isNew = (i == 2 || i == 4); // Tomoro & Point
                    allCards.get(i).setVisibility(isNew ? View.VISIBLE : View.GONE);
                }
                updateNoResultsAfterFilter();
                break;
        }
    }

    /** Ubah urutan tampil card di container */
    private void setCardsOrder(int[] order) {
        if (coffeeListContainer == null) return;
        coffeeListContainer.removeAllViews();
        for (int idx : order) {
            CardView card = allCards.get(idx);
            if (card.getParent() != null) ((ViewGroup) card.getParent()).removeView(card);
            coffeeListContainer.addView(card);
        }
    }

    /** Update style chips — aktif vs tidak aktif */
    private void updateChipStyles() {
        TextView[] chips = {chipSemua, chipTerdekat, chipRating, chipPopuler, chipBaru};
        String[] ids     = {"semua",   "terdekat",   "rating",   "populer",   "baru"};

        for (int i = 0; i < chips.length; i++) {
            if (chips[i] == null) continue;
            if (ids[i].equals(activeChip)) {
                chips[i].setBackgroundResource(R.drawable.bg_chip_active);
                chips[i].setTextColor(getResources().getColor(R.color.chip_active_text, null));
                chips[i].setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                chips[i].setBackgroundResource(R.drawable.bg_chip_inactive);
                chips[i].setTextColor(getResources().getColor(R.color.text_secondary, null));
                chips[i].setTypeface(null, android.graphics.Typeface.NORMAL);
            }
        }
    }

    private void updateNoResultsAfterFilter() {
        int visible = 0;
        for (CardView c : allCards) if (c.getVisibility() == View.VISIBLE) visible++;
        if (tvNoResults != null) {
            tvNoResults.setVisibility(visible == 0 ? View.VISIBLE : View.GONE);
        }
    }

    // ===================== SEARCH =====================

    private void filterCoffeeShops(String query) {
        if (query.isEmpty()) {
            applyChipFilter(activeChip);
            return;
        }
        // Reset chip ke semua saat search aktif
        String lowerQuery = query.toLowerCase().trim();
        visibleCardsCount = 0;
        for (int i = 0; i < coffeeNames.size(); i++) {
            boolean match = coffeeNames.get(i).contains(lowerQuery);
            allCards.get(i).setVisibility(match ? View.VISIBLE : View.GONE);
            if (match) visibleCardsCount++;
        }
        updateNoResultsMessage(query);
    }

    private void showAllCoffeeShops() {
        for (CardView card : allCards) if (card != null) card.setVisibility(View.VISIBLE);
        visibleCardsCount = allCards.size();
        if (tvNoResults != null) tvNoResults.setVisibility(View.GONE);
    }

    private void updateNoResultsMessage(String query) {
        if (tvNoResults == null) return;
        if (visibleCardsCount == 0) {
            tvNoResults.setText("Tidak ditemukan coffee shop untuk \"" + query + "\"");
            tvNoResults.setVisibility(View.VISIBLE);
        } else {
            tvNoResults.setVisibility(View.GONE);
        }
    }

    // ===================== CLICK LISTENERS =====================

    private void setupListeners() {
        // Coffee shop cards → buka detail
        if (cardKenangan  != null) cardKenangan.setOnClickListener(v  -> openCoffeeDetail("Kenangan Coffee"));
        if (btnDescKenangan  != null) btnDescKenangan.setOnClickListener(v  -> openCoffeeDetail("Kenangan Coffee"));

        if (cardStarbucks != null) cardStarbucks.setOnClickListener(v -> openCoffeeDetail("Starbucks Coffee"));
        if (btnDescStarbucks != null) btnDescStarbucks.setOnClickListener(v -> openCoffeeDetail("Starbucks Coffee"));

        if (cardTomoro    != null) cardTomoro.setOnClickListener(v    -> openCoffeeDetail("Tomoro Coffee"));
        if (btnDescTomoro    != null) btnDescTomoro.setOnClickListener(v    -> openCoffeeDetail("Tomoro Coffee"));

        if (cardJanjiJiwa != null) cardJanjiJiwa.setOnClickListener(v -> openCoffeeDetail("Janji Jiwa Coffee"));
        if (btnDescJanjiJiwa != null) btnDescJanjiJiwa.setOnClickListener(v -> openCoffeeDetail("Janji Jiwa Coffee"));

        if (cardPoint     != null) cardPoint.setOnClickListener(v     -> openCoffeeDetail("Point Coffee"));
        if (btnDescPoint     != null) btnDescPoint.setOnClickListener(v     -> openCoffeeDetail("Point Coffee"));

        // Rekomendasi menu cards → buka detail coffee shop terkait
        if (cardMenuCaramel  != null) cardMenuCaramel.setOnClickListener(v  -> openCoffeeDetail("Starbucks Coffee"));
        if (cardMenuKopiSusu != null) cardMenuKopiSusu.setOnClickListener(v -> openCoffeeDetail("Kenangan Coffee"));
        if (cardMenuOatLatte != null) cardMenuOatLatte.setOnClickListener(v -> openCoffeeDetail("Tomoro Coffee"));
    }

    private void openCoffeeDetail(String shopName) {
        try {
            Intent intent = new Intent(requireActivity(), CoffeeDetailActivity.class);
            intent.putExtra("shop_name", shopName);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void displayUsername() {
        if (tvUsername != null) tvUsername.setText(username);
    }

    private void hideKeyboard() {
        if (requireActivity().getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(requireActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sliderHandler != null && sliderRunnable != null) sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sliderHandler != null && sliderRunnable != null) sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sliderHandler != null && sliderRunnable != null) sliderHandler.removeCallbacks(sliderRunnable);
    }
}
