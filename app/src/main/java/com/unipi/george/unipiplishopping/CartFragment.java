package com.unipi.george.unipiplishopping;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firestore.admin.v1.Index;
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
        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Context is null, cannot create views");
            return;
        }
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

        // οριζόντια διάταξη
        LinearLayout horizontalLayout = new LinearLayout(getContext());
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
        horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // κάθετο Layout
        LinearLayout imageAndButtonLayout = new LinearLayout(getContext());
        imageAndButtonLayout.setOrientation(LinearLayout.VERTICAL);
        imageAndButtonLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // εικόνα προϊόντος
        ImageView productImageView = new ImageView(getContext());
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(200, 200);
        imageParams.setMargins(0, 0, 16, 0);
        productImageView.setLayoutParams(imageParams);
        productImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Φόρτωση εικόνας με Picasso
        Picasso.get()
                .load(imageURL)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.errorimage)
                .into(productImageView);

        // Button κάτω από την εικόνα
        Button buyButton = new Button(getContext());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(0, 8, 0, 0);
        buyButton.setLayoutParams(buttonParams);
        buyButton.setText("Αγορά");
        buyButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserSettings", Context.MODE_PRIVATE);
            String fullName = sharedPreferences.getString("fullName", null);

            if (fullName == null || fullName.isEmpty()) {
                // Αν το όνομα δεν υπάρχει, εμφάνιση διαλόγου για εισαγωγή
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_enter_fullname, null);
                EditText editTextFullName = dialogView.findViewById(R.id.editTextFullName);

                builder.setView(dialogView)
                        .setTitle("Εισάγετε το Ονοματεπώνυμό σας")
                        .setPositiveButton("Αποθήκευση", (dialog, which) -> {
                            String enteredName = editTextFullName.getText().toString().trim();
                            if (!enteredName.isEmpty()) {
                                // Αποθήκευση στο SharedPreferences
                                sharedPreferences.edit().putString("fullName", enteredName).apply();
                                // Εκκίνηση διαδικασίας αγοράς
                                processOrder(enteredName, name, code, documentId);
                            } else {
                                Toast.makeText(getContext(), "Το όνομα δεν μπορεί να είναι κενό!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Ακύρωση", (dialog, which) -> dialog.dismiss());

                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                // Εκκίνηση διαδικασίας αγοράς αν το όνομα υπάρχει
                processOrder(fullName, name, code, documentId);
            }
        });


        imageAndButtonLayout.addView(productImageView);
        imageAndButtonLayout.addView(buyButton);

        LinearLayout verticalLayout = new LinearLayout(getContext());
        verticalLayout.setOrientation(LinearLayout.VERTICAL);
        verticalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        ));

        // ImageView για την καρδιά (αγαπημένα)
        ImageView favoriteIconView = new ImageView(getContext());
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(50, 50);
        iconParams.setMargins(0, 0, 16, 0);
        favoriteIconView.setLayoutParams(iconParams);
        favoriteIconView.setImageResource(R.drawable.heart2); // Υποθέτουμε ότι είναι ήδη στα αγαπημένα
        favoriteIconView.setOnClickListener(v -> {
            db.collection("users_data").document(userId)
                    .update("favorites", FieldValue.arrayRemove(documentId))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Αφαιρέθηκε από τα αγαπημένα!", Toast.LENGTH_SHORT).show();
                        linearLayout.removeView(cardView);
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

        ImageView ttsImageView = new ImageView(getContext());
        LinearLayout.LayoutParams ttsParams = new LinearLayout.LayoutParams(50, 50);
        ttsParams.setMargins(0, 0, 16, 0);
        ttsImageView.setLayoutParams(ttsParams);
        ttsImageView.setImageResource(R.drawable.volume); // Αντικατάστησε με το δικό σου icon
        ttsImageView.setOnClickListener(v -> {
            if (description != null && !description.isEmpty()) {
                MyTts.speakOrPause(getContext(), description);
            } else {
                Toast.makeText(getContext(), "Δεν υπάρχει περιγραφή για εκφώνηση", Toast.LENGTH_SHORT).show();
            }
        });


        // Προσθήκη στοιχείων στο κάθετο Layout
        verticalLayout.addView(favoriteIconView);
        verticalLayout.addView(nameTextView);
        verticalLayout.addView(descriptionTextView);
        verticalLayout.addView(ttsImageView);
        verticalLayout.addView(priceTextView);
        verticalLayout.addView(dateTextView);

        // Προσθήκη του ImageAndButtonLayout και του VerticalLayout στο HorizontalLayout
        horizontalLayout.addView(imageAndButtonLayout);
        horizontalLayout.addView(verticalLayout);

        cardView.addView(horizontalLayout);
        linearLayout.addView(cardView);
    }
    private void processOrder(String fullName, String productName, String productCode, String documentId) {
        if (userId == null) {
            Toast.makeText(getContext(), "Πρέπει να είστε συνδεδεμένος για να αγοράσετε.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Δημιουργία παραγγελίας
        Order order = new Order(fullName, userId, productCode, documentId, Timestamp.now());

        // Αποθήκευση στη βάση δεδομένων
        db.collection("orders")
                .add(order)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Η αγορά ολοκληρώθηκε!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Order saved successfully: " + documentReference.getId());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error saving order", e));
    }

    public class Order {
        private String customerName;
        private String customerId;
        private String productCode;
        private String documentId; // Νέο πεδίο για το document ID
        private Timestamp timestamp;

        public Order(String customerName,String customerId, String productCode, String documentId, Timestamp timestamp) {
            this.customerName = customerName;
            this.customerId = customerId;
            this.productCode = productCode;
            this.documentId = documentId;
            this.timestamp = timestamp;
        }

        // Getters and setters
        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getProductCode() {
            return productCode;
        }

        public void setProductCode(String productCode) {
            this.productCode = productCode;
        }

        public String getDocumentId() {
            return documentId;
        }

        public void setDocumentId(String documentId) {
            this.documentId = documentId;
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Timestamp timestamp) {
            this.timestamp = timestamp;
        }
    }


}
