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

    private static final String TAG = "NotificationsFragment"; // Debugging tag
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String userId;
    private NotificationHelper notificationHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor(); // Background thread executor

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user != null) {
            userId = user.getUid(); // Retrieve user ID if logged in
        } else {
            Log.e(TAG, "No user logged in");
        }

        notificationHelper = new NotificationHelper(requireContext()); // Initialize helper for notifications
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext()); // Initialize location client
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        db = FirebaseFirestore.getInstance();

        // Check location permissions and load documents if granted
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

        // Apply theme based on user preferences
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
        // Shutdown executor if needed
    }

    // Check if location permissions are granted
    private boolean checkLocationPermissions() {
        return ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // Request location permissions
    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                100);
    }

    // Load all product documents from Firestore
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
                        getDeviceLocation(null); // Retrieve device location if product location is unavailable
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

        // Retrieve device's last known location
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

    private void processProductsAsync(double userLatitude, double userLongitude, List<String> favorites) {
        backgroundExecutor.execute(() -> {
            db.collection("products").get().addOnCompleteListener(productTask -> {
                if (productTask.isSuccessful() && productTask.getResult() != null) {
                    requireActivity().runOnUiThread(() -> {
                        LinearLayout linearLayoutData = requireView().findViewById(R.id.linearLayoutData);
                        linearLayoutData.removeAllViews(); // Clear previous product cards
                    });

                    int notificationCount = 0; // Counter for notifications

                    for (QueryDocumentSnapshot document : productTask.getResult()) {
                        GeoPoint locationShop = document.getGeoPoint("location");

                        if (locationShop == null) continue;

                        double productLatitude = locationShop.getLatitude();
                        double productLongitude = locationShop.getLongitude();

                        float[] results = new float[1];
                        Location.distanceBetween(userLatitude, userLongitude, productLatitude, productLongitude, results);

                        // Filter products within 200 meters
                        if (results[0] <= 200) {
                            String name = document.getString("name");
                            String description = document.getString("description");

                            // Add product to UI
                            requireActivity().runOnUiThread(() ->
                                    addCardToLayout(name, description, productLatitude, productLongitude)
                            );

                            // Send notification for nearby products
                            if (name != null) {
                                String notificationMessage = name + " βρίσκεται κοντά σας.";
                                requireActivity().runOnUiThread(() ->
                                        notificationHelper.sendSimpleNotification(
                                                "Κοντινό Προϊόν",
                                                notificationMessage
                                        )
                                );
                                notificationCount++;
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

        // Product title
        TextView titleTextView = new TextView(requireContext());
        titleTextView.setText(name != null ? name : "Unknown Product");
        titleTextView.setTextSize(18);
        titleTextView.setGravity(Gravity.START);
        titleTextView.setTextColor(requireContext().getColor(android.R.color.black));

        // Product description
        TextView descriptionTextView = new TextView(requireContext());
        String additionalInfo = "Το προϊόν βρίσκεται κοντά σας.";
        if (latitude != 0 && longitude != 0) {
            additionalInfo += String.format(" Δείτε την ακριβή τοποθεσία: Lat %.5f, Lon %.5f", latitude, longitude);
        }
        descriptionTextView.setText((description != null ? description + "\n" : "") + additionalInfo);
        descriptionTextView.setTextSize(14);
        descriptionTextView.setGravity(Gravity.START);
        descriptionTextView.setTextColor(requireContext().getColor(android.R.color.darker_gray));

        // Add views to card layout
        cardContentLayout.addView(titleTextView);
        cardContentLayout.addView(descriptionTextView);

        cardView.addView(cardContentLayout);
        linearLayoutData.addView(cardView);
    }
}
