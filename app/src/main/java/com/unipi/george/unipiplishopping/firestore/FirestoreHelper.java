package com.unipi.george.unipiplishopping.firestore;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import com.unipi.george.unipiplishopping.models.Order;

import java.util.List;

public class FirestoreHelper {

    private FirebaseFirestore db;

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // Callback για φόρτωση αποθηκευμένων ιστοριών
    public interface SavedStoriesCallback {
        void onSuccess(List<String> savedStories);
        void onFailure(Exception e);
    }

    public void loadSavedStories(String userId, SavedStoriesCallback callback) {
        db.collection("users_data")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> savedStories = (List<String>) documentSnapshot.get("favorites");
                        if (savedStories != null && !savedStories.isEmpty()) {
                            callback.onSuccess(savedStories);
                        } else {
                            callback.onFailure(new Exception("Δεν έχετε αποθηκευμένες ιστορίες"));
                        }
                    } else {
                        callback.onFailure(new Exception("Δεν υπάρχει δεδομένο για τον χρήστη"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    // Callback για την ανάκτηση δεδομένων ενός προϊόντος (ιστορίας)
    public interface StoryDataCallback {
        void onSuccess(DocumentSnapshot documentSnapshot);
        void onFailure(Exception e);
    }

    public void fetchStoryData(String storyId, StoryDataCallback callback) {
        db.collection("products")
                .document(storyId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onSuccess(documentSnapshot);
                    } else {
                        callback.onFailure(new Exception("Το έγγραφο δεν υπάρχει"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    // Callback για την επεξεργασία παραγγελίας
    public interface ProcessOrderCallback {
        void onSuccess(DocumentReference documentReference);
        void onFailure(Exception e);
    }

    public void processOrder(Order order, ProcessOrderCallback callback) {
        db.collection("orders")
                .add(order)
                .addOnSuccessListener(documentReference -> callback.onSuccess(documentReference))
                .addOnFailureListener(callback::onFailure);
    }

    // Callback για αφαίρεση αγαπημένου προϊόντος
    public interface RemoveFavoriteCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void removeFavorite(String userId, String documentId, RemoveFavoriteCallback callback) {
        db.collection("users_data").document(userId)
                .update("favorites", FieldValue.arrayRemove(documentId))
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }
}
