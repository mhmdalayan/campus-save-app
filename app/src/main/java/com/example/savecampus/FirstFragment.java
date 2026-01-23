package com.example.savecampus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

// We only need to import the R class and the binding class from our package.
// CampusItem and NewCustomAdapter are found automatically.
import com.example.savecampus.databinding.FragmentFirstBinding;

import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private NewCustomAdapter adapter;
    private ActivityResultLauncher<Intent> addItemLauncher;
    private static List<CampusItem> items; // Make this static to persist data

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- ADMIN CHECK ---
        SharedPreferences prefs = requireActivity().getSharedPreferences("SaveCampusPrefs", Context.MODE_PRIVATE);
        String currentUserEmail = prefs.getString("logged_in_email", "");

        // REPLACE "admin@gmail.com" with the specific email you want to allow
        if ("admin@gmail.com".equals(currentUserEmail)) {
            binding.fabAddItem.setVisibility(View.VISIBLE);
        } else {
            binding.fabAddItem.setVisibility(View.GONE);
        }

        // Initialize the ActivityResultLauncher to handle the data returned from AddItemActivity
        addItemLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        String name = data.getStringExtra("name");
                        String price = data.getStringExtra("price");
                        String description = data.getStringExtra("description");

                        // Create the new item (Using a default image for now)
                        CampusItem newItem = new CampusItem(name, price, description, R.drawable.js);
                        items.add(0, newItem); // Add to the static list so it is saved
                        adapter.addItem(newItem);
                        binding.recyclerView.scrollToPosition(0);
                        Toast.makeText(requireContext(), "Item Added!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // 1. Initialize the list only if it is null (first time app runs)
        if (items == null) {
            items = new ArrayList<>();
            // Now R.drawable will be resolved correctly
            items.add(new CampusItem("Old Textbook", "$15.00", "Slightly used, good condition.", R.drawable.css));
            items.add(new CampusItem("Desk Lamp", "$10.00", "Works perfectly, bulb included.", R.drawable.js));
            items.add(new CampusItem("Mini Fridge", "$50.00", "Keeps drinks cold. Great for a dorm room.", R.drawable.php));
            items.add(new CampusItem("Skateboard", "$25.00", "Barely used, a few scratches.", R.drawable.python));
        }

        // 2. Create and set the adapter with the new item list
        // No error here, because NewCustomAdapter is in the same package
        adapter = new NewCustomAdapter(requireContext(), items);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        // 3. Setup the search filter (this remains the same)
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        // Set the text for the header
        binding.headerTextView.setText("Items Available on Campus");

        // Handle Floating Action Button Click
        binding.fabAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddItemActivity.class);
            addItemLauncher.launch(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
