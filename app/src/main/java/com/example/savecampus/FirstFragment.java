package com.example.savecampus;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FirstFragment extends Fragment {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private RequestQueue queue;
    private JSONArray staticItems; // Store our static items

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // This is correct: it inflates the layout with the button.
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Volley
        queue = Volley.newRequestQueue(requireContext());

        // Find the RecyclerView from the layout
        recyclerView = view.findViewById(R.id.rv_items);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Find the FloatingActionButton and set its click listener.
        FloatingActionButton fab = view.findViewById(R.id.fab_add_item);
        fab.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddItemActivity.class));
        });

        // **NEW:** Create the static list first
        createStaticItems();

        // *** THIS IS THE FIX ***
        // The constructor for ItemAdapter now only takes two arguments.
        // The extra 'new JSONArray()' has been removed.
        adapter = new ItemAdapter(requireContext(), item -> {
            CartManager.getInstance().addItem(item);
            Toast.makeText(getContext(), "Added to cart", Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fetch data from the server every time the screen is shown.
        fetchItemsFromServer();
    }

    /**
     * NEW: Creates the hard-coded list of 4 items.
     * These will be combined with the server list later.
     */
    private void createStaticItems() {
        staticItems = new JSONArray();
        try {
            // These IDs are negative to avoid any conflict with database IDs
            staticItems.put(new JSONObject()
                    .put("id", "-1")
                    .put("name", "Old Textbook")
                    .put("price", "15.00")
                    .put("type", "Slightly used, good condition."));

            staticItems.put(new JSONObject()
                    .put("id", "-2")
                    .put("name", "Desk Lamp")
                    .put("price", "10.00")
                    .put("type", "Works perfectly, bulb included."));

            staticItems.put(new JSONObject()
                    .put("id", "-3")
                    .put("name", "Mini Fridge")
                    .put("price", "50.00")
                    .put("type", "Keeps drinks cold. Great for a dorm room."));

            staticItems.put(new JSONObject()
                    .put("id", "-4")
                    .put("name", "Skateboard")
                    .put("price", "25.00")
                    .put("type", "Barely used, a few scratches."));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fetchItemsFromServer() {
        // This URL points to your PHP script to get all items.
        String url = "http://10.0.2.2/mobileApp/get_items.php";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    // **NEW:** On success, merge the lists
                    combineListsAndUpdateAdapter(response);
                },
                error -> {
                    // On failure, still show the static items
                    Toast.makeText(getContext(), "Couldn't fetch new items. Showing defaults.", Toast.LENGTH_SHORT).show();
                    adapter.updateData(staticItems);
                }
        );
        // Add the request to the queue to execute it.
        queue.add(request);
    }

    /**
     * NEW: Combines the static list and the server list into one.
     * @param serverItems The JSONArray that came from the Volley request.
     */
    private void combineListsAndUpdateAdapter(JSONArray serverItems) {
        JSONArray combinedList = new JSONArray();
        try {
            // First, add all static items
            for (int i = 0; i < staticItems.length(); i++) {
                combinedList.put(staticItems.get(i));
            }
            // Second, add all items from the server
            for (int i = 0; i < serverItems.length(); i++) {
                combinedList.put(serverItems.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Finally, update the adapter with the single, combined list
        adapter.updateData(combinedList);
    }
}
