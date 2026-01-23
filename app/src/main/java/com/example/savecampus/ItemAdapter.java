package com.example.savecampus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private final Context context;
    private List<JSONObject> items = new ArrayList<>();
    private final Consumer<JSONObject> onTakeItClickListener;

    public ItemAdapter(Context context, Consumer<JSONObject> onTakeItClickListener) {
        this.context = context;
        this.onTakeItClickListener = onTakeItClickListener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        JSONObject item = items.get(position);
        try {
            holder.nameTextView.setText(item.getString("name"));
            holder.priceTextView.setText(String.format(Locale.US, "$%.2f", item.getDouble("price")));

            Glide.with(context)
                    .load(item.getString("image_url"))
                    .placeholder(R.drawable.ic_launcher_background) // Default image while loading
                    .error(R.drawable.ic_launcher_foreground)       // Image to show if loading fails
                    .into(holder.imageView);

            holder.takeItButton.setOnClickListener(v -> {
                int currentPosition = holder.getAdapterPosition();
                // Ensure the position is valid before processing the click
                if (currentPosition != RecyclerView.NO_POSITION) {
                    JSONObject clickedItem = items.get(currentPosition);
                    onTakeItClickListener.accept(clickedItem);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Updates the adapter's data with a new list of items using DiffUtil
     * to calculate changes and animate them efficiently.
     * @param newJsonItems The new data as a JSONArray.
     */
    public void updateData(JSONArray newJsonItems) {
        List<JSONObject> newItemsList = new ArrayList<>();
        for (int i = 0; i < newJsonItems.length(); i++) {
            try {
                newItemsList.add(newJsonItems.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        this.items.clear();
        this.items.addAll(newItemsList);
        notifyDataSetChanged();
    }

    /**
     * Safely removes a specific item from the adapter's list
     * and animates the removal in the RecyclerView.
     * @param itemToRemove The JSONObject of the item to be removed.
     */
    public void removeItem(JSONObject itemToRemove) {
        if (itemToRemove == null) return;

        // 1. Try to remove by object reference (Most reliable)
        int index = items.indexOf(itemToRemove);
        if (index != -1) {
            items.remove(index);
            notifyItemRemoved(index);
            notifyItemRangeChanged(index, items.size());
            return;
        }

        try {
            String idToRemove = itemToRemove.getString("id");
            for (int i = 0; i < items.size(); i++) {
                String currentId = items.get(i).getString("id");
                if (currentId.equals(idToRemove)) {
                    items.remove(i);
                    notifyItemRemoved(i); // Animate the removal
                    notifyItemRangeChanged(i, items.size()); // Update positions of items below
                    return; // Item found and removed, exit the method
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * The ViewHolder that holds the layout for each item in the RecyclerView.
     */
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView;
        TextView priceTextView;
        Button takeItButton;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            nameTextView = itemView.findViewById(R.id.item_name);
            priceTextView = itemView.findViewById(R.id.item_price);
            takeItButton = itemView.findViewById(R.id.take_it_button);
        }
    }
}
