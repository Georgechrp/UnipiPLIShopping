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
import android.widget.SeekBar;
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

    // ActivityResultLauncher for location permissions
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

    // ActivityResultLauncher for notification permissions
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(requireContext(), "Άδεια για ειδοποιήσεις δόθηκε.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Η άδεια για ειδοποιήσεις απορρίφθηκε.", Toast.LENGTH_SHORT).show();
                }
            });

    public ProfileFragment() {
        // Default constructor
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

        // Check for notification permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("UserSettings", Context.MODE_PRIVATE);

        // Find views
        etName = view.findViewById(R.id.et_name);
        rgColors = view.findViewById(R.id.rg_colors);
        SeekBar fontSizeSeekBar = view.findViewById(R.id.sb_font_size);
        Button saveButton = view.findViewById(R.id.btn_save);
        Button logoutButton = view.findViewById(R.id.button);

        // Load preferences and theme
        loadPreferences(view);
        loadThemePreference();

        // Listener for theme selection
        rgColors.setOnCheckedChangeListener((group, checkedId) -> {
            isDarkThemeSelected = (checkedId == R.id.rb_dark);
        });

        // Listener for font size adjustment
        fontSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                etName.setTextSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No action needed
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No action needed
            }
        });

        // Listener for save button
        saveButton.setOnClickListener(v -> {
            savePreferences(view);
            applyTheme();
        });

        // Listener for logout button
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Logout from Firebase
            Toast.makeText(requireContext(), "Αποσυνδεθήκατε.", Toast.LENGTH_SHORT).show();

            // Navigate to LoginActivity
            Intent intent = new Intent(requireContext(), Login.class);
            startActivity(intent);
            requireActivity().finish(); // Terminate current activity
        });

        return view;
    }

    private void loadPreferences(View view) {
        if (view == null) return; // Check for null
        PreferencesManager preferencesManager = new PreferencesManager(requireContext());
        etName.setText(preferencesManager.getName());
        isDarkThemeSelected = preferencesManager.isDarkThemeSelected();
        SeekBar fontSizeSeekBar = view.findViewById(R.id.sb_font_size);
        etName.setTextSize(preferencesManager.getFontSize());
        fontSizeSeekBar.setProgress(preferencesManager.getFontSize());
    }

    private void savePreferences(View view) {
        PreferencesManager preferencesManager = new PreferencesManager(requireContext());
        preferencesManager.setName(etName.getText().toString());
        preferencesManager.setDarkThemeSelected(isDarkThemeSelected);
        SeekBar fontSizeSeekBar = view.findViewById(R.id.sb_font_size);
        preferencesManager.setFontSize(fontSizeSeekBar.getProgress());
        Toast.makeText(requireContext(), "Οι ρυθμίσεις αποθηκεύτηκαν.", Toast.LENGTH_SHORT).show();
    }

    private void loadThemePreference() {
        // Update RadioGroup based on saved theme
        if (isDarkThemeSelected) {
            rgColors.check(R.id.rb_dark);
        } else {
            rgColors.check(R.id.rb_light);
        }
    }

    private void applyTheme() {
        // Apply selected theme
        if (isDarkThemeSelected) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
