package com.unipi.george.unipiplishopping.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.unipi.george.unipiplishopping.MainActivity;
import com.unipi.george.unipiplishopping.R;

public class Register extends AppCompatActivity {

    private EditText emailText2, passwordText2;
    private Button registerButton;

    @Override
    public void onStart() {
        super.onStart();
        AuthManager.checkUserLoggedIn(this);
    }

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
        initializeUI();
        setRegisterButtonListener();
    }

    private void initializeUI() {
        emailText2 = findViewById(R.id.emailText2);
        passwordText2 = findViewById(R.id.passwordText2);
        registerButton = findViewById(R.id.registerButton);
    }

    // listener για το κουμπί εγγραφής
    private void setRegisterButtonListener() {
        registerButton.setOnClickListener(v -> handleRegistration());
    }

    // Ελέγχει τις εισόδους και ξεκινά την εγγραφή του χρήστη.
    private void handleRegistration() {
        String email = emailText2.getText().toString();
        String password = passwordText2.getText().toString();

        if (!validateInputs(email, password)) {
            return;
        }
        registerUser(email, password);
    }

    // Ελέγχει αν τα πεδία email και password είναι έγκυρα
    private boolean validateInputs(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(Register.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(Register.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(Register.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void registerUser(String email, String password) {
        AuthManager.registerUser(this, email, password, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(Register.this, "Registration Successful.", Toast.LENGTH_SHORT).show();
                gotoMainActivity();
            }

            @Override
            public void onFailure() {
                Toast.makeText(Register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(Register.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void gotoLogin(View view) {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }
}
