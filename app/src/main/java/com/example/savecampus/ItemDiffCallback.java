package com.example.savecampus;

import androidx.recyclerview.widget.DiffUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ItemDiffCallback extends DiffUtil.Callback {

    private final List<JSONObject> oldList;
    private final List<JSONObject> newList;    public ItemDiffCallback(List<JSONObject> oldList, List<JSONObject> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        // Use a unique ID to check if items are the same.
        try {
            String oldId = oldList.get(oldItemPosition).getString("id");
            String newId = newList.get(newItemPosition).getString("id");
            return oldId.equals(newId);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        // Check if the content of the items is the same.
        // For JSONObjects, comparing the string representation is a straightforward way.
        return oldList.get(oldItemPosition).toString().equals(newList.get(newItemPosition).toString());
    }
}
