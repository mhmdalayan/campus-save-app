package com.example.savecampus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

// We only need to import the R class and the binding class from our package.
// CampusItem and NewCustomAdapter are found automatically.
import com.example.savecampus.CampusItem;
import com.example.savecampus.NewCustomAdapter;
import com.example.savecampus.R;
import com.example.savecampus.databinding.FragmentFirstBinding;

import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Create a list of your new CampusItem objects
        List<CampusItem> items = new ArrayList<>();
        // Now R.drawable will be resolved correctly
        items.add(new CampusItem("Old Textbook", "$15.00", "Slightly used, good condition.", R.drawable.css));
        items.add(new CampusItem("Desk Lamp", "$10.00", "Works perfectly, bulb included.", R.drawable.js));
        items.add(new CampusItem("Mini Fridge", "$50.00", "Keeps drinks cold. Great for a dorm room.", R.drawable.php));
        items.add(new CampusItem("Skateboard", "$25.00", "Barely used, a few scratches.", R.drawable.python));
        // Add more items here

        // 2. Create and set the adapter with the new item list
        // No error here, because NewCustomAdapter is in the same package
        NewCustomAdapter adapter = new NewCustomAdapter(requireContext(), items);
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
