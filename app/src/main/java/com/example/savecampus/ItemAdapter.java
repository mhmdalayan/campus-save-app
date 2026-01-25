package com.example.savecampus;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private final Context context;
    private final List<JSONObject> items = new ArrayList<>();
    private final String userRole;

    public ItemAdapter(Context context) {
        this.context = context;

        // 🔑 Read role ONCE
        SharedPreferences prefs =
                context.getSharedPreferences("SaveCampusPrefs", Context.MODE_PRIVATE);
        userRole = prefs.getString("role", "student");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        JSONObject item = items.get(position);

        try {
            // ================= RESET (VERY IMPORTANT) =================
            holder.tvExpiresAt.setVisibility(View.VISIBLE);
            holder.btnClaim.setEnabled(true);
            holder.btnClaim.setAlpha(1f);

            // ================= BASIC DATA =================
            holder.tvMealName.setText(item.getString("name"));
            holder.tvPortions.setText(
                    item.getInt("available_portions") + " left"
            );

            // ================= IMAGE =================
            Glide.with(context)
                    .load(item.getString("image_path"))
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.imgMeal);

            // ================= EXPIRY LOGIC =================
            String expiresRaw = item.getString("expires_at");

            SimpleDateFormat sdf =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            Date expiresDate = sdf.parse(expiresRaw);

            long now = System.currentTimeMillis();
            long diff = expiresDate.getTime() - now;

            if (diff <= 0) {
                // ❌ EXPIRED
                holder.tvExpiresAt.setText("Expired");
                holder.btnClaim.setEnabled(false);
                holder.btnClaim.setAlpha(0.4f);

            } else {
                long minutes = diff / (1000 * 60);
                long hours = minutes / 60;
                long days = hours / 24;

                if (hours < 24) {
                    holder.tvExpiresAt.setText(
                            "Expires in " + hours + "h " + (minutes % 60) + "m"
                    );
                } else if (days == 1) {
                    holder.tvExpiresAt.setText("Expires tomorrow");
                } else {
                    holder.tvExpiresAt.setText("Expires " + expiresRaw);
                }
            }

            // ================= ROLE LOGIC =================
            if ("student".equalsIgnoreCase(userRole)) {
                holder.btnClaim.setVisibility(View.VISIBLE);
                holder.btnDelete.setVisibility(View.GONE);
            } else {
                holder.btnClaim.setVisibility(View.GONE);
                holder.btnDelete.setVisibility(View.VISIBLE);
            }

            // ================= BUTTON ACTIONS (PLACEHOLDERS) =================
            holder.btnClaim.setOnClickListener(v -> {
                // TODO: call claim_meal.php
            });

            holder.btnDelete.setOnClickListener(v -> {
                // TODO: call delete_meal.php
            });

        } catch (Exception e) {
            // HARD FAIL SAFE
            holder.tvExpiresAt.setText("Unknown expiry");
            holder.btnClaim.setEnabled(false);
            holder.btnClaim.setAlpha(0.4f);
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<JSONObject> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    // ================= VIEW HOLDER =================
    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgMeal;
        TextView tvMealName, tvPortions, tvExpiresAt;
        Button btnClaim, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            imgMeal = itemView.findViewById(R.id.imgMeal);
            tvMealName = itemView.findViewById(R.id.tvMealName);
            tvPortions = itemView.findViewById(R.id.tvPortions);
            tvExpiresAt = itemView.findViewById(R.id.tvExpiresAt);
            btnClaim = itemView.findViewById(R.id.btnClaim);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
