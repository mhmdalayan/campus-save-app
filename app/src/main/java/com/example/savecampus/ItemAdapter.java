package com.example.savecampus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private final Context context;
    private final List<JSONObject> items = new ArrayList<>();

    public ItemAdapter(Context context) {
        this.context = context;
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
            holder.tvMealName.setText(item.getString("name"));
            holder.tvPortions.setText(
                    "Available: " + item.getInt("available_portions")
            );
            holder.tvExpiresAt.setText(
                    "Expires: " + item.getString("expires_at")
            );

            Glide.with(context)
                    .load(item.getString("image_url"))
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.imgMeal);

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

        ViewHolder(View itemView) {
            super(itemView);
            imgMeal = itemView.findViewById(R.id.imgMeal);
            tvMealName = itemView.findViewById(R.id.tvMealName);
            tvPortions = itemView.findViewById(R.id.tvPortions);
            tvExpiresAt = itemView.findViewById(R.id.tvExpiresAt);
        }
    }
}
