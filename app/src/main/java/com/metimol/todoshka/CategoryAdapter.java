package com.metimol.todoshka;

import android.annotation.SuppressLint;
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
import com.metimol.todoshka.database.CategoryInfo;

public class CategoryAdapter extends ListAdapter<CategoryInfo, CategoryAdapter.CategoryViewHolder> {

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

    private static final DiffUtil.ItemCallback<CategoryInfo> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CategoryInfo>() {
                @Override
                public boolean areItemsTheSame(@NonNull CategoryInfo oldItem, @NonNull CategoryInfo newItem) {
                    return oldItem.category.id == newItem.category.id;
                }

                @Override
                @SuppressLint("DiffUtilEquals")
                public boolean areContentsTheSame(@NonNull CategoryInfo oldItem, @NonNull CategoryInfo newItem) {
                    return oldItem.category.name.equals(newItem.category.name) &&
                            oldItem.category.position == newItem.category.position &&
                            oldItem.totalTasks == newItem.totalTasks &&
                            oldItem.completedTasks == newItem.completedTasks;
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
        CategoryInfo currentCategoryInfo = getItem(position);
        holder.bind(currentCategoryInfo, settingsClickListener, itemClickListener);
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

        @SuppressLint("SetTextI18n")
        public void bind(final CategoryInfo categoryInfo,
                         final OnCategorySettingsClickListener settingsListener,
                         final OnCategoryItemClickListener itemClickListener) {
            final Category category = categoryInfo.category;

            tvCategoryTitle.setText(category.name);

            tvCompletedTodos.setText(categoryInfo.completedTasks + "/" + categoryInfo.totalTasks + " task");
            tvCompletedTodos.setVisibility(View.VISIBLE);

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
        }
    }
}
