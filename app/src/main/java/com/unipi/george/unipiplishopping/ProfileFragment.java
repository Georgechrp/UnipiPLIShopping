package com.unipi.george.unipiplishopping;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private EditText etName;
    private SharedPreferences sharedPreferences;
    private RadioGroup rgColors;
    private String mParam1;
    private String mParam2;
    private boolean isDarkThemeSelected;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if (fineLocationGranted != null && fineLocationGranted) {
                    Toast.makeText(requireContext(), "Η άδεια ACCESS_FINE_LOCATION δόθηκε.", Toast.LENGTH_SHORT).show();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    Toast.makeText(requireContext(), "Η άδεια ACCESS_COARSE_LOCATION δόθηκε.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Η πρόσβαση στην τοποθεσία είναι απαραίτητη για να λειτουργήσει.", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(requireContext(), "Άδεια για ειδοποιήσεις δόθηκε.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Η άδεια για ειδοποιήσεις απορρίφθηκε.", Toast.LENGTH_SHORT).show();
                }
            });

    public ProfileFragment() {
        // Απαιτείται κενός constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Έλεγχος άδειας ειδοποιήσεων
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Αρχικοποίηση SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("UserSettings", Context.MODE_PRIVATE);

        // Εύρεση του EditText για το όνομα
        etName = view.findViewById(R.id.et_name);

        // Φόρτωση δεδομένων από SharedPreferences
        loadPreferences();

        // Εύρεση του RadioGroup για την επιλογή θέματος
        rgColors = view.findViewById(R.id.rg_colors);

        // Φόρτωση της αποθηκευμένης επιλογής θέματος
        loadThemePreference();

        // Αποθήκευση προσωρινής επιλογής θέματος κατά την αλλαγή του RadioButton
        rgColors.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_light) {
                isDarkThemeSelected = false;
            } else if (checkedId == R.id.rb_dark) {
                isDarkThemeSelected = true;
            }
        });

        // Αποθήκευση όταν ο χρήστης πατήσει το κουμπί Save
        Button saveButton = view.findViewById(R.id.btn_save);
        saveButton.setOnClickListener(v -> {
            savePreferences();
            applyTheme();
        });

        // Λειτουργικότητα για το Logout Button
        Button logoutButton = view.findViewById(R.id.button); // Το ID πρέπει να είναι σωστό
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Αποσύνδεση από το Firebase
            Toast.makeText(requireContext(), "Αποσυνδεθήκατε.", Toast.LENGTH_SHORT).show();

            // Μετάβαση στο LoginActivity
            Intent intent = new Intent(requireContext(), Login.class);
            startActivity(intent);
            requireActivity().finish(); // Τερματισμός της τρέχουσας δραστηριότητας
        });

        return view;
    }

    private void loadPreferences() {
        // Φόρτωση αποθηκευμένων δεδομένων
        String name = sharedPreferences.getString("name", "");
        etName.setText(name);

        // Φόρτωση αποθηκευμένης επιλογής θέματος
        isDarkThemeSelected = sharedPreferences.getBoolean("isDarkTheme", false);
    }

    private void savePreferences() {
        // Αποθήκευση νέων δεδομένων
        String name = etName.getText().toString();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putBoolean("isDarkTheme", isDarkThemeSelected);
        editor.apply();

        Toast.makeText(requireContext(), "Οι ρυθμίσεις αποθηκεύτηκαν.", Toast.LENGTH_SHORT).show();
        // Εφαρμογή θέματος
        applyTheme();

    }

    private void loadThemePreference() {
        // Ενημέρωση RadioGroup με βάση την αποθηκευμένη επιλογή
        if (isDarkThemeSelected) {
            rgColors.check(R.id.rb_dark);
        } else {
            rgColors.check(R.id.rb_light);
        }
    }

    private void applyTheme() {
        // Εφαρμογή του θέματος
        if (isDarkThemeSelected) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
