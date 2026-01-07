package com.example.monokromcoffee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GetStartedActivity extends AppCompatActivity {

    private TextView locationText;
    private Button getStartedButton;
    private ImageView flagIcon;
    private String selectedCountry = "Indonesia";
    private String selectedCity = "Kabupaten Bekasi";

    // Data 3 negara
    private String[] countries = {
            "Indonesia - Kabupaten Bekasi",
            "Singapore - Singapore",
            "Malaysia - Kuala Lumpur"
    };

    private int[] countryFlags = {
            R.drawable.bendera_indo,      // Indonesia - PERHATIKAN KOMA INI
            R.drawable.logo_singapore,    // Singapore - PERHATIKAN KOMA INI
            R.drawable.logo_malaysia      // Malaysia - baris terakhir tidak perlu koma
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        // Inisialisasi views
        flagIcon = findViewById(R.id.flag_icon);
        locationText = findViewById(R.id.location_text);
        getStartedButton = findViewById(R.id.btn_get_started);

        // Set default flag dan lokasi
        flagIcon.setImageResource(R.drawable.bendera_indo);
        locationText.setText("Kabupaten Bekasi, Indonesia");

        // Klik pada bendera untuk ganti negara
        flagIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCountrySelectionDialog();
            }
        });

        // Klik pada teks lokasi untuk ganti negara
        locationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCountrySelectionDialog();
            }
        });

        // Tombol Get Started - Pindah ke LoginActivity
        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GetStartedActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showCountrySelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Lokasi Anda");
        builder.setItems(countries, (dialog, which) -> {
            // Ambil data negara yang dipilih
            String selected = countries[which];
            String[] parts = selected.split(" - ");
            selectedCountry = parts[0];
            selectedCity = parts[1];

            // Update tampilan
            locationText.setText(selectedCity + ", " + selectedCountry);
            flagIcon.setImageResource(countryFlags[which]);
        });
        builder.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}