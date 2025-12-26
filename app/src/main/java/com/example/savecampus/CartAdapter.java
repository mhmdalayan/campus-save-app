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

import java.util.Locale;
import java.util.function.BiConsumer;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private JSONArray cartItems;
    // This will hold the "Undo" logic from DashboardFragment
    private final BiConsumer<JSONObject, Integer> onUndoClickListener;

    public CartAdapter(Context context, JSONArray cartItems, BiConsumer<JSONObject, Integer> onUndoClickListener) {
        this.context = context;
        this.cartItems = cartItems;
        this.onUndoClickListener = onUndoClickListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item_view, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        try {
            JSONObject item = cartItems.getJSONObject(position);

            holder.nameTextView.setText(item.getString("name"));
            holder.priceTextView.setText(String.format(Locale.US, "$%.2f", item.getDouble("price")));

            Glide.with(context)
                    .load(item.getString("image_url"))
                    .into(holder.imageView);

            // *** THIS IS THE NEW LOGIC ***
            holder.undoButton.setOnClickListener(v -> {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    try {
                        JSONObject clickedItem = cartItems.getJSONObject(currentPosition);
                        // Execute the logic defined in DashboardFragment, passing the item and its position.
                        onUndoClickListener.accept(clickedItem, currentPosition);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.length();
    }

    // Method to update the data and refresh the list
    public void updateData(JSONArray newCartItems) {
        this.cartItems = newCartItems;
        notifyDataSetChanged();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView;
        TextView priceTextView;
        Button undoButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            nameTextView = itemView.findViewById(R.id.item_name);
            priceTextView = itemView.findViewById(R.id.item_price);
            undoButton = itemView.findViewById(R.id.undo_button);
        }
    }
}
