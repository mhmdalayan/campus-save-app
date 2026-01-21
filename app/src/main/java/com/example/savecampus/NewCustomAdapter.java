package com.example.savecampus;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NewCustomAdapter extends RecyclerView.Adapter<NewCustomAdapter.ViewHolder> implements Filterable {

    private final Context context;
    private List<CampusItem> itemList;
    private final List<CampusItem> itemListFull; // For filtering

    public NewCustomAdapter(Context c, List<CampusItem> items) {
        this.context = c;
        this.itemList = new ArrayList<>(items);
        this.itemListFull = new ArrayList<>(items); // Full copy for the filter
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.row, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CampusItem currentItem = itemList.get(position);

        holder.itemImage.setImageResource(currentItem.getImageId());
        holder.itemName.setText(currentItem.getName());
        holder.itemPrice.setText(currentItem.getPrice());
        holder.itemDescription.setText(currentItem.getDescription());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void addItem(CampusItem item) {
        // Add to both the master list and the filtered list to ensure consistency
        itemListFull.add(0, item);
        itemList.add(0, item);
        notifyItemInserted(0); // Efficiently notify the adapter of the new item at the top
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView itemImage;
        public final TextView itemName;
        public final TextView itemPrice;
        public final TextView itemDescription;
        public final Button takeItButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views from the new row.xml layout
            itemImage = itemView.findViewById(R.id.item_image);
            itemName = itemView.findViewById(R.id.item_name);
            itemPrice = itemView.findViewById(R.id.item_price);
            itemDescription = itemView.findViewById(R.id.item_description);
            takeItButton = itemView.findViewById(R.id.take_it_button);

            // "Take It" button click listener
            takeItButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    CampusItem clickedItem = itemList.get(position);
                    Toast.makeText(context, "You took " + clickedItem.getName(), Toast.LENGTH_SHORT).show();
                    // You can add more logic here, like removing the item
                }
            });

            // Whole item click listener (to navigate to details)
            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    CampusItem clickedItem = itemList.get(position);
                    Bundle args = new Bundle();
                    args.putString("title", clickedItem.getName()); // Still passing title for SecondFragment

                    try {
                        Navigation.findNavController((Activity) context, R.id.nav_host_fragment_activity_main)
                                .navigate(R.id.action_FirstFragment_to_SecondFragment, args);
                    } catch (Exception e) {
                        Toast.makeText(context, "Navigation Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("NavigationError", "Failed to navigate", e);
                    }
                }
            });
        }
    }

    @Override
    public Filter getFilter() {
        return dataFilter;
    }

    private final Filter dataFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<CampusItem> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(itemListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (CampusItem item : itemListFull) {
                    // Search by item name
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            itemList.clear();
            itemList.addAll((List) results.values);
            notifyDataSetChanged(); // This is okay for now, can be optimized later
        }
    };
}
