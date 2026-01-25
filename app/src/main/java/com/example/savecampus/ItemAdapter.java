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
    private final boolean showClaimButton;
    private final boolean showPortions;

    public ItemAdapter(Context context, boolean showClaimButton, boolean showPortions) {
        this.context = context;
        this.showClaimButton = showClaimButton;
        this.showPortions = showPortions;

        SharedPreferences prefs = context.getSharedPreferences("SaveCampusPrefs", Context.MODE_PRIVATE);
        userRole = prefs.getString("role", "student");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JSONObject item = items.get(position);

        try {
            // Basic data
            holder.tvMealName.setText(item.getString("name"));

            if (showPortions) {
                holder.tvPortions.setVisibility(View.VISIBLE);
                holder.tvPortions.setText(item.getInt("available_portions") + " left");
            } else {
                holder.tvPortions.setVisibility(View.GONE);
            }

            Glide.with(context)
                    .load(item.getString("image_path"))
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.imgMeal);

            // Expiry logic
            String expiresRaw = item.getString("expires_at");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            Date expiresDate = sdf.parse(expiresRaw);

            long diff = expiresDate.getTime() - System.currentTimeMillis();
            if (diff <= 0) {
                holder.tvExpiresAt.setText("Expired");
                holder.btnClaim.setEnabled(false);
                holder.btnClaim.setAlpha(0.4f);
            } else {
                long minutes = diff / (1000 * 60);
                long hours = minutes / 60;
                long days = hours / 24;

                if (hours < 24) {
                    holder.tvExpiresAt.setText("Expires in " + hours + "h " + (minutes % 60) + "m");
                } else if (days == 1) {
                    holder.tvExpiresAt.setText("Expires tomorrow");
                } else {
                    holder.tvExpiresAt.setText("Expires " + expiresRaw);
                }
            }

            // Claim button visibility
            holder.btnDelete.setVisibility(View.GONE);
            if ("student".equalsIgnoreCase(userRole) && showClaimButton) {
                holder.btnClaim.setVisibility(View.VISIBLE);
                holder.btnClaim.setEnabled(true);
                holder.btnClaim.setAlpha(1f);
            } else {
                holder.btnClaim.setVisibility(View.GONE);
            }

            // Claim action
            if (showClaimButton) {
                holder.btnClaim.setOnClickListener(v -> claimMeal(item, holder));
            }

        } catch (Exception e) {
            holder.tvExpiresAt.setText("Unknown expiry");
            holder.btnClaim.setEnabled(false);
            holder.btnClaim.setAlpha(0.4f);
            e.printStackTrace();
        }
    }

    private void claimMeal(JSONObject item, ViewHolder holder) {
        try {
            int mealId = item.getInt("id");

            SharedPreferences prefs = context.getSharedPreferences("SaveCampusPrefs", Context.MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            if (userId == -1) return;

            String url = "http://10.0.2.2/mobileApp/claim_meal.php";

            JSONObject body = new JSONObject();
            body.put("meal_id", mealId);
            body.put("user_id", userId);

            com.android.volley.toolbox.JsonObjectRequest request =
                    new com.android.volley.toolbox.JsonObjectRequest(
                            com.android.volley.Request.Method.POST,
                            url,
                            body,
                            response -> {
                                try {
                                    if (response.getBoolean("success")) {
                                        int current = item.getInt("available_portions");
                                        item.put("available_portions", current - 1);
                                        notifyItemChanged(holder.getAdapterPosition());
                                    } else {
                                        // Show server message
                                        android.widget.Toast.makeText(context,
                                                response.getString("message"),
                                                android.widget.Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            },
                            error -> error.printStackTrace()
                    ) {
                        @Override
                        public java.util.Map<String, String> getHeaders() {
                            java.util.HashMap<String, String> headers = new java.util.HashMap<>();
                            headers.put("Content-Type", "application/json");
                            return headers;
                        }
                    };

            com.android.volley.RequestQueue queue = com.android.volley.toolbox.Volley.newRequestQueue(context);
            queue.add(request);

        } catch (Exception e) {
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
