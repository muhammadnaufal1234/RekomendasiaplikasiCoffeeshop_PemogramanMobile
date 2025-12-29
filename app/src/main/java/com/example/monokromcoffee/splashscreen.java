package com.example.monokromcoffee;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class splashscreen extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 detik
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable navigateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Hide action bar if exists
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        navigateRunnable = new Runnable() {
            @Override
            public void run() {
                // Pindah ke GetStartedActivity setelah 3 detik
                Intent intent = new Intent(splashscreen.this, GetStartedActivity.class);
                startActivity(intent);
                finish(); // Tutup splash screen agar tidak bisa kembali
            }
        };

        // Delay 3 detik lalu pindah ke GetStartedActivity
        handler.postDelayed(navigateRunnable, SPLASH_DURATION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hapus callback untuk mencegah memory leak
        if (handler != null && navigateRunnable != null) {
            handler.removeCallbacks(navigateRunnable);
        }
    }
}