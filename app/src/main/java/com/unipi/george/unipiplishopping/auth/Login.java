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

public class Login extends AppCompatActivity {

    private EditText emailText, passwordText;
    private Button loginButton;

    @Override
    public void onStart() {
        super.onStart();
        AuthManager.checkUserLoggedIn(this);
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

        initializeUI();

        loginButton.setOnClickListener(v -> {
            String email = emailText.getText().toString();
            String password = passwordText.getText().toString();

            if (validateInput(email, password)) {
                loginUser(email, password);
            }
        });
    }

    private void initializeUI() {
        emailText = findViewById(R.id.emailText1);
        passwordText = findViewById(R.id.passwordText1);
        loginButton = findViewById(R.id.loginButton);
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void loginUser(String email, String password) {
        AuthManager.loginUser(this, email, password, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(Login.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                gotoMainActivity();
            }

            @Override
            public void onFailure() {
                Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void gotoRegister(View view) {
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
    }
}
