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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.savecampus.ItemAdapter;
import com.example.savecampus.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ItemAdapter adapter;

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

        adapter = new ItemAdapter(getContext());
        recyclerView.setAdapter(adapter);

        loadMeals();

        return view;
    }

    private void loadMeals() {

        String url = "http://10.0.2.2/mobileApp/get_meals.php";

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    List<JSONObject> list = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        list.add(response.optJSONObject(i));
                    }
                    adapter.setItems(list);
                },
                error -> error.printStackTrace()
        );

        queue.add(request);
    }
}
