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
import androidx.appcompat.app.AlertDialog;
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

        SharedPreferences prefs =
                context.getSharedPreferences("SaveCampusPrefs", Context.MODE_PRIVATE);
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
            // ================= RESET =================
            holder.btnClaim.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
            holder.tvDiscount.setVisibility(View.GONE);
            holder.tvClaimedAt.setVisibility(View.GONE);

            holder.tvExpiresAt.setVisibility(View.VISIBLE);
            holder.tvPrice.setVisibility(View.VISIBLE);

            // ================= BASIC =================
            holder.tvMealName.setText(item.optString("name", ""));

            String desc = item.optString("description", "");
            if (!desc.trim().isEmpty()) {
                holder.tvDescription.setVisibility(View.VISIBLE);
                holder.tvDescription.setText(desc);
            } else {
                holder.tvDescription.setVisibility(View.GONE);
            }

            // ================= PORTIONS =================
            if (showPortions) {
                holder.tvPortions.setText(
                        item.optInt("available_portions", 0) + " left"
                );
                holder.tvPortions.setVisibility(View.VISIBLE);
            } else {
                holder.tvPortions.setVisibility(View.GONE);
            }

            Glide.with(context)
                    .load(item.optString("image_path", ""))
                    .placeholder(R.drawable.placeholder_meal)
                    .error(R.drawable.placeholder_error)
                    .into(holder.imgMeal);

            // ================= DASHBOARD MODE =================
            boolean isDashboard = !showClaimButton && !showPortions;

            if (isDashboard && item.has("claimed_at")) {

                holder.tvExpiresAt.setVisibility(View.GONE);
                holder.tvPrice.setVisibility(View.GONE);
                holder.tvDiscount.setVisibility(View.GONE);

                holder.tvClaimedAt.setVisibility(View.VISIBLE);

                String claimedRaw = item.optString("claimed_at", "");
                try {
                    SimpleDateFormat in =
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    SimpleDateFormat out =
                            new SimpleDateFormat("MMM dd, HH:mm", Locale.US);

                    Date d = in.parse(claimedRaw);
                    Date now = new Date();

// Check if same day
                    SimpleDateFormat dayFormat =
                            new SimpleDateFormat("yyyyMMdd", Locale.US);

                    if (dayFormat.format(d).equals(dayFormat.format(now))) {

                        long diffMs = now.getTime() - d.getTime();
                        long diffMin = diffMs / (1000 * 60);
                        long diffHour = diffMin / 60;

                        holder.tvClaimedAt.setText(
                                "Claimed " + diffHour + "h " + (diffMin % 60) + "m ago"
                        );

                    } else {
                        SimpleDateFormat outFmt =
                                new SimpleDateFormat("MMM dd, HH:mm", Locale.US);

                        holder.tvClaimedAt.setText(
                                "Claimed at " + outFmt.format(d)
                        );
                    }

                } catch (Exception e) {
                    holder.tvClaimedAt.setText("Claimed");
                }

            } else {

                // ================= PRICE / DISCOUNT =================
                double price = item.optDouble("price", 0);
                int discount = item.optInt("discount_percent", 0);

                holder.tvPrice.setText(String.format(Locale.US, "$%.2f", price));

                if (discount > 0) {
                    holder.tvDiscount.setVisibility(View.VISIBLE);
                    holder.tvDiscount.setText(discount + "% OFF");
                }

                // ================= EXPIRY LOGIC =================
                if (item.has("expires_at") && !item.isNull("expires_at")) {

                    String expiresRaw = item.optString("expires_at", "");
                    SimpleDateFormat sdf =
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

                    Date expiresDate = sdf.parse(expiresRaw);
                    Date now = new Date();

                    if (expiresDate.before(now)) {
                        int pos = holder.getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            items.remove(pos);
                            notifyItemRemoved(pos);
                        }
                        return;
                    }

                    long diffMs = expiresDate.getTime() - now.getTime();
                    long diffMin = diffMs / (1000 * 60);
                    long diffHour = diffMin / 60;
                    long diffDay = diffHour / 24;

                    if (diffDay == 0) {
                        holder.tvExpiresAt.setText(
                                "Expires in " + diffHour + "h " + (diffMin % 60) + "m"
                        );
                    } else if (diffDay == 1) {
                        holder.tvExpiresAt.setText("Expires tomorrow");
                    } else {
                        SimpleDateFormat out =
                                new SimpleDateFormat("MMM dd, HH:mm", Locale.US);
                        holder.tvExpiresAt.setText(
                                "Expires " + out.format(expiresDate)
                        );
                    }

                } else {
                    holder.tvExpiresAt.setText("No expiry");
                }
            }

            // ================= ROLE =================
            if ("student".equalsIgnoreCase(userRole)) {
                if (showClaimButton) holder.btnClaim.setVisibility(View.VISIBLE);
            } else {
                holder.btnDelete.setVisibility(View.VISIBLE);
            }

            holder.btnClaim.setOnClickListener(v -> claimMeal(item, holder));
            holder.btnDelete.setOnClickListener(v -> showDelete(item, holder));

        } catch (Exception e) {
            e.printStackTrace();
            holder.tvExpiresAt.setText("Error");
        }
    }

    // ================= CLAIM MEAL (FIXED JSONException) =================
    private void claimMeal(JSONObject item, ViewHolder holder) {
        try {
            int mealId = item.getInt("id");

            SharedPreferences prefs =
                    context.getSharedPreferences("SaveCampusPrefs", Context.MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            if (userId == -1) return;

            String url = "http://10.0.2.2/mobileApp/claim_meal.php";

            JSONObject body = new JSONObject();
            body.put("meal_id", mealId);
            body.put("user_id", userId);

            com.android.volley.toolbox.Volley
                    .newRequestQueue(context)
                    .add(new com.android.volley.toolbox.JsonObjectRequest(
                            com.android.volley.Request.Method.POST,
                            url,
                            body,
                            res -> {
                                int current = item.optInt("available_portions", 0);
                                int newVal = current - 1;

                                if (newVal <= 0) {
                                    int pos = holder.getAdapterPosition();
                                    if (pos != RecyclerView.NO_POSITION) {
                                        items.remove(pos);
                                        notifyItemRemoved(pos);
                                    }
                                } else {
                                    try {
                                        item.put("available_portions", newVal);
                                    } catch (Exception ignored) {}
                                    notifyItemChanged(holder.getAdapterPosition());
                                }
                            },
                            err -> {}
                    ));

        } catch (Exception ignored) {}
    }

    private void showDelete(JSONObject item, ViewHolder holder) {
        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert) // 👈 this was missing
                .setTitle("Delete meal")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", (d, w) -> deleteMeal(item, holder))
                .setNegativeButton("Cancel", null)
                .show();

    }

    private void deleteMeal(JSONObject item, ViewHolder holder) {
        try {
            int mealId = item.getInt("id");
            String url = "http://10.0.2.2/mobileApp/deletedish.php";

            JSONObject body = new JSONObject();
            body.put("meal_id", mealId);

            com.android.volley.toolbox.Volley
                    .newRequestQueue(context)
                    .add(new com.android.volley.toolbox.JsonObjectRequest(
                            com.android.volley.Request.Method.POST,
                            url,
                            body,
                            r -> {
                                int pos = holder.getAdapterPosition();
                                if (pos != RecyclerView.NO_POSITION) {
                                    items.remove(pos);
                                    notifyItemRemoved(pos);
                                }
                            },
                            e -> {}
                    ));

        } catch (Exception ignored) {}
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<JSONObject> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    // ================= VIEW HOLDER =================
    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgMeal;
        TextView tvMealName, tvDescription, tvPortions, tvExpiresAt;
        TextView tvPrice, tvDiscount, tvClaimedAt;
        Button btnClaim, btnDelete;

        ViewHolder(View v) {
            super(v);
            imgMeal = v.findViewById(R.id.imgMeal);
            tvMealName = v.findViewById(R.id.tvMealName);
            tvDescription = v.findViewById(R.id.tvDescription);
            tvPortions = v.findViewById(R.id.tvPortions);
            tvExpiresAt = v.findViewById(R.id.tvExpiresAt);
            tvClaimedAt = v.findViewById(R.id.tvClaimedAt);
            tvPrice = v.findViewById(R.id.tvPrice);
            tvDiscount = v.findViewById(R.id.tvDiscount);
            btnClaim = v.findViewById(R.id.btnClaim);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
