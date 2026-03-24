package com.example.monokromcoffee;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapsActivity extends AppCompatActivity {

    private ImageView btnClose, imgShopInfo;
    private TextView tvShopName, tvAddress, tvTime;
    private MapView map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Penting untuk OSMDroid: set user agent
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_maps);

        btnClose = findViewById(R.id.btn_close_map);
        tvShopName = findViewById(R.id.tv_map_shop_name);
        tvAddress = findViewById(R.id.tv_map_address);
        tvTime = findViewById(R.id.tv_map_time);
        imgShopInfo = findViewById(R.id.img_map_shop);
        map = findViewById(R.id.map_view);

        // Ambil data dari Intent
        String shopName = getIntent().getStringExtra("shop_name");
        int imageResId = getIntent().getIntExtra("shop_image", R.drawable.banner_enjoy);
        String shopLat = getIntent().getStringExtra("shop_lat");
        String shopLng = getIntent().getStringExtra("shop_lng");
        String shopAddress = getIntent().getStringExtra("shop_address");

        if (shopName != null) {
            tvShopName.setText(shopName);
            tvAddress.setText(shopAddress != null ? shopAddress : "Pusat Kota, Cabang Utama");
            tvTime.setText("08:00 - 22:00");
        }

        imgShopInfo.setImageResource(imageResId);

        btnClose.setOnClickListener(v -> finish());

        // Konfigurasi Map
        map.setMultiTouchControls(true);

        double lat = -6.200000;
        double lng = 106.816666;

        if (shopLat != null && shopLng != null) {
            try {
                lat = Double.parseDouble(shopLat);
                lng = Double.parseDouble(shopLng);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        GeoPoint startPoint = new GeoPoint(lat, lng);
        map.getController().setZoom(18.0);
        map.getController().setCenter(startPoint);

        // Tambahkan Marker
        Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        if (shopName != null) {
            startMarker.setTitle(shopName);
        }
        map.getOverlays().add(startMarker);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
        }
    }
}
