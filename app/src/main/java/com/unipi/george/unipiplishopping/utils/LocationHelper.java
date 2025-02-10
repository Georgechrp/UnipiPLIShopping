package com.unipi.george.unipiplishopping.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationHelper {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private final Context context;
    private final Activity activity;
    private final FusedLocationProviderClient fusedLocationProviderClient;

    public LocationHelper(Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    // Ελέγχει αν το δικαίωμα τοποθεσίας έχει δοθεί
    public boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // Ζητά από τον χρήστη την άδεια για την τοποθεσία
    public void requestLocationPermission() {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    // Διαχειρίζεται την απάντηση του χρήστη για τα δικαιώματα
    public void handlePermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                accessUserLocation();
            } else {
                showPermissionDeniedMessage();
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void accessUserLocation() {
        if (checkLocationPermission()) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            // Χρήση των συντεταγμένων όπως απαιτείται
                        } else {
                            Toast.makeText(context, "Δεν είναι δυνατή η εύρεση τοποθεσίας. Ενεργοποιήστε την τοποθεσία στη συσκευή σας.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Σφάλμα: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            requestLocationPermission();
        }
    }

    private void showPermissionDeniedMessage() {
        Toast.makeText(context, "Η πρόσβαση στην τοποθεσία είναι απαραίτητη για την εφαρμογή.", Toast.LENGTH_SHORT).show();
    }
}
