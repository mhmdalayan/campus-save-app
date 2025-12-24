package com.example.savecampus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    // Define password validation patterns as constants
    private static final Pattern PASSWORD_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern PASSWORD_SPECIAL_CHAR = Pattern.compile("[@#]");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView tvRegister = findViewById(R.id.btnRegister);
        EditText etEmail = findViewById(R.id.etEmail);
        // Get the new password EditText from the layout
        EditText etPassword = findViewById(R.id.etPassword);

        tvRegister.setOnClickListener( v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validate email first
            if (!isValidEmail(email)) {
                etEmail.setError("Please enter a valid @gmail.com address.");
                Toast.makeText(RegisterActivity.this, "Invalid email format.", Toast.LENGTH_SHORT).show();
                return; // Stop the process if email is invalid
            }

            // Validate password
            if (!isValidPassword(password)) {
                etPassword.setError("Password must be at least 8 characters, with one uppercase letter and one special character (@, #).");
                Toast.makeText(RegisterActivity.this, "Invalid password format.", Toast.LENGTH_LONG).show();
                return; // Stop the process if password is invalid
            }

            // Both email and password are valid, proceed with registration
            Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Validates the email address.
     *
     * @param email The email string to validate.
     * @return true if the email is valid and ends with "@gmail.com", false otherwise.
     */
    private boolean isValidEmail(String email) {
        return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.endsWith("@gmail.com");
    }

    /**
     * Validates the password based on specific criteria.
     * - At least 8 characters long.
     * - Contains at least one uppercase letter.
     * - Contains at least one special character from the set (@, #).
     *
     * @param password The password string to validate.
     * @return true if the password is valid, false otherwise.
     */
    private boolean isValidPassword(String password) {
        if (password.length() < 8) {
            return false; // Check for minimum length
        }
        if (!PASSWORD_UPPERCASE.matcher(password).find()) {
            return false; // Check for an uppercase letter
        }
        if (!PASSWORD_SPECIAL_CHAR.matcher(password).find()) {
            return false; // Check for a special character
        }
        return true; // Password is valid
    }
}
