package com.example.monokromcoffee;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GetStartedActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private TextView locationText;
    private Button getStartedButton;
    private ImageView flagIcon;
    private LocationManager locationManager;

    private String selectedCountry = "Indonesia";
    private String selectedCity = "Kabupaten Bekasi";

    // Data 3 negara untuk manual fallback
    // Data 3 negara untuk manual fallback
    private String[] countries = {
            "Indonesia",
            "Singapore",
            "Malaysia"
    };

    private int[] countryFlags = {
            R.drawable.bendera_indo,
            R.drawable.logo_singapore,
            R.drawable.logo_malaysia
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        // Inisialisasi views
        flagIcon = findViewById(R.id.flag_icon);
        locationText = findViewById(R.id.location_text);
        getStartedButton = findViewById(R.id.btn_get_started);
        
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Set default flag
        flagIcon.setImageResource(R.drawable.bendera_indo);

        // Klik pada area bendera — pakai findViewById langsung
        View flagContainer = findViewById(R.id.flag_container);
        if (flagContainer != null) {
            flagContainer.setOnClickListener(v -> showCountrySelectionDialog());
        }
        // Fallback: pastikan icon juga bisa diklik
        flagIcon.setOnClickListener(v -> showCountrySelectionDialog());

        // Apply status bar insets agar flag tidak menabrak status bar
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(
            flagContainer != null ? flagContainer : flagIcon,
            (v, insets) -> {
                int statusBarHeight = insets.getInsets(
                    androidx.core.view.WindowInsetsCompat.Type.statusBars()).top;
                android.view.ViewGroup.MarginLayoutParams params =
                    (android.view.ViewGroup.MarginLayoutParams) v.getLayoutParams();
                params.topMargin = statusBarHeight + 16;
                v.setLayoutParams(params);
                return insets;
            });

        // Klik pada teks lokasi untuk ganti negara
        locationText.setOnClickListener(v -> showCountrySelectionDialog());

        // Tombol Get Started - Pindah ke LoginActivity
        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GetStartedActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        
        requestLocationPermission();
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocation();
        } else {
            locationText.setText(selectedCity + ", " + selectedCountry);
        }
    }

    private void fetchLocation() {
        try {
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            
            Location location = null;
            if (isNetworkEnabled) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (location == null && isGpsEnabled) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            if (location != null) {
                updateLocationText(location);
            } 
            
            LocationListener listener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location loc) {
                    updateLocationText(loc);
                    locationManager.removeUpdates(this);
                }
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}
                @Override
                public void onProviderEnabled(String provider) {}
                @Override
                public void onProviderDisabled(String provider) {}
            };
            
            // Request single update from available providers
            if (isNetworkEnabled) {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, listener, null);
            }
            if (isGpsEnabled) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null);
            }

            if (!isGpsEnabled && !isNetworkEnabled) {
                locationText.setText("GPS tidak aktif. Nyalakan lokasi!");
            }

        } catch (SecurityException e) {
            e.printStackTrace();
            locationText.setText("Izin lokasi belum diberikan");
        }
    }

    private void updateLocationText(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                final String city = (address.getSubAdminArea() != null) ? address.getSubAdminArea() : 
                                   (address.getLocality() != null ? address.getLocality() : "Kota Tidak Diketahui");
                
                final String country = (address.getCountryName() != null) ? address.getCountryName() : "Negara Tidak Diketahui";
                
                selectedCity = city;
                selectedCountry = country;
                
                final String finalLocation = city + ", " + country;
                
                // Update on UI thread
                runOnUiThread(() -> {
                    locationText.setText(finalLocation);
                    
                    // Update bendera berdasarkan negara yang terdeteksi
                    if (country != null) {
                        if (country.toLowerCase().contains("indonesia")) {
                            flagIcon.setImageResource(R.drawable.bendera_indo);
                        } else if (country.toLowerCase().contains("singapore")) {
                            flagIcon.setImageResource(R.drawable.logo_singapore);
                        } else if (country.toLowerCase().contains("malaysia")) {
                            flagIcon.setImageResource(R.drawable.logo_malaysia);
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                locationText.setText(selectedCity + ", " + selectedCountry);
            });
        }
    }

    private void showCountrySelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Negara");
        builder.setItems(countries, (dialog, which) -> {
            // Ambil data negara yang dipilih
            selectedCountry = countries[which];
            selectedCity = ""; // Reset kota jika pilih manual negara saja

            // Update tampilan
            locationText.setText(selectedCountry);
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