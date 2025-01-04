package com.unipi.george.unipiplishopping;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(requireContext(), "Άδεια για ειδοποιήσεις δόθηκε.", Toast.LENGTH_SHORT).show();
                    showNotification();
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

    private void showNotification() {
        NotificationHelper notificationHelper = new NotificationHelper(requireContext());
        notificationHelper.sendSimpleNotification("Κοντινό μαγαζί", "Κοντά σας βρίσκεται το προιον!");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Έλεγχος άδειας και αίτηση εάν χρειάζεται
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        Button btnSendNotification = view.findViewById(R.id.btnSendNotification);
        btnSendNotification.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Ζήτησε άδεια αν δεν υπάρχει
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                // Εμφάνιση ειδοποίησης αν η άδεια έχει ήδη δοθεί
                showNotification();
            }
        });

        // Εύρεση του TextView
        TextView textView = view.findViewById(R.id.beautiful_textview);
        textView.setText("Καλωσόρισες στο προφίλ!");

        // Εύρεση του Switch για τοποθεσία
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch locationSwitch = view.findViewById(R.id.location_switch);

        if (getActivity() instanceof MainActivity) {
            boolean isPermissionGranted = ((MainActivity) getActivity()).checkLocationPermission();
            locationSwitch.setChecked(isPermissionGranted);
        }

        locationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                if (isChecked) {
                    if (!mainActivity.checkLocationPermission()) {
                        mainActivity.requestLocationPermission();
                    }
                } else {
                    Toast.makeText(getContext(), "Η χρήση τοποθεσίας απενεργοποιήθηκε.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Ρύθμιση του κουμπιού για αποσύνδεση
        view.findViewById(R.id.button).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).callSignOut(v);
            }
        });

        return view;
    }
}
