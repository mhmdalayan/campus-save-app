package com.example.savecampus.ui.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savecampus.ItemAdapter;
import com.example.savecampus.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    RecyclerView recyclerView;
    ItemAdapter adapter;



    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view =
                inflater.inflate(R.layout.fragment_dashboard, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ItemAdapter(getContext(), false , false); // hide Claim button
        recyclerView.setAdapter(adapter);


        loadClaimedMeals();

        return view;
    }

    private void loadClaimedMeals() {

        SharedPreferences prefs =
                requireContext().getSharedPreferences(
                        "SaveCampusPrefs", Context.MODE_PRIVATE);

        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) return;

        new Thread(() -> {
            try {
                URL url = new URL(
                        "http://10.0.2.2/mobileApp/get_claimed_meals.php?user_id=" + userId
                );

                HttpURLConnection conn =
                        (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(conn.getInputStream())
                        );

                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }

                reader.close();
                conn.disconnect();

                JSONObject response = new JSONObject(json.toString());

                if (!response.getBoolean("success")) return;

                JSONArray arr = response.getJSONArray("meals");
                List<JSONObject> items = new ArrayList<>();

                for (int i = 0; i < arr.length(); i++) {
                    items.add(arr.getJSONObject(i));
                }

                requireActivity().runOnUiThread(() ->
                        adapter.setItems(items)
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
