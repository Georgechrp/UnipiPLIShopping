package com.unipi.george.unipiplishopping;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.unipi.george.unipiplishopping.auth.Login;
import com.unipi.george.unipiplishopping.fragments.CartFragment;
import com.unipi.george.unipiplishopping.fragments.HomeFragment;
import com.unipi.george.unipiplishopping.fragments.NotificationsFragment;
import com.unipi.george.unipiplishopping.fragments.ProfileFragment;
import com.unipi.george.unipiplishopping.utils.LocationHelper;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser user;
    private LocationHelper locationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyUserTheme();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Αρχικοποίηση Firebase και έλεγχος αν υπάρχει συνδεδεμένος χρήστης
        initializeFirebase();
        checkUserAuthentication();

        // Φόρτωση αρχικού fragment
        loadDefaultFragment();

        initializeBottomNavigation();

        // Αρχικοποίηση και έλεγχος δικαιωμάτων τοποθεσίας
        initializeLocationHelper();
    }

    // εφαρμογή του θέματος(ανάλογα με την προτιμηση του χρηστη)
    private void applyUserTheme() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSettings", Context.MODE_PRIVATE);
        boolean isDarkTheme = sharedPreferences.getBoolean("isDarkTheme", false);
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    // Αρχικοποίηση του Firebase authentication
    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    // Έλεγχος εάν υπάρχει ήδη συνδεδεμένος χρήστης
    private void checkUserAuthentication() {
        if (user == null) {
            Intent intent = new Intent(this, Login.class);// Αν δεν υπάρχει συνδεδεμένος χρήστης, go to στη Login activity
            startActivity(intent);
            finish();
        }
    }

    // Φόρτωση του main/Home fragment
    private void loadDefaultFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new HomeFragment())
                .addToBackStack(null)
                .commit();
    }

    //Πλοήγηση μεταξύ fragments
    private void initializeBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

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


    // Αρχικοποίηση του LocationHelper και έλεγχος των δικαιωμάτων τοποθεσίας
    private void initializeLocationHelper() {
        locationHelper = new LocationHelper(this);
        if (!locationHelper.checkLocationPermission()) {
            locationHelper.requestLocationPermission();
        }
    }


    // Διαχείριση της απάντησης του χρήστη στα permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationHelper.handlePermissionsResult(requestCode, grantResults);
    }
}