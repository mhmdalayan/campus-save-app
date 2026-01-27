package com.example.savecampus.ui.home;

import android.os.Bundle;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.savecampus.ItemAdapter;
import com.example.savecampus.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ItemAdapter adapter;
    private List<JSONObject> allMeals = new ArrayList<>();
    private List<JSONObject> filteredMeals = new ArrayList<>();

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

        adapter = new ItemAdapter(requireContext() , true , true);
        recyclerView.setAdapter(adapter);

        loadMeals();

        return view;
    }

    private void loadMeals() {

        String url = "http://10.0.2.2/mobileApp/get_meals.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {

                            JSONArray mealsArray = response.getJSONArray("meals");
                            allMeals.clear();

                            for (int i = 0; i < mealsArray.length(); i++) {
                                allMeals.add(mealsArray.getJSONObject(i));
                            }

                            filteredMeals.clear();
                            filteredMeals.addAll(allMeals);
                            adapter.setItems(filteredMeals);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        );

        queue.add(request);
    }

    public void filterMeals(String query) {
        if (query == null || query.trim().isEmpty()) {
            filteredMeals.clear();
            filteredMeals.addAll(allMeals);
        } else {
            filteredMeals.clear();
            String lowerQuery = query.toLowerCase();
            
            for (JSONObject meal : allMeals) {
                try {
                    String mealName = meal.getString("name").toLowerCase();
                    if (mealName.contains(lowerQuery)) {
                        filteredMeals.add(meal);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        if (adapter != null) {
            adapter.setItems(filteredMeals);
        }
    }
}
