package com.unipi.george.unipiplishopping;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.Manifest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth; // Firebase authentication object
    private FirebaseUser user; // Firebase user object
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1; // Permission request code
    private FusedLocationProviderClient fusedLocationProviderClient; // For accessing user's location

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Load user settings for theme preference (dark/light mode)
        SharedPreferences sharedPreferences = getSharedPreferences("UserSettings", Context.MODE_PRIVATE);
        boolean isDarkTheme = sharedPreferences.getBoolean("isDarkTheme", false);

        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Firebase authentication initialization
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) { // If no user is logged in, navigate to Login activity
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            finish();
        }

        // Initialize location services
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up the initial fragment (HomeFragment)
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new HomeFragment())
                .addToBackStack(null)
                .commit();

        // Set up BottomNavigationView functionality
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Switch between fragments based on the selected tab
            if (itemId == R.id.nav_home) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new HomeFragment())
                        .commit();
                return true;
            } else if (itemId == R.id.nav_cart) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new CartFragment())
                        .commit();
                return true;
            } else if (itemId == R.id.nav_notifications) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new NotificationsFragment())
                        .commit();
                return true;

            } else if (itemId == R.id.nav_profile) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new ProfileFragment())
                        .commit();
                return true;
            } else {
                return false;
            }
        });

    }

    // Check if location permission is granted
    boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // Request location permission from the user
    void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Call the superclass method
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                accessUserLocation();
            } else {
                // Permission denied
                showPermissionDeniedMessage();
            }
        }
    }

    private void accessUserLocation() {
        // Logic for accessing user's location
    }

    private void showPermissionDeniedMessage() {
        // Notify the user that location access is necessary
        Toast.makeText(this, "Η πρόσβαση στην τοποθεσία είναι απαραίτητη για την εφαρμογή.", Toast.LENGTH_SHORT).show();
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            // Use the coordinates
                        } else {
                            Toast.makeText(this, "Δεν είναι δυνατή η εύρεση τοποθεσίας. Ενεργοποιήστε την τοποθεσία στη συσκευή σας.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Σφάλμα: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            requestLocationPermission();
        }
    }

    // Logout button click handler
    public void callSignOut(View view) {
        signOut();
    }

    private void signOut() {
        auth.signOut(); // Sign out from Firebase
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();

    }
}
