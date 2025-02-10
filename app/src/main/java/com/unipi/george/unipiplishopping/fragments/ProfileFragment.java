package com.unipi.george.unipiplishopping.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.unipi.george.unipiplishopping.auth.Login;
import com.unipi.george.unipiplishopping.utils.PreferencesManager;
import com.unipi.george.unipiplishopping.R;

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

        // Ρυθμίσεις για το όνομα χρήστη
        etName.setText(preferencesManager.getName());
        isDarkThemeSelected = preferencesManager.isDarkThemeSelected();

        // Ρυθμίσεις για το μέγεθος γραμματοσειράς
        int fontSize = preferencesManager.getFontSize();
        etName.setTextSize(fontSize);

        // Εφαρμογή του μεγέθους γραμματοσειράς σε όλα τα κουμπιά και views του ProfileFragment
        applyFontSizeToViews(view, fontSize);

        SeekBar fontSizeSeekBar = view.findViewById(R.id.sb_font_size);
        fontSizeSeekBar.setProgress(fontSize);
    }

    private void savePreferences(View view) {
        PreferencesManager preferencesManager = new PreferencesManager(requireContext());
        preferencesManager.setName(etName.getText().toString());
        preferencesManager.setDarkThemeSelected(isDarkThemeSelected);

        SeekBar fontSizeSeekBar = view.findViewById(R.id.sb_font_size);
        int fontSize = fontSizeSeekBar.getProgress();
        preferencesManager.setFontSize(fontSize);

        // Εφαρμογή του μεγέθους γραμματοσειράς σε όλα τα κουμπιά και views του ProfileFragment
        applyFontSizeToViews(view, fontSize);

        Toast.makeText(requireContext(), "Οι ρυθμίσεις αποθηκεύτηκαν.", Toast.LENGTH_SHORT).show();
    }
    private void applyFontSizeToViews(View view, int fontSize) {
        // Εφαρμόζουμε το μέγεθος γραμματοσειράς στα υπόλοιπα views
        Button saveButton = view.findViewById(R.id.btn_save);
        Button logoutButton = view.findViewById(R.id.button);
        TextView beautifulTextView = view.findViewById(R.id.beautiful_textview);

        saveButton.setTextSize(fontSize);
        logoutButton.setTextSize(fontSize);
        beautifulTextView.setTextSize(fontSize);

        RadioGroup rgColors = view.findViewById(R.id.rg_colors);
        for (int i = 0; i < rgColors.getChildCount(); i++) {
            View child = rgColors.getChildAt(i);
            if (child instanceof RadioButton) {
                ((RadioButton) child).setTextSize(fontSize);
            }
        }
        // Μπορείτε να προσθέσετε και άλλα views αν χρειάζεται
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