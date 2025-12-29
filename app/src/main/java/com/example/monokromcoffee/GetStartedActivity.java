package com.example.monokromcoffee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GetStartedActivity extends AppCompatActivity {

    private TextView locationText;
    private Button getStartedButton;
    private ImageView flagIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        // Inisialisasi views
        flagIcon = findViewById(R.id.flag_icon);
        locationText = findViewById(R.id.location_text);
        getStartedButton = findViewById(R.id.btn_get_started);

        // Set Indonesian flag
        flagIcon.setImageResource(R.drawable.logo_bendera);

        // Set lokasi statis
        locationText.setText("Cikarang, Indonesia");

        // Tombol Get Started - Pindah ke LoginActivity
        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GetStartedActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Tutup GetStartedActivity
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Disable back button agar tidak kembali ke splash screen
        super.onBackPressed();
        finishAffinity();
    }
}