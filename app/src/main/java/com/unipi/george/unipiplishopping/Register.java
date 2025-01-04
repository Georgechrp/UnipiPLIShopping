package com.unipi.george.unipiplishopping;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity {
    EditText emailText2, passwordText2;
    Button registerButton;

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance(); // Αρχικοποίηση του mAuth
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Έλεγχος εάν υπάρχει ήδη συνδεδεμένος χρήστης
        if (currentUser != null) {
            Intent intent = new Intent(Register.this, MainActivity.class);
            startActivity(intent); // Μεταφορά στην κύρια δραστηριότητα
        }
    }
    FirebaseAuth mAuth; // Διαχείριση Firebase Authentication
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        emailText2 = findViewById(R.id.emailText2);
        passwordText2 = findViewById(R.id.passwordText2);
        registerButton = findViewById(R.id.registerButton);

        // listener για το κουμπί εγγραφής
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailText2.getText().toString();
                String password = passwordText2.getText().toString();

                // Έλεγχοι εγκυρότητας εισόδων
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(Register.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(Register.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 6) {
                    Toast.makeText(Register.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                } else {
                    // Δημιουργία νέου χρήστη μέσω Firebase
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Αν η εγγραφή ήταν επιτυχής
                                        Toast.makeText(Register.this, "Authentication Successful.", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(Register.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // Εμφάνιση μηνύματος σφάλματος αν η εγγραφή αποτύχει
                                        Toast.makeText(Register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
    public void gotoLogin(View view){
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }
}