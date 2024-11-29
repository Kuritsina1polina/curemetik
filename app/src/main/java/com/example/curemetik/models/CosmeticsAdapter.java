package com.example.curemetik.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curemetik.R;

import java.util.List;

public class CosmeticsAdapter extends RecyclerView.Adapter<CosmeticsAdapter.CosmeticsViewHolder> {

    private List<CosmeticItem> cosmeticItems;
    private Context context;

    public CosmeticsAdapter(Context context, List<CosmeticItem> cosmeticItems) {
        this.context = context;
        this.cosmeticItems = cosmeticItems;
    }

    @NonNull
    @Override
    public CosmeticsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cosmetic, parent, false);
        return new CosmeticsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CosmeticsViewHolder holder, int position) {
        CosmeticItem cosmeticItem = cosmeticItems.get(position);
        holder.textView.setText(cosmeticItem.getName());
        holder.descriptionTextView.setText(cosmeticItem.getDescription());
        holder.checkBox.setChecked(cosmeticItem.isSelected());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> cosmeticItem.setSelected(isChecked));
    }

    @Override
    public int getItemCount() {
        return cosmeticItems.size();
    }

    public class CosmeticsViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        TextView descriptionTextView;
        CheckBox checkBox;

        public CosmeticsViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
