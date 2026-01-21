package com.example.savecampus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    // (Keep your password patterns here)
    private static final Pattern PASSWORD_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern PASSWORD_SPECIAL_CHAR = Pattern.compile("[@#]");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView tvRegister = findViewById(R.id.btnRegister);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etUsername = findViewById(R.id.etUserName);
        EditText etPassword = findViewById(R.id.etPassword);

        tvRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String username = etUsername.getText().toString().trim(); // <-- Add this
            String password = etPassword.getText().toString().trim();
            if (username.isEmpty()) { // <-- Add validation for username
                etUsername.setError("Username cannot be empty.");
                return;
            }
            if (!isValidEmail(email)) {
                etEmail.setError("Please enter a valid @gmail.com address.");
                return;
            }

            if (!isValidPassword(password)) {
                etPassword.setError("Password must be at least 8 characters, with one uppercase letter and one special character (@, #).");
                return;
            }

            // If validation passes, call the registration method
            registerUser(email ,username, password);
        });
    }

    /**
     * Sends user credentials to the server for registration.
     * @param email The user's email.
     * @param password The user's password.
     */
    private void registerUser(final String email, final String username, final String password) {
        // 1. REMOVE the parameters from the URL.
        // The URL should only point to the script itself.
        String url = "http://10.0.2.2/mobileApp/register.php";

        // Create a new request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // 2. The request type is already POST, which is correct.
        // In your registerUser method...
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                // In StringRequest...
                response -> {
                    // Restore your JSON parsing logic
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean success = jsonResponse.getBoolean("success");

                        if (success) {
                            Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish(); // Close the register activity
                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e("RegisterActivity", "JSON Parsing error: " + e.getMessage());
                        Toast.makeText(RegisterActivity.this, "Invalid response from server.", Toast.LENGTH_LONG).show();
                    }
                },
                // ...

                error -> { //... the rest of your code remains the same

                    // This block is executed if there's a network error
                    Log.e("RegisterActivity", "Volley error: " + error.toString());
                    Toast.makeText(RegisterActivity.this, "Registration failed. Please check your internet connection.", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                // 3. This part is correct. It sends the email and password in the POST body.
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };

        // Add the request to the queue to execute it
        queue.add(stringRequest);
    }

    private boolean isValidEmail(String email) {
        return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.endsWith("@gmail.com");
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8) return false;
        if (!PASSWORD_UPPERCASE.matcher(password).find()) return false;
        if (!PASSWORD_SPECIAL_CHAR.matcher(password).find()) return false;
        return true;
    }
}
