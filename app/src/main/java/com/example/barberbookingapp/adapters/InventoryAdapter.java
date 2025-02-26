package com.example.barberbookingapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barberbookingapp.R;
import com.example.barberbookingapp.models.InventoryItem;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/*
אדפטר המיועד להצגת מלאי הציוד במספרה בRecView
* של הספרים כל פריט יכלול את שם הפריט והכמות שלו וכמו כן,
כפתור מינוס - להורדת הכמות ב1
כפתור פלוס - להוספת הכמות ב1
וכפתור ביטול המאפשר למחוק את הפריט מהרשימה
* */

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private final List<InventoryItem> itemList;

    public InventoryAdapter(List<InventoryItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = itemList.get(position);
        holder.itemName.setText(item.getName());
        holder.itemQuantity.setText(String.valueOf(item.getQuantity()));

        //מציאת הנתיב לפריט הנוכחי
        DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("inventory").child(item.getKey());

        //בעת לחיצה על כפתור מינוס תתבצע הורדה ב1 של הכמות ועדכון ברשימה ובפיירבייס
        holder.decreaseButton.setOnClickListener(v -> {
            if (item.getQuantity() > 0) {
                int newQuantity = item.getQuantity() - 1;
                item.setQuantity(newQuantity);
                itemRef.child("quantity").setValue(newQuantity);
            }
        });

        //בעת לחיצה על כפתור פלוס תתבצע הוספה ב1 של הכמות ועדכון ברשימה ובפיירבייס
        holder.increaseButton.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;
            item.setQuantity(newQuantity);
            itemRef.child("quantity").setValue(newQuantity);
        });

        //בעת לחיצה על REMOVE תתבצע מחיקה של הפריט מהפיירבייס ומהרשימה
        holder.deleteButton.setOnClickListener(v -> {
            itemRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                } else {
                    Toast.makeText(v.getContext(), "Failed to delete item", Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    //הצגת השדות הסופיים לכל פריט ברשימה
    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView itemName;
        TextView itemQuantity;
        Button decreaseButton, increaseButton, deleteButton;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            itemQuantity = itemView.findViewById(R.id.item_quantity);
            decreaseButton = itemView.findViewById(R.id.button_decrease);
            increaseButton = itemView.findViewById(R.id.button_increase);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }
    }
}
