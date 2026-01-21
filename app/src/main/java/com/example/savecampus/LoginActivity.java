package com.example.savecampus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Assuming this is your layout file

        // Assuming these are the IDs in your activity_login.xml
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        TextView btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoToRegister = findViewById(R.id.tvRegister); // Optional: Link to RegisterActivity

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            // If validation passes, call the login method
            loginUser(email, password);
        });

        // Optional: Set up a click listener to go back to the registration screen
        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Sends credentials to the server for authentication.
     * @param email The user's email.
     * @param password The user's password.
     */
    private void loginUser(final String email, final String password) {
        String url = "http://10.0.2.2/mobileApp/login.php"; // Point to the new login script
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean success = jsonResponse.getBoolean("success");

                        if (success) {
                            // Login was successful
                            String username = jsonResponse.getString("username");
                            Toast.makeText(LoginActivity.this, "Welcome, " + username + "!", Toast.LENGTH_SHORT).show();

                            // Save the email to SharedPreferences to check permissions later
                            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            prefs.edit().putString("logged_in_email", email).apply();

                            // Navigate to your main app screen (e.g., MainActivity or HomeActivity)
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            // Optional: Clear the activity stack so the user can't go back to the login screen
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish(); // Close the login activity

                        } else {
                            // Login failed, show the error message from the server
                            String message = jsonResponse.getString("message");
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e("LoginActivity", "JSON Parsing error: " + e.getMessage());
                        Toast.makeText(LoginActivity.this, "Invalid response from server.", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    // Handle network errors
                    Log.e("LoginActivity", "Volley error: " + error.toString());
                    Toast.makeText(LoginActivity.this, "Login failed. Please check your internet connection.", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Send email and password as POST parameters
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        // Add the request to the queue to execute it
        queue.add(stringRequest);
    }
}
