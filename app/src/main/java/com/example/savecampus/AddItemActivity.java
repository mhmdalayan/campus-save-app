package com.example.savecampus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddItemActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item); // This displays the layout

        // Find the views from your XML layout
        EditText etName = findViewById(R.id.etItemName);
        EditText etPrice = findViewById(R.id.etItemPrice);
        EditText etDesc = findViewById(R.id.etDescription);
        Button btnSave = findViewById(R.id.btnSave);

        // Set a click listener on the "Save" button
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String price = etPrice.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();

            // Validate that the name and price are not empty
            if (name.isEmpty() || price.isEmpty()) {
                Toast.makeText(this, "Please enter at least a name and price.", Toast.LENGTH_SHORT).show();
                return; // This return is correct: it stops execution only IF validation fails
            }

            // Prepare the data to be sent back to the previous screen (HomeFragment/MainActivity)
            Intent resultIntent = new Intent();
            resultIntent.putExtra("name", name);
            resultIntent.putExtra("price", price);
            resultIntent.putExtra("description", desc);

            setResult(RESULT_OK, resultIntent);
            finish(); // Close this activity and return to the list
        });
    }
}