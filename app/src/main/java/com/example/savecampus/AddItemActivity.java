package com.example.savecampus;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;import android.util.Base64;
import android.widget.ArrayAdapter; // <-- ADD THIS IMPORT
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner; // <-- ADD THIS IMPORT
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class AddItemActivity extends AppCompatActivity {

    // --- MODIFIED ---
    // Change typeEditText to typeSpinner
    private EditText nameEditText, priceEditText;
    private Spinner typeSpinner; // <-- CHANGED
    private ImageView itemImageView;
    private Button takePhotoButton, addButton;

    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Intent> openCameraLauncher;

    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item); // Ensure this matches your XML file name

        // --- MODIFIED ---
        // Find the new Spinner by its ID
        nameEditText = findViewById(R.id.nameEditText);
        priceEditText = findViewById(R.id.priceEditText);
        typeSpinner = findViewById(R.id.typeSpinner); // <-- CHANGED
        itemImageView = findViewById(R.id.itemImageView);
        takePhotoButton = findViewById(R.id.takePhotoButton);
        addButton = findViewById(R.id.addButton);

        // --- ADDED ---
        // Setup the Spinner with item types
        setupSpinner();

        setupLaunchers();

        takePhotoButton.setOnClickListener(v -> handleTakePhoto());
        addButton.setOnClickListener(v -> handleAddItem());
    }

    // --- NEW METHOD ---
    // This method populates the spinner with data from strings.xml
    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.item_types, // This array must exist in your res/values/strings.xml
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
    }

    private void setupLaunchers() {
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
                    }
                });

        openCameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            imageBitmap = (Bitmap) extras.get("data");
                            itemImageView.setImageBitmap(imageBitmap);
                        }
                    }
                });
    }

    private void handleTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        openCameraLauncher.launch(cameraIntent);
    }

    private void handleAddItem() {
        final String name = nameEditText.getText().toString().trim();
        final String price = priceEditText.getText().toString().trim();

        // --- MODIFIED ---
        // Get the selected item from the Spinner
        final String type = typeSpinner.getSelectedItem().toString(); // <-- CHANGED

        if (name.isEmpty() || price.isEmpty()) { // No need to check type, a value is always selected
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageBitmap == null) {
            Toast.makeText(this, "Please take a photo", Toast.LENGTH_SHORT).show();
            return;
        }

        String imageData = bitmapToBase64(imageBitmap);
        addItem(name, price, type, imageData);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void addItem(final String name, final String price, final String type, final String imageData) {
        String url = "http://10.0.2.2/mobileApp/add_item.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        Toast.makeText(this, "Adding item...", Toast.LENGTH_LONG).show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
                    if (response.toLowerCase().contains("success")) {
                        setResult(RESULT_OK); // Signal success to the previous screen
                        finish(); // Close this activity
                    }
                },
                error -> {
                    String errorMessage = "Unknown error";
                    if (error instanceof TimeoutError) errorMessage = "Connection timed out";
                    else if (error instanceof ServerError) errorMessage = "Server error";
                    else if (error instanceof NetworkError) errorMessage = "Network error";
                    Toast.makeText(this, "Upload Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("price", price);
                params.put("type", type);
                params.put("image", imageData);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(20000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }
}
