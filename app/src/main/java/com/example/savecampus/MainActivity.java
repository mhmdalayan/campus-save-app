package com.example.savecampus;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.savecampus.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // The binding variable will hold direct references to all views in your layout.
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This is correct and necessary to prevent issues with stale cart data.
        CartManager.getInstance().clearCart();

        // 1. Inflate the layout using the generated binding class.
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // 2. Set the content view to the root of the inflated layout.
        setContentView(binding.getRoot());

        // The AppBarConfiguration defines your top-level navigation destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();

        // Find the NavController, which manages fragment transactions within the NavHost.
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        // *** THIS IS THE ONLY LINE NEEDED FOR NAVIGATION SETUP ***
        // It connects the NavController to your BottomNavigationView.
        // We use 'binding.navView' which is the safe, direct reference to the
        // BottomNavigationView with the ID 'nav_view' in your XML.
        // The problematic 'findViewById' is completely removed.
        NavigationUI.setupWithNavController(binding.navView, navController);
    }
}
