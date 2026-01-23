package com.example.savecampus.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.savecampus.AddItemActivity;
import com.example.savecampus.ItemAdapter;
import com.example.savecampus.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private RequestQueue queue;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        queue = Volley.newRequestQueue(requireContext());
        recyclerView = view.findViewById(R.id.rv_items);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        FloatingActionButton fab = view.findViewById(R.id.fab_add_item);

        // Check if the user is admin to show/hide the Add Item button
        SharedPreferences prefs = requireActivity().getSharedPreferences("SaveCampusPrefs", Context.MODE_PRIVATE);
        String currentUserEmail = prefs.getString("logged_in_email", "");
        if ("admin@gmail.com".equals(currentUserEmail)) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // The adapter's click listener defines what happens when "Take It" is pressed
        adapter = new ItemAdapter(requireContext(), item -> {
            // 2. Tell the server to delete the item permanently from the database
            deleteItemOnServer(item);
        });

        recyclerView.setAdapter(adapter);
        fab.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddItemActivity.class)));
        swipeRefreshLayout.setOnRefreshListener(this::fetchItemsFromServer);
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchItemsFromServer();
    }

    private void fetchItemsFromServer() {
        String url = "http://10.0.2.2/mobileApp/get_items.php";
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    adapter.updateData(response);
                    swipeRefreshLayout.setRefreshing(false);
                },
                error -> {
                    Toast.makeText(getContext(), "Error fetching items: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
        );
        // Disable cache to ensure we always get the latest list from the server
        jsonArrayRequest.setShouldCache(false);
        queue.add(jsonArrayRequest);
    }

    /**
     * This is the method that handles the "Take It" button's main action.
     * @param item The item that was clicked.
     */
    private void deleteItemOnServer(JSONObject item) {
        // Point to the correct PHP script for permanent deletion
        String url = "http://10.0.2.2/mobileApp/deletedish.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.optBoolean("success")) {
                            Toast.makeText(getContext(), "Item permanently removed!", Toast.LENGTH_SHORT).show();
                            adapter.removeItem(item);
                        } else {
                            String msg = jsonResponse.optString("message", "Unknown error");
                            Toast.makeText(getContext(), "Failed: " + msg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("HomeFragment", "JSON Parse Error: " + e.getMessage());
                        Toast.makeText(getContext(), "Error parsing server response.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // This code runs if the server returns an error
                    Toast.makeText(getContext(), "Error: Could not remove item. Please refresh.", Toast.LENGTH_LONG).show();
                    Log.e("HomeFragment", "Server Error: " + error.toString());
                }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                try {
                    // Send the 'id' of the item to the PHP script
                    params.put("id", item.getString("id"));
                } catch (JSONException e) {
                    Log.e("HomeFragment", "Failed to get item ID for server request", e);
                }
                return params;
            }
        };
        // Also disable cache for this request
        stringRequest.setShouldCache(false);
        queue.add(stringRequest);
    }
}
