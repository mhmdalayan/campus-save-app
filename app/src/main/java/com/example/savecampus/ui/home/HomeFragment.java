package com.example.savecampus.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.savecampus.ItemAdapter;
import com.example.savecampus.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ItemAdapter adapter;
    private final List<JSONObject> allMeals = new ArrayList<>();
    private final List<JSONObject> filteredMeals = new ArrayList<>();

    private Handler refreshHandler;
    private Runnable refreshRunnable;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rv_items);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ItemAdapter(requireContext(), true, true);
        recyclerView.setAdapter(adapter);

        loadMeals(); // load once

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        startAutoRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoRefresh();
    }

    private void startAutoRefresh() {
        if (refreshHandler == null) {
            refreshHandler = new Handler(Looper.getMainLooper());
        }

        refreshRunnable = () -> {
            loadMeals();
            refreshHandler.postDelayed(refreshRunnable, 30000);
        };

        refreshHandler.postDelayed(refreshRunnable, 30000);
    }

    private void stopAutoRefresh() {
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

    private void loadMeals() {

        Context ctx = getContext();
        if (ctx == null) return; // 🔒 avoid crash

        String url = "http://10.0.2.2/mobileApp/get_meals.php";
        RequestQueue queue = Volley.newRequestQueue(ctx);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        JSONArray mealsArray;

                        String trimmed = response.trim();
                        if (trimmed.startsWith("[")) {
                            // old API format
                            mealsArray = new JSONArray(trimmed);
                        } else {
                            // new API format
                            JSONObject obj = new JSONObject(trimmed);
                            if (!obj.optBoolean("success")) return;
                            mealsArray = obj.optJSONArray("meals");
                            if (mealsArray == null) mealsArray = new JSONArray();
                        }

                        allMeals.clear();
                        for (int i = 0; i < mealsArray.length(); i++) {
                            allMeals.add(mealsArray.getJSONObject(i));
                        }

                        filteredMeals.clear();
                        filteredMeals.addAll(allMeals);
                        adapter.setItems(filteredMeals);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        );

        queue.add(request);
    }

    public void filterMeals(String query) {
        filteredMeals.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredMeals.addAll(allMeals);
        } else {
            String lower = query.toLowerCase();
            for (JSONObject meal : allMeals) {
                try {
                    if (meal.optString("name", "")
                            .toLowerCase()
                            .contains(lower)) {
                        filteredMeals.add(meal);
                    }
                } catch (Exception ignored) {}
            }
        }

        adapter.setItems(filteredMeals);
    }
}
