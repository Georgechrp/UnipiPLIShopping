package com.unipi.george.unipiplishopping;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;

public class NotificationsFragment extends Fragment {

    private static final String TAG = "NotificationsFragment";
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String userId;
    private NotificationHelper notificationHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        } else {
            Log.e(TAG, "No user logged in");
        }

        notificationHelper = new NotificationHelper(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        db = FirebaseFirestore.getInstance();

        if (!checkLocationPermissions()) {
            requestLocationPermissions();
        } else {
            loadAllDocuments();
        }

        return view;
    }
    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserSettings", Context.MODE_PRIVATE);
        boolean isDarkTheme = sharedPreferences.getBoolean("isDarkTheme", false);

        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        //((Executors) backgroundExecutor).shutdown();
    }

    private boolean checkLocationPermissions() {
        return ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                100);
    }

    private void loadAllDocuments() {
        db.collection("products").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    GeoPoint productLocation = document.getGeoPoint("location");
                    if (productLocation != null) {
                        double latitude = productLocation.getLatitude();
                        double longitude = productLocation.getLongitude();
                        Log.d(TAG, "Product location retrieved: Lat " + latitude + ", Lon " + longitude);
                        processProductsAsync(latitude, longitude, null);
                    } else {
                        Log.e(TAG, "Product location not found in Firestore for document: " + document.getId());
                        // Κλήση για λήψη τοποθεσίας από τη συσκευή
                        getDeviceLocation(null);
                    }
                }
            } else {
                Log.w(TAG, "Failed to retrieve products", task.getException());
            }
        });
    }


    @SuppressLint("MissingPermission")
    private void getDeviceLocation(List<String> favorites) {
        if (!checkLocationPermissions()) {
            Log.e(TAG, "Location permissions not granted");
            return;
        }

        fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Location location = task.getResult();
                double userLatitude = location.getLatitude();
                double userLongitude = location.getLongitude();
                Log.d(TAG, "Device location retrieved: Lat " + userLatitude + ", Lon " + userLongitude);
                processProductsAsync(userLatitude, userLongitude, favorites);
            } else {
                Log.e(TAG, "Failed to get device location");
                notificationHelper.sendSimpleNotification(
                        "Location Unavailable",
                        "Please ensure your location services are enabled."
                );
            }
        });
    }

    // Μέσα στην κλάση NotificationsFragment
    private void processProductsAsync(double userLatitude, double userLongitude, List<String> favorites) {
        backgroundExecutor.execute(() -> {
            db.collection("products").get().addOnCompleteListener(productTask -> {
                if (productTask.isSuccessful() && productTask.getResult() != null) {
                    requireActivity().runOnUiThread(() -> {
                        LinearLayout linearLayoutData = requireView().findViewById(R.id.linearLayoutData);
                        linearLayoutData.removeAllViews(); // Καθαρισμός πριν από την προσθήκη νέων καρτών
                    });

                    int notificationCount = 0; // Μετρητής για τις ειδοποιήσεις

                    for (QueryDocumentSnapshot document : productTask.getResult()) {
                        GeoPoint locationShop = document.getGeoPoint("location");

                        if (locationShop == null) continue;

                        double productLatitude = locationShop.getLatitude();
                        double productLongitude = locationShop.getLongitude();

                        float[] results = new float[1];
                        Location.distanceBetween(userLatitude, userLongitude, productLatitude, productLongitude, results);

                        // Έλεγχος για απόσταση
                        if (results[0] <= 200) { // Φιλτράρισμα προϊόντων σε απόσταση 200 μέτρων
                            String name = document.getString("name");
                            String description = document.getString("description");

                            // Προσθήκη στο UI
                            requireActivity().runOnUiThread(() ->
                                    addCardToLayout(name, description, productLatitude, productLongitude)
                            );

                            // Δημιουργία ειδοποίησης μόνο για αυτά που πληρούν τα κριτήρια
                            if (name != null) {
                                String notificationMessage = name + " βρίσκεται κοντά σας.";
                                requireActivity().runOnUiThread(() ->
                                        notificationHelper.sendSimpleNotification(
                                                "Κοντινό Προϊόν",
                                                notificationMessage
                                        )
                                );
                                notificationCount++; // Αύξηση του μετρητή ειδοποιήσεων
                            }
                        }
                    }

                    int finalNotificationCount = notificationCount;
                    requireActivity().runOnUiThread(() -> {
                        if (finalNotificationCount == 0) {
                            Log.d(TAG, "Δεν βρέθηκαν προϊόντα κοντά.");
                        }
                    });
                } else {
                    Log.w(TAG, "Error retrieving products", productTask.getException());
                }
            });
        });
    }




    private void addCardToLayout(String name, String description, double latitude, double longitude) {
        LinearLayout linearLayoutData = requireView().findViewById(R.id.linearLayoutData);

        CardView cardView = new CardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(16, 16, 16, 16);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(12);
        cardView.setCardElevation(8);
        cardView.setUseCompatPadding(true);

        LinearLayout cardContentLayout = new LinearLayout(requireContext());
        cardContentLayout.setOrientation(LinearLayout.VERTICAL);
        cardContentLayout.setPadding(16, 16, 16, 16);

        // Τίτλος προϊόντος
        TextView titleTextView = new TextView(requireContext());
        titleTextView.setText(name != null ? name : "Unknown Product");
        titleTextView.setTextSize(18);
        titleTextView.setGravity(Gravity.START);
        titleTextView.setTextColor(requireContext().getColor(android.R.color.black));

        // Περιγραφή προϊόντος
        TextView descriptionTextView = new TextView(requireContext());
        String additionalInfo = "Το προϊόν βρίσκεται κοντά σας.";
        if (latitude != 0 && longitude != 0) {
            additionalInfo += String.format(" Δείτε την ακριβή τοποθεσία: Lat %.5f, Lon %.5f", latitude, longitude);
        }
        descriptionTextView.setText((description != null ? description + "\n" : "") + additionalInfo);
        descriptionTextView.setTextSize(14);
        descriptionTextView.setGravity(Gravity.START);
        descriptionTextView.setTextColor(requireContext().getColor(android.R.color.darker_gray));

        // Προσθήκη στο layout της κάρτας
        cardContentLayout.addView(titleTextView);
        cardContentLayout.addView(descriptionTextView);

        cardView.addView(cardContentLayout);
        linearLayoutData.addView(cardView);
    }

}