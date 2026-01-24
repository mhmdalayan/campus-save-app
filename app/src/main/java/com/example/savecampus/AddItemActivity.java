package com.example.savecampus;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddItemActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1001;
    private static final int CAMERA_REQUEST = 1002;
    private static final int CAMERA_PERMISSION_REQUEST = 2001;

    EditText etMealName, etAvailablePortions, etExpiresAt, etDescription;
    Button btnSaveMeal, btnPickImage, btnCaptureImage;
    ImageView imgMeal;

    String imageBase64 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        etMealName = findViewById(R.id.etMealName);
        etAvailablePortions = findViewById(R.id.etAvailablePortions);
        etExpiresAt = findViewById(R.id.etExpiresAt);
        etDescription = findViewById(R.id.etDescription);

        btnSaveMeal = findViewById(R.id.btnSaveMeal);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnCaptureImage = findViewById(R.id.btnCaptureImage);

        imgMeal = findViewById(R.id.imgMeal);

        etExpiresAt.setOnClickListener(v -> showDateTimePicker());
        btnPickImage.setOnClickListener(v -> openGallery());
        btnCaptureImage.setOnClickListener(v -> checkCameraPermission());
        btnSaveMeal.setOnClickListener(v -> saveMeal());
    }

    /* ---------------- CAMERA PERMISSION ---------------- */

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this,
                        "Camera permission denied",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /* ---------------- IMAGE PICK ---------------- */

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (resultCode != RESULT_OK || data == null) return;

            if (requestCode == PICK_IMAGE_REQUEST) {
                Uri imageUri = data.getData();
                imgMeal.setImageURI(imageUri);
                imageBase64 = convertUriToBase64(imageUri);

            } else if (requestCode == CAMERA_REQUEST) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                imgMeal.setImageBitmap(bitmap);
                imageBase64 = convertBitmapToBase64(bitmap);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Image error", Toast.LENGTH_SHORT).show();
        }
    }

    /* ---------------- DATE & TIME ---------------- */

    private void showDateTimePicker() {
        Calendar c = Calendar.getInstance();

        new DatePickerDialog(
                this,
                (v, y, m, d) -> new TimePickerDialog(
                        this,
                        (tv, h, min) -> etExpiresAt.setText(
                                String.format(
                                        Locale.US,
                                        "%04d-%02d-%02d %02d:%02d:00",
                                        y, m + 1, d, h, min
                                )
                        ),
                        c.get(Calendar.HOUR_OF_DAY),
                        c.get(Calendar.MINUTE),
                        true
                ).show(),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    /* ---------------- SAVE MEAL ---------------- */

    private void saveMeal() {

        String name = etMealName.getText().toString().trim();
        String portions = etAvailablePortions.getText().toString().trim();
        String expiresAt = etExpiresAt.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (name.isEmpty() || portions.isEmpty()
                || expiresAt.isEmpty() || imageBase64 == null) {
            Toast.makeText(this,
                    "Fill all fields and add image",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs =
                getSharedPreferences("SaveCampusPrefs", MODE_PRIVATE);

        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this,
                    "Session expired",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String url = "http://10.0.2.2/mobileApp/adddish.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            Toast.makeText(this,
                                    "Meal added successfully",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this,
                                    json.getString("message"),
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this,
                                "Invalid server response",
                                Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this,
                        "Network error",
                        Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("user_id", String.valueOf(userId));
                p.put("name", name);
                p.put("description", description);
                p.put("available_portions", portions);
                p.put("expires_at", expiresAt);
                p.put("image_base64", imageBase64);
                return p;
            }
        };

        queue.add(request);
    }

    /* ---------------- BASE64 HELPERS ---------------- */

    private String convertUriToBase64(Uri uri) throws Exception {
        InputStream in = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int n;
        while ((n = in.read(data)) != -1) buffer.write(data, 0, n);
        in.close();
        return Base64.encodeToString(buffer.toByteArray(), Base64.DEFAULT);
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }
}
