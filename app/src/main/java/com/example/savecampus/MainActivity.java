package com.example.savecampus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.savecampus.databinding.ActivityMainBinding;
import com.example.savecampus.ui.home.HomeFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.json.JSONArray;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SearchView searchView;
    private ActivityResultLauncher<Intent> addMealLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(
                        R.id.navigation_home,
                        R.id.navigation_notifications
                ).build();

        NavigationUI.setupWithNavController(binding.navView, navController);

        // Setup ActivityResultLauncher for adding meals
        addMealLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Meal was added successfully, refresh HomeFragment
                        refreshHomeFragment();
                    }
                }
        );

        // --- Admin-only FAB logic ---
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        SharedPreferences prefs =
                getSharedPreferences("SaveCampusPrefs", MODE_PRIVATE);

        String role = prefs.getString("role", "student");

        if (!"staff".equals(role)) {
            fabAdd.setVisibility(View.GONE);
        }

        fabAdd.setOnClickListener(v ->
                addMealLauncher.launch(new Intent(MainActivity.this, AddItemActivity.class))
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        
        searchView.setQueryHint("Search meals...");
        
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });
        
        return true;
    }

    private void performSearch(String query) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        
        if (navController.getCurrentDestination() != null && 
            navController.getCurrentDestination().getId() == R.id.navigation_home) {
            
            androidx.fragment.app.Fragment navHostFragment = getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
                
            if (navHostFragment != null) {
                for (androidx.fragment.app.Fragment fragment : navHostFragment.getChildFragmentManager().getFragments()) {
                    if (fragment instanceof HomeFragment && fragment.isVisible()) {
                        ((HomeFragment) fragment).filterMeals(query);
                        break;
                    }
                }
            }
        }
    }

    private void refreshHomeFragment() {
        androidx.fragment.app.Fragment navHostFragment = getSupportFragmentManager()
            .findFragmentById(R.id.nav_host_fragment_activity_main);
            
        if (navHostFragment != null) {
            for (androidx.fragment.app.Fragment fragment : navHostFragment.getChildFragmentManager().getFragments()) {
                if (fragment instanceof HomeFragment) {
                    ((HomeFragment) fragment).loadMealsPublic();
                    break;
                }
            }
        }
    }
}
