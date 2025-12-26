package com.example.savecampus;

import org.json.JSONArray;
import org.json.JSONObject;

public class CartManager {

    private static CartManager instance;
    private JSONArray cartItems;

    // Private constructor to prevent instantiation
    private CartManager() {
        cartItems = new JSONArray();
    }

    // The single entry point to get the instance of the CartManager
    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addItem(JSONObject item) {
        cartItems.put(item);
    }

    public void removeItem(int position) {
        if (position >= 0 && position < cartItems.length()) {
            cartItems.remove(position);
        }
    }

    public JSONArray getCartItems() {
        return cartItems;
    }

    public double getTotalPrice() {
        double total = 0.0;
        for (int i = 0; i < cartItems.length(); i++) {
            try {
                JSONObject item = cartItems.getJSONObject(i);
                total += item.getDouble("price");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return total;
    }

    // *** NEW METHOD TO BE CALLED ON APP START ***
    public void clearCart() {
        cartItems = new JSONArray();
    }
}
