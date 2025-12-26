package com.example.savecampus.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savecampus.CartAdapter;
import com.example.savecampus.CartManager;
import com.example.savecampus.R;

import java.util.Locale;

public class DashboardFragment extends Fragment {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private TextView emptyCartTextView;
    private TextView totalTextView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rv_cart_items);
        emptyCartTextView = view.findViewById(R.id.tv_empty_cart);
        totalTextView = view.findViewById(R.id.text_total_price);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // *** THIS IS THE FIX: The "Undo" button has NO network logic ***
        adapter = new CartAdapter(requireContext(), CartManager.getInstance().getCartItems(),
                (item, position) -> {
                    // 1. Remove the item ONLY from the local CartManager in memory.
                    CartManager.getInstance().removeItem(position);

                    // 2. Refresh the cart's view to show the removal.
                    refreshCartView();

                    // 3. Give feedback to the user.
                    Toast.makeText(getContext(), "Removed from this session's cart", Toast.LENGTH_SHORT).show();
                }
        );

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Always refresh the cart view when the fragment is shown.
        refreshCartView();
    }

    /**
     * Updates the RecyclerView, total price, and visibility of the "empty cart" message.
     */
    private void refreshCartView() {
        adapter.updateData(CartManager.getInstance().getCartItems());

        if (CartManager.getInstance().getCartItems().length() == 0) {
            recyclerView.setVisibility(View.GONE);
            emptyCartTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyCartTextView.setVisibility(View.GONE);
        }

        double totalPrice = CartManager.getInstance().getTotalPrice();
        totalTextView.setText(String.format(Locale.US, "$%.2f", totalPrice));
    }
}
