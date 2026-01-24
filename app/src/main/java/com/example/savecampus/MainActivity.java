package com.example.savecampus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.savecampus.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(
                        R.id.navigation_home,
                        R.id.navigation_notifications
                ).build();

        NavigationUI.setupWithNavController(binding.navView, navController);

        // --- Admin-only FAB logic ---
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        SharedPreferences prefs =
                getSharedPreferences("SaveCampusPrefs", MODE_PRIVATE);

        String role = prefs.getString("role", "student");

        if (!"staff".equals(role)) {
            fabAdd.setVisibility(View.GONE);
        }

        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddItemActivity.class))
        );
    }
}
