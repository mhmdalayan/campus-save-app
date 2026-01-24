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
        setContentView(R.layout.activity_login);

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        TextView btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoToRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(email, password);
        });

        tvGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    private void loginUser(final String email, final String password) {

        String url = "http://10.0.2.2/mobileApp/login.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);

                        if (json.getBoolean("success")) {

                            int userId = json.getInt("user_id"); // ✅ IMPORTANT
                            String name = json.getString("name");
                            String role = json.getString("role");

                            SharedPreferences prefs =
                                    getSharedPreferences("SaveCampusPrefs", MODE_PRIVATE);

                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("user_id", userId);     // ✅ FIX
                            editor.putString("logged_in_email", email);
                            editor.putString("username", name);
                            editor.putString("role", role);
                            editor.apply();

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Welcome, " + name,
                                    Toast.LENGTH_SHORT
                            ).show();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(
                                    LoginActivity.this,
                                    json.getString("message"),
                                    Toast.LENGTH_LONG
                            ).show();
                        }

                    } catch (JSONException e) {
                        Log.e("LoginActivity", "JSON error: " + e.getMessage());
                        Toast.makeText(
                                LoginActivity.this,
                                "Invalid server response",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                },
                error -> {
                    Log.e("LoginActivity", "Volley error: " + error.toString());
                    Toast.makeText(
                            LoginActivity.this,
                            "Login failed. Check internet connection.",
                            Toast.LENGTH_LONG
                    ).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        queue.add(request);
    }
}
