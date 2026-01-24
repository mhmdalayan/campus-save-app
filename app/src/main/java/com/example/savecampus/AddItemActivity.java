package com.example.savecampus;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddItemActivity extends AppCompatActivity {

    EditText etMealName, etAvailablePortions, etExpiresAt, etDescription;
    Button btnSaveMeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        etMealName = findViewById(R.id.etMealName);
        etAvailablePortions = findViewById(R.id.etAvailablePortions);
        etExpiresAt = findViewById(R.id.etExpiresAt);
        etDescription = findViewById(R.id.etDescription);
        btnSaveMeal = findViewById(R.id.btnSaveMeal);

        // ✅ Disable keyboard for expiry field
        etExpiresAt.setFocusable(false);
        etExpiresAt.setClickable(true);

        // ✅ Proper Date + Time picker flow
        etExpiresAt.setOnClickListener(v -> showDateTimePicker());

        btnSaveMeal.setOnClickListener(v -> {

            String name = etMealName.getText().toString().trim();
            String portionsStr = etAvailablePortions.getText().toString().trim();
            String expiresAt = etExpiresAt.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (name.isEmpty() || portionsStr.isEmpty() || expiresAt.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int availablePortions = Integer.parseInt(portionsStr);

            SharedPreferences prefs =
                    getSharedPreferences("SaveCampusPrefs", MODE_PRIVATE);

            int userId = prefs.getInt("user_id", -1);

            if (userId == -1) {
                Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            addMeal(userId, name, description, availablePortions, expiresAt);
        });
    }

    // ✅ CLEAN date → time picker (no bugs)
    private void showDateTimePicker() {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AddItemActivity.this,
                (view, year, month, dayOfMonth) -> {

                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            AddItemActivity.this,
                            (timeView, hourOfDay, minute) -> {

                                String formattedDateTime = String.format(
                                        Locale.US,
                                        "%04d-%02d-%02d %02d:%02d:00",
                                        year,
                                        month + 1,
                                        dayOfMonth,
                                        hourOfDay,
                                        minute
                                );

                                etExpiresAt.setText(formattedDateTime);
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    );

                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void addMeal(
            int userId,
            String name,
            String description,
            int availablePortions,
            String expiresAt
    ) {

        String url = "http://10.0.2.2/mobileApp/adddish.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            Toast.makeText(this, "Meal added successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, json.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Invalid server response", Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "Network error", Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("name", name);
                params.put("description", description);
                params.put("available_portions", String.valueOf(availablePortions));
                params.put("expires_at", expiresAt);
                return params;
            }
        };

        queue.add(request);
    }
}
