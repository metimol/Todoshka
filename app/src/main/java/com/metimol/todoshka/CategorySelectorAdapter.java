package com.metimol.todoshka;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.metimol.todoshka.database.Category;

import java.util.List;

public class CategorySelectorAdapter extends RecyclerView.Adapter<CategorySelectorAdapter.ViewHolder> {

    private final List<Category> categories;
    private int selectedPosition = 0;
    private final OnCategorySelectedListener listener;

    public interface OnCategorySelectedListener {
        void onCategorySelected(Category category, int position);
    }

    public CategorySelectorAdapter(List<Category> categories, int selectedPosition, OnCategorySelectedListener listener) {
        this.categories = categories;
        this.selectedPosition = selectedPosition;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_dialog, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.tvCategoryName.setText(category.name);

        holder.ivSelected.setVisibility(position == selectedPosition ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onCategorySelected(category, selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        ImageView ivSelected;

        ViewHolder(View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            ivSelected = itemView.findViewById(R.id.ivSelected);
        }
    }
}