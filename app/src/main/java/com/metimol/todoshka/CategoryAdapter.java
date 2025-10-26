package com.metimol.todoshka;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.metimol.todoshka.database.Category;

public class CategoryAdapter extends ListAdapter<Category, CategoryAdapter.CategoryViewHolder> {

    public interface OnCategorySettingsClickListener {
        void onSettingsClick(Category category, View anchorView);
    }

    public interface OnCategoryItemClickListener {
        void onItemClick(Category category);
    }

    private OnCategorySettingsClickListener settingsClickListener;
    private OnCategoryItemClickListener itemClickListener;


    public CategoryAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnCategorySettingsClickListener(OnCategorySettingsClickListener listener) {
        this.settingsClickListener = listener;
    }

    private static final DiffUtil.ItemCallback<Category> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Category>() {
                @Override
                public boolean areItemsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
                    return oldItem.name.equals(newItem.name) && oldItem.position == newItem.position;
                }
            };

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category currentCategory = getItem(position);
        holder.bind(currentCategory, settingsClickListener, itemClickListener);
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCategoryTitle;
        private final TextView tvCompletedTodos;
        private final ImageView ivOpenCategorySettings;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryTitle = itemView.findViewById(R.id.tvCategoryTitle);
            tvCompletedTodos = itemView.findViewById(R.id.tvCompletedTodos);
            ivOpenCategorySettings = itemView.findViewById(R.id.ivOpenCategorySettings);
        }

        public void bind(final Category category,
                         final OnCategorySettingsClickListener settingsListener,
                         final OnCategoryItemClickListener itemClickListener) {
            tvCategoryTitle.setText(category.name);

            ivOpenCategorySettings.setOnClickListener(v -> {
                if (settingsListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    settingsListener.onSettingsClick(category, v);
                }
            });

            itemView.setOnClickListener(v -> {
                if (itemClickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(category);
                }
            });

            tvCompletedTodos.setVisibility(View.GONE);
        }
    }
}
