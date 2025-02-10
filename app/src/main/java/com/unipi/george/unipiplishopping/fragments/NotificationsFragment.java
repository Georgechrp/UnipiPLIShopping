package com.unipi.george.unipiplishopping.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.unipi.george.unipiplishopping.utils.PreferencesManager;
import com.unipi.george.unipiplishopping.R;
import com.unipi.george.unipiplishopping.utils.NotificationHelper;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;

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

        checkAndRequestPermissionsOnStart();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAndRequestPermissionsOnStart();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserSettings", Context.MODE_PRIVATE);
        boolean isDarkTheme = sharedPreferences.getBoolean("isDarkTheme", false);

        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void checkAndRequestPermissionsOnStart() {

        boolean locationPermissionGranted = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        boolean notificationPermissionGranted = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionGranted = hasPermission(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (!locationPermissionGranted || !notificationPermissionGranted) {
            if (!locationPermissionGranted) {
                Toast.makeText(requireContext(), "Η άδεια τοποθεσίας είναι απαραίτητη για να λειτουργήσει η εφαρμογή.", Toast.LENGTH_SHORT).show();
                requestLocationPermission();
            }

            if (!notificationPermissionGranted) {
                Toast.makeText(requireContext(), "Η άδεια ειδοποιήσεων είναι απαραίτητη για να λειτουργήσει η εφαρμογή.", Toast.LENGTH_SHORT).show();
                requestNotificationPermission();
            }
        } else {
            loadAllDocuments();
        }
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Άδεια τοποθεσίας παραχωρήθηκε!", Toast.LENGTH_SHORT).show();
                checkAndRequestPermissionsOnStart();
            } else {
                Toast.makeText(requireContext(), "Η άδεια τοποθεσίας απορρίφθηκε. Παρακαλώ παραχωρήστε την για να συνεχίσετε.", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Άδεια ειδοποιήσεων παραχωρήθηκε!", Toast.LENGTH_SHORT).show();
                checkAndRequestPermissionsOnStart();
            } else {
                Toast.makeText(requireContext(), "Η άδεια ειδοποιήσεων απορρίφθηκε. Παρακαλώ παραχωρήστε την για να συνεχίσετε.", Toast.LENGTH_SHORT).show();
            }
        }
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
                        "Please ensure your location services are enabled.",
                        0
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
                        linearLayoutData.removeAllViews();
                    });

                    for (QueryDocumentSnapshot document : productTask.getResult()) {
                        GeoPoint locationShop = document.getGeoPoint("location");

                        if (locationShop == null) continue;

                        double productLatitude = locationShop.getLatitude();
                        double productLongitude = locationShop.getLongitude();

                        float[] results = new float[1];
                        Location.distanceBetween(userLatitude, userLongitude, productLatitude, productLongitude, results);

                        if (results[0] <= 200) { // Έλεγχος απόστασης
                            String name = document.getString("name");
                            String description = document.getString("description");

                            // Δημιουργία μοναδικού ID για την ειδοποίηση
                            int notificationId = document.getId().hashCode();

                            // Δημιουργία CardView
                            requireActivity().runOnUiThread(() ->
                                    addCardToLayout(name, description, productLatitude, productLongitude)
                            );

                            // Αποστολή ειδοποίησης μόνο για προϊόντα εντός 200 μέτρων
                            if (name != null) {
                                String notificationMessage = name + " βρίσκεται κοντά σας.";
                                Log.d(TAG, "Sending notification: " + notificationMessage);
                                notificationHelper.sendSimpleNotification(
                                        "Κοντινό Προϊόν",
                                        notificationMessage,
                                        notificationId
                                );
                            } else {
                                Log.e(TAG, "Name is null. Notification not sent.");
                            }
                        }

                    }
                } else {
                    Log.w(TAG, "Error retrieving products", productTask.getException());
                }
            });
        });
    }

    private void addCardToLayout(String name, String description, double latitude, double longitude) {
        PreferencesManager preferencesManager = new PreferencesManager(requireContext());
        int fontSize = preferencesManager.getFontSize();
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

        TextView titleTextView = new TextView(requireContext());
        titleTextView.setText(name != null ? name : "Unknown Product");
        titleTextView.setTextSize(fontSize);
        titleTextView.setGravity(Gravity.START);
        titleTextView.setTextColor(requireContext().getColor(android.R.color.black));

        TextView descriptionTextView = new TextView(requireContext());
        String additionalInfo = "Το προϊόν βρίσκεται κοντά σας.";
        if (latitude != 0 && longitude != 0) {
            additionalInfo += String.format(" Δείτε την ακριβή τοποθεσία: Lat %.5f, Lon %.5f", latitude, longitude);
        }
        descriptionTextView.setText((description != null ? description + "\n" : "") + additionalInfo);
        descriptionTextView.setTextSize(fontSize - 2);
        descriptionTextView.setGravity(Gravity.START);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            descriptionTextView.setTextColor(requireContext().getColor(android.R.color.darker_gray));
        }

        cardContentLayout.addView(titleTextView);
        cardContentLayout.addView(descriptionTextView);

        cardView.addView(cardContentLayout);
        linearLayoutData.addView(cardView);
    }
}
