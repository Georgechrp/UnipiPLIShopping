package com.unipi.george.unipiplishopping;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CartFragment extends Fragment {

    private static final String TAG = "CartFragment";
    private FirebaseFirestore db;
    private LinearLayout linearLayout;
    private String userId;
    private FirebaseAuth auth;
    private FirebaseUser user;

    public CartFragment() {
        // Required empty public constructor
    }

    public static CartFragment newInstance() {
        return new CartFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        linearLayout = view.findViewById(R.id.linearLayoutData);
        db = FirebaseFirestore.getInstance();

        if (userId != null) {
            loadSavedStories();
        } else {
            Log.e(TAG, "User is not logged in");
        }

        return view;
    }

    private void loadSavedStories() {
        db.collection("users_data")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> savedStories = (List<String>) documentSnapshot.get("favorites");
                        if (savedStories != null && !savedStories.isEmpty()) {
                            fetchStoriesData(savedStories);
                        } else {
                            Toast.makeText(getContext(), "Δεν έχετε αποθηκευμένες ιστορίες", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Δεν έχετε αποθηκεύσει κάποια ιστορία", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error retrieving data", e));
    }

    private void fetchStoriesData(List<String> storyIds) {
        linearLayout.removeAllViews(); // Καθαρισμός της προβολής πριν τη φόρτωση
        for (String storyId : storyIds) {
            db.collection("products")
                    .document(storyId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String code = documentSnapshot.getString("code");
                            String description = documentSnapshot.getString("description");
                            String imageURL = documentSnapshot.getString("imageURL");
                            GeoPoint locationShop = documentSnapshot.getGeoPoint("location");
                            double latitude = locationShop != null ? locationShop.getLatitude() : 0;
                            double longitude = locationShop != null ? locationShop.getLongitude() : 0;
                            String name = documentSnapshot.getString("name");
                            String price = documentSnapshot.getString("price");
                            Timestamp timestamp = documentSnapshot.getTimestamp("release_date");
                            String releaseDate = timestamp != null ? timestamp.toDate().toString() : "N/A";

                            addDataToView(storyId, code, description, imageURL, latitude, longitude, name, price, releaseDate);
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error retrieving story: " + storyId, e));
        }
    }

    private void addDataToView(String documentId, String code, String description, String imageURL, double latitude, double longitude, String name, String price, String releaseDate) {
        // Δημιουργία CardView
        CardView cardView = new CardView(getContext());
        cardView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        cardView.setRadius(16);
        cardView.setCardElevation(8);
        cardView.setUseCompatPadding(true);
        cardView.setPadding(16, 16, 16, 16);

        // Δημιουργία κύριου LinearLayout (οριζόντια διάταξη)
        LinearLayout horizontalLayout = new LinearLayout(getContext());
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
        horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // Δημιουργία κάθετου Layout για την εικόνα και το κουμπί
        LinearLayout imageAndButtonLayout = new LinearLayout(getContext());
        imageAndButtonLayout.setOrientation(LinearLayout.VERTICAL);
        imageAndButtonLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // Δημιουργία ImageView για εικόνα προϊόντος
        ImageView imageView = new ImageView(getContext());
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(200, 200);
        imageParams.setMargins(0, 0, 16, 0);
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Φόρτωση εικόνας με Picasso
        Picasso.get()
                .load(imageURL)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.errorimage)
                .into(imageView);

        // Δημιουργία Button κάτω από την εικόνα
        Button buyButton = new Button(getContext());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(0, 8, 0, 0);
        buyButton.setLayoutParams(buttonParams);
        buyButton.setText("Αγορά");
        buyButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Το προϊόν " + name + " επιλέχθηκε για αγορά!", Toast.LENGTH_SHORT).show();
            // Εδώ μπορείς να προσθέσεις επιπλέον λειτουργικότητα (π.χ. προσθήκη σε καλάθι)
        });

        // Προσθήκη της εικόνας και του κουμπιού στο κάθετο Layout
        imageAndButtonLayout.addView(imageView);
        imageAndButtonLayout.addView(buyButton);

        // Δημιουργία κάθετου LinearLayout για το κείμενο
        LinearLayout verticalLayout = new LinearLayout(getContext());
        verticalLayout.setOrientation(LinearLayout.VERTICAL);
        verticalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        ));

        // Δημιουργία ImageView για την καρδιά (αγαπημένα)
        ImageView iconView = new ImageView(getContext());
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(50, 50);
        iconParams.setMargins(0, 0, 16, 0);
        iconView.setLayoutParams(iconParams);

        // Αρχικό εικονίδιο
        iconView.setImageResource(R.drawable.heart2); // Υποθέτουμε ότι είναι ήδη στα αγαπημένα
        iconView.setOnClickListener(v -> {
            db.collection("users_data").document(userId)
                    .update("favorites", FieldValue.arrayRemove(documentId))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Αφαιρέθηκε από τα αγαπημένα!", Toast.LENGTH_SHORT).show();
                        linearLayout.removeView(cardView); // Αφαίρεση από την προβολή
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to remove from favorites: " + e.getMessage()));
        });

        // Δημιουργία TextViews για όνομα, περιγραφή, τιμή και ημερομηνία
        TextView nameTextView = new TextView(getContext());
        nameTextView.setText(name);
        nameTextView.setTextSize(18);
        nameTextView.setTypeface(null, Typeface.BOLD);

        TextView descriptionTextView = new TextView(getContext());
        descriptionTextView.setText(description);
        descriptionTextView.setTextSize(14);
        descriptionTextView.setPadding(0, 8, 0, 8);

        TextView priceTextView = new TextView(getContext());
        priceTextView.setText(price);
        priceTextView.setTextSize(16);
        priceTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.teal_700));

        TextView dateTextView = new TextView(getContext());
        dateTextView.setText("Release Date: " + releaseDate);
        dateTextView.setTextSize(14);
        dateTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        dateTextView.setPadding(0, 8, 0, 0);

        verticalLayout.addView(iconView); // Προσθήκη του εικονιδίου καρδιάς
        verticalLayout.addView(nameTextView);
        verticalLayout.addView(descriptionTextView);
        verticalLayout.addView(priceTextView);
        verticalLayout.addView(dateTextView);

        // Προσθήκη του ImageAndButtonLayout και του VerticalLayout στο HorizontalLayout
        horizontalLayout.addView(imageAndButtonLayout);
        horizontalLayout.addView(verticalLayout);

        cardView.addView(horizontalLayout);
        linearLayout.addView(cardView);
    }


}
