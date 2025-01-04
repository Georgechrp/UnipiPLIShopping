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

public class Login extends AppCompatActivity {
    FirebaseAuth mAuth;
    EditText emailText, passwordText;
    Button loginButton;
    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance(); // Αρχικοποίηση του FirebaseAuth instance
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Έλεγχος αν υπάρχει ήδη συνδεδεμένος χρήστης
        if (currentUser != null) {
            // Αν υπάρχει συνδεδεμένος χρήστης, κατευθύνεται στο MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish(); // Κλείσιμο της Login Activity για να μην μπορεί ο χρήστης να επιστρέψει
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Αρχικοποίηση
        mAuth = FirebaseAuth.getInstance();
        emailText = findViewById(R.id.emailText1);
        passwordText = findViewById(R.id.passwordText1);
        loginButton = findViewById(R.id.loginButton);

        // όταν ο χρήστης πατάει το κουμπί login
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailText.getText().toString();
                String password = passwordText.getText().toString();

                // Έλεγχοι εγκυρότητας των πεδίων εισόδου
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(Login.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(Login.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 6) {
                    Toast.makeText(Login.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                } else {
                    // Προσπάθεια σύνδεσης με το Firebase Authentication
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Αν η σύνδεση είναι επιτυχής
                                        Toast.makeText(Login.this, "Login Successful.",
                                                Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Login.this, MainActivity.class);
                                        startActivity(intent);
                                        finish(); // Κλείσιμο της Login Activity
                                    } else {
                                        // Αν αποτύχει η σύνδεση
                                        Toast.makeText(Login.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    public void gotoRegister(View view){
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
    }
}