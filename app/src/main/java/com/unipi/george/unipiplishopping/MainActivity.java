package com.unipi.george.unipiplishopping;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
    private FirebaseAuth auth; // Αντικείμενο για χρήστη Firebase
    private FirebaseUser user; // Αντικείμενο που αναφέρεται στον τρέχοντα χρήστη
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) { // Αν δεν υπάρχει τρέχων χρήστης, μετάβαση στη Login δραστηριότητα
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            finish();
        }
       /* if (checkLocationPermission()) {
            // Έχει ήδη δοθεί η άδεια
            accessUserLocation();
        } else {
            // Ζήτα άδεια από τον χρήστη
            requestLocationPermission();
        }*/
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Switch locationSwitch = findViewById(R.id.location_switch);

        /*locationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (checkLocationPermission()) {
                    // Έχει ήδη δοθεί η άδεια
                    accessUserLocation();
                } else {
                    // Ζήτα άδεια από τον χρήστη
                    requestLocationPermission();
                }
            } *//*else {
                // Ο χρήστης απενεργοποίησε την τοποθεσία
               // disableLocationAccess();
            }*//*
        });*/

        // Προσθήκη του αρχικού HomeFragment στο container
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new HomeFragment())
                .addToBackStack(null)
                .commit();


        // Αρχικοποίηση και λειτουργικότητα του BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Εναλλαγή μεταξύ των fragment ανάλογα με το επιλεγμένο tab
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
    boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Κλήση στην υπερκλάση
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Άδεια δόθηκε
                accessUserLocation();
            } else {
                // Άδεια απορρίφθηκε
                showPermissionDeniedMessage();
            }
        }
    }


    private void accessUserLocation() {
        // Λογική για πρόσβαση στην τοποθεσία
    }

    private void showPermissionDeniedMessage() {
        // Δείξε ένα μήνυμα στον χρήστη
        Toast.makeText(this, "Η πρόσβαση στην τοποθεσία είναι απαραίτητη για την εφαρμογή.", Toast.LENGTH_SHORT).show();
    }
    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            // Χρησιμοποίησε τις συντεταγμένες
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

    public void callSignOut(View view){
        signOut();
    }
    private void signOut() {
        auth.signOut(); // Αποσύνδεση από το Firebase
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();

    }

}