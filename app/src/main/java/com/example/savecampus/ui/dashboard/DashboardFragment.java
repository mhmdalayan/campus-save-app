package com.example.savecampus.ui.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.savecampus.ItemAdapter;
import com.example.savecampus.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Dashboard: no Claim button, no Portions badge
        adapter = new ItemAdapter(requireContext(), false, false);
        recyclerView.setAdapter(adapter);

        loadClaimedMeals();

        return view;
    }

    // ================= LOAD CLAIMED MEALS =================

    private void loadClaimedMeals() {

        Context ctx = getContext();
        if (ctx == null) return;

        SharedPreferences prefs =
                ctx.getSharedPreferences("SaveCampusPrefs", Context.MODE_PRIVATE);

        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(ctx, "Session expired", Toast.LENGTH_SHORT).show();
            return;
        }

        String url =
                "http://10.0.2.2/mobileApp/get_claimed_meals.php?user_id=" + userId;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (!obj.optBoolean("success")) return;

                        JSONArray arr = obj.optJSONArray("meals");
                        if (arr == null) arr = new JSONArray();

                        List<JSONObject> items = new ArrayList<>();
                        for (int i = 0; i < arr.length(); i++) {
                            items.add(arr.getJSONObject(i));
                        }

                        adapter.setItems(items);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ctx,
                                "Parse error",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(ctx,
                            "Network error",
                            Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(ctx).add(request);
    }
}
