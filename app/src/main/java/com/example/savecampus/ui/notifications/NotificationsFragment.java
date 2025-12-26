package com.example.savecampus.ui.notifications;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savecampus.R;
import com.example.savecampus.databinding.FragmentNotificationsBinding;
import com.example.savecampus.databinding.NotificationItemBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        SharedPreferences prefs = requireActivity().getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
        Set<String> notificationSet = prefs.getStringSet("notifications", new HashSet<>());

        List<String> notificationList = new ArrayList<>(notificationSet);
        Collections.sort(notificationList, Collections.reverseOrder()); // Sorts alphabetically descending, good for timestamps

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
    }

    private void clearNotifications() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("notifications");
        editor.apply();

        // Refresh the view
        loadNotifications();
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
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
                textView = view.findViewById(R.id.notificationTextView);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
