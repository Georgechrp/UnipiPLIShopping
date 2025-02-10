package com.unipi.george.unipiplishopping.fragments;

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
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;
import com.unipi.george.unipiplishopping.firestore.FirestoreHelper;
import com.unipi.george.unipiplishopping.models.Order;
import com.unipi.george.unipiplishopping.utils.MyTts;
import com.unipi.george.unipiplishopping.utils.PreferencesManager;
import com.unipi.george.unipiplishopping.R;

import java.util.List;

public class CartFragment extends Fragment {

    private static final String TAG = "CartFragment";

    private FirestoreHelper firestoreHelper;
    private LinearLayout linearLayout;
    private String userId;
    private FirebaseAuth auth;
    private FirebaseUser user;

    public CartFragment() {
        // constructor
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
        firestoreHelper = new FirestoreHelper();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Εφαρμογή θέματος βάσει των ρυθμίσεων χρήστη
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

        PreferencesManager preferencesManager = new PreferencesManager(requireContext());
        int fontSize = preferencesManager.getFontSize();

        if (userId != null) {
            loadSavedStories(fontSize);
        } else {
            Log.e(TAG, "User is not logged in");
        }
        return view;
    }


     //Φορτώνει τις αποθηκευμένες ιστορίες του χρήστη μέσω του FirestoreHelper.

    private void loadSavedStories(int fontSize) {
        firestoreHelper.loadSavedStories(userId, new FirestoreHelper.SavedStoriesCallback() {
            @Override
            public void onSuccess(List<String> savedStories) {
                fetchStoriesData(savedStories, fontSize);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Για κάθε αποθηκευμένη ιστορία, ανακτά τα δεδομένα της.

    private void fetchStoriesData(List<String> storyIds, int fontSize) {
        linearLayout.removeAllViews(); // Καθαρισμός της προβολής πριν τη φόρτωση
        for (String storyId : storyIds) {
            firestoreHelper.fetchStoryData(storyId, new FirestoreHelper.StoryDataCallback() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
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

                        addDataToView(storyId, code, description, imageURL, latitude, longitude, name, price, releaseDate, fontSize);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Error retrieving story: " + storyId, e);
                }
            });
        }
    }


    //Δημιουργεί την εμφάνιση για κάθε προϊόν (ιστορία) και την προσθέτει στο layout.

    private void addDataToView(String documentId, String code, String description, String imageURL,
                               double latitude, double longitude, String name, String price,
                               String releaseDate, int fontSize) {
        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Context is null, cannot create views");
            return;
        }

        // Δημιουργία CardView
        CardView cardView = new CardView(context);
        cardView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        cardView.setRadius(16);
        cardView.setCardElevation(8);
        cardView.setUseCompatPadding(true);
        cardView.setPadding(16, 16, 16, 16);

        // Οριζόντια διάταξη
        LinearLayout horizontalLayout = new LinearLayout(context);
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
        horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // Κάθετο Layout για την εικόνα και το κουμπί
        LinearLayout imageAndButtonLayout = new LinearLayout(context);
        imageAndButtonLayout.setOrientation(LinearLayout.VERTICAL);
        imageAndButtonLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // Εικόνα προϊόντος
        ImageView productImageView = new ImageView(context);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(200, 200);
        imageParams.setMargins(0, 0, 16, 0);
        productImageView.setLayoutParams(imageParams);
        productImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Picasso.get()
                .load(imageURL)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.errorimage)
                .into(productImageView);

        // Κουμπί "Αγορά"
        Button buyButton = new Button(context);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonParams.setMargins(0, 8, 0, 0);
        buyButton.setLayoutParams(buttonParams);
        buyButton.setText("Αγορά");
        buyButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserSettings", Context.MODE_PRIVATE);
            String fullName = sharedPreferences.getString("fullName", null);

            if (fullName == null || fullName.isEmpty()) {
                // Εμφάνιση διαλόγου για εισαγωγή ονοματεπώνυμου
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_enter_fullname, null);
                EditText editTextFullName = dialogView.findViewById(R.id.editTextFullName);

                builder.setView(dialogView)
                        .setTitle("Εισάγετε το Ονοματεπώνυμό σας")
                        .setPositiveButton("Αποθήκευση", (dialog, which) -> {
                            String enteredName = editTextFullName.getText().toString().trim();
                            if (!enteredName.isEmpty()) {
                                // Αποθήκευση στο SharedPreferences
                                sharedPreferences.edit().putString("fullName", enteredName).apply();
                                processOrder(enteredName, name, code, documentId);
                            } else {
                                Toast.makeText(context, "Το όνομα δεν μπορεί να είναι κενό!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Ακύρωση", (dialog, which) -> dialog.dismiss());

                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                processOrder(fullName, name, code, documentId);
            }
        });

        imageAndButtonLayout.addView(productImageView);
        imageAndButtonLayout.addView(buyButton);

        // Κάθετο Layout για τα κείμενα και το εικονίδιο "αγαπημένα"
        LinearLayout verticalLayout = new LinearLayout(context);
        verticalLayout.setOrientation(LinearLayout.VERTICAL);
        verticalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1));

        // Εικονίδιο αγαπημένων (καρδιά)
        ImageView favoriteIconView = new ImageView(context);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(50, 50);
        iconParams.setMargins(0, 0, 16, 0);
        favoriteIconView.setLayoutParams(iconParams);
        favoriteIconView.setImageResource(R.drawable.heart2);
        favoriteIconView.setOnClickListener(v -> {
            firestoreHelper.removeFavorite(userId, documentId, new FirestoreHelper.RemoveFavoriteCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(context, "Αφαιρέθηκε από τα αγαπημένα!", Toast.LENGTH_SHORT).show();
                    linearLayout.removeView(cardView);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to remove from favorites: " + e.getMessage());
                }
            });
        });

        // Δημιουργία TextViews για όνομα, περιγραφή, τιμή και ημερομηνία
        TextView nameTextView = new TextView(context);
        nameTextView.setText(name);
        nameTextView.setTextSize(fontSize);
        nameTextView.setTypeface(null, Typeface.BOLD);

        TextView descriptionTextView = new TextView(context);
        descriptionTextView.setText(description);
        descriptionTextView.setTextSize(fontSize - 2);
        descriptionTextView.setPadding(0, 8, 0, 8);

        TextView priceTextView = new TextView(context);
        priceTextView.setText(price);
        priceTextView.setTextSize(fontSize - 2);
        priceTextView.setTextColor(ContextCompat.getColor(context, R.color.teal_700));

        TextView dateTextView = new TextView(context);
        dateTextView.setText("Release Date: " + releaseDate);
        dateTextView.setTextSize(fontSize - 2);
        dateTextView.setTextColor(ContextCompat.getColor(context, R.color.black));
        dateTextView.setPadding(0, 8, 0, 0);

        // Εικονίδιο Text-to-Speech (tts)
        ImageView ttsImageView = new ImageView(context);
        LinearLayout.LayoutParams ttsParams = new LinearLayout.LayoutParams(50, 50);
        ttsParams.setMargins(0, 0, 16, 0);
        ttsImageView.setLayoutParams(ttsParams);
        ttsImageView.setImageResource(R.drawable.volume);
        ttsImageView.setOnClickListener(v -> {
            if (description != null && !description.isEmpty()) {
                MyTts.speakOrPause(context, description);
            } else {
                Toast.makeText(context, "Δεν υπάρχει περιγραφή για εκφώνηση", Toast.LENGTH_SHORT).show();
            }
        });

        verticalLayout.addView(favoriteIconView);
        verticalLayout.addView(nameTextView);
        verticalLayout.addView(descriptionTextView);
        verticalLayout.addView(ttsImageView);
        verticalLayout.addView(priceTextView);
        verticalLayout.addView(dateTextView);

        // Προσθήκη των Layouts στο οριζόντιο Layout
        horizontalLayout.addView(imageAndButtonLayout);
        horizontalLayout.addView(verticalLayout);

        cardView.addView(horizontalLayout);
        linearLayout.addView(cardView);
    }


     //Επεξεργάζεται την παραγγελία του χρήστη χρησιμοποιώντας τον FirestoreHelper.
    private void processOrder(String fullName, String productName, String productCode, String documentId) {
        if (userId == null) {
            Toast.makeText(getContext(), "Πρέπει να είστε συνδεδεμένος για να αγοράσετε.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Δημιουργία αντικειμένου Order
        Order order = new Order(fullName, userId, productCode, documentId, Timestamp.now());

        firestoreHelper.processOrder(order, new FirestoreHelper.ProcessOrderCallback() {
            @Override
            public void onSuccess(com.google.firebase.firestore.DocumentReference documentReference) {
                Toast.makeText(getContext(), "Η αγορά ολοκληρώθηκε!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Order saved successfully: " + documentReference.getId());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error saving order", e);
            }
        });
    }
}
