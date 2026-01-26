package com.example.savecampus.ui.notifications;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.savecampus.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyView;
    private Button clearAllButton;
    private NotificationAdapter adapter;

    @SuppressLint("MissingInflatedId")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = root.findViewById(R.id.notificationsRecyclerView);
        emptyView = root.findViewById(R.id.emptyNotificationsTextView);
        clearAllButton = root.findViewById(R.id.clearAllButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        clearAllButton.setOnClickListener(v -> clearNotifications());

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotifications();
    }

    private void loadNotifications() {
        String url = "http://10.0.2.2/mobileApp/get_notifications.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    java.util.ArrayList<String> notificationList = new java.util.ArrayList<>();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject notification = response.getJSONObject(i);
                            notificationList.add(notification.getString("message"));
                        }

                        if (notificationList.isEmpty()) {
                            emptyView.setVisibility(View.VISIBLE);
                            clearAllButton.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            emptyView.setVisibility(View.GONE);
                            clearAllButton.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.VISIBLE);
                            adapter = new NotificationAdapter(notificationList);
                            recyclerView.setAdapter(adapter);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to parse notifications.", Toast.LENGTH_SHORT).show();
                        emptyView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(getContext(), "Failed to load notifications.", Toast.LENGTH_SHORT).show();
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                });

        queue.add(request);
    }

    private void clearNotifications() {
        String url = "http://10.0.2.2/mobileApp/clear_notifications.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            Toast.makeText(getContext(), "Notifications cleared", Toast.LENGTH_SHORT).show();
                            loadNotifications();
                        } else {
                            Toast.makeText(getContext(), "Failed to clear notifications: " + json.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "Error parsing server response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Network error while clearing notifications", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    // --- Inner Adapter Class ---
    private static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        private final List<String> notifications;

        NotificationAdapter(List<String> notifications) {
            this.notifications = notifications;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_custom, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.textView.setText(notifications.get(position));
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView textView;
            ViewHolder(View view) {
                super(view);
                textView = view.findViewById(R.id.notificationMessage);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
