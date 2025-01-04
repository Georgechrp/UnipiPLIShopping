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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FirebaseFirestore db;
    private LinearLayout linearLayout;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String userId;

    public HomeFragment() {
        // Default constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
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

    private void loadAllDocuments() {
        if (userId == null) {
            Log.e(TAG, "User not logged in");
            return;
        }

        db.collection("users_data").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<String> favorites = (List<String>) task.getResult().get("favorites");

                db.collection("products").get().addOnCompleteListener(productTask -> {
                    if (productTask.isSuccessful()) {
                        for (QueryDocumentSnapshot document : productTask.getResult()) {
                            String documentId = document.getId();
                            String code = document.getString("code");
                            String description = document.getString("description");
                            String imageURL = document.getString("imageURL");
                            GeoPoint locationShop = document.getGeoPoint("location");
                            double latitude = 0;
                            double longitude = 0;
                            if (locationShop != null) {
                                latitude = locationShop.getLatitude();
                                longitude = locationShop.getLongitude();
                            }
                            String name = document.getString("name");
                            String price = document.getString("price");
                            Timestamp timestamp = document.getTimestamp("release_date");
                            String releaseDate = (timestamp != null) ? timestamp.toDate().toString() : "N/A";

                            boolean isFavorite = favorites != null && favorites.contains(documentId);

                            addDataToView(documentId, code, description, imageURL, latitude, longitude, name, price, releaseDate, isFavorite);
                        }
                    } else {
                        Log.w(TAG, "Error retrieving products", productTask.getException());
                    }
                });
            } else {
                Log.w(TAG, "Error retrieving user favorites", task.getException());
            }
        });
    }

    private void addDataToView(String documentId, String code, String description, String imageURL, double latitude, double longitude, String name, String price, String releaseDate, boolean isFavorite) {
        CardView cardView = new CardView(getContext());
        cardView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        cardView.setRadius(16);
        cardView.setCardElevation(8);
        cardView.setUseCompatPadding(true);
        cardView.setPadding(16, 16, 16, 16);

        LinearLayout horizontalLayout = new LinearLayout(getContext());
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
        horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        ImageView imageView = new ImageView(getContext());
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(200, 200);
        imageParams.setMargins(0, 0, 16, 0);
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Picasso.get()
                .load(imageURL)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.errorimage)
                .into(imageView);

        ImageView iconView = new ImageView(getContext());
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(50, 50);
        iconParams.setMargins(0, 0, 16, 0);
        iconView.setLayoutParams(iconParams);

        if (isFavorite) {
            iconView.setImageResource(R.drawable.heart2);
            iconView.setTag("added");
        } else {
            iconView.setImageResource(R.drawable.heart);
            iconView.setTag("removed");
        }

        iconView.setOnClickListener(v -> {
            if ("added".equals(iconView.getTag())) {
                iconView.setImageResource(R.drawable.heart);
                iconView.setTag("removed");
                db.collection("users_data").document(userId)
                        .update("favorites", FieldValue.arrayRemove(documentId))
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(getContext(), "Removed from favorites!", Toast.LENGTH_SHORT).show()
                        )
                        .addOnFailureListener(e ->
                                Log.e(TAG, "Failed to remove from favorites: " + e.getMessage())
                        );
            } else {
                iconView.setImageResource(R.drawable.heart2);
                iconView.setTag("added");
                db.collection("users_data").document(userId)
                        .update("favorites", FieldValue.arrayUnion(documentId))
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(getContext(), "Added to favorites!", Toast.LENGTH_SHORT).show()
                        )
                        .addOnFailureListener(e ->
                                Log.e(TAG, "Failed to add to favorites: " + e.getMessage())
                        );
            }
        });

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

        LinearLayout verticalLayout = new LinearLayout(getContext());
        verticalLayout.setOrientation(LinearLayout.VERTICAL);
        verticalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        ));
        verticalLayout.addView(nameTextView);
        verticalLayout.addView(descriptionTextView);
        verticalLayout.addView(priceTextView);
        verticalLayout.addView(dateTextView);

        horizontalLayout.addView(imageView);
        horizontalLayout.addView(iconView);
        horizontalLayout.addView(verticalLayout);

        cardView.addView(horizontalLayout);
        linearLayout.addView(cardView);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        db = FirebaseFirestore.getInstance();

        linearLayout = view.findViewById(R.id.linearLayoutData);

        loadAllDocuments();

        return view;
    }
}
