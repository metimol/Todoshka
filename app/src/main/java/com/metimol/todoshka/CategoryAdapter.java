package com.metimol.todoshka;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.metimol.todoshka.database.Category;
import com.metimol.todoshka.database.CategoryInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CategoryAdapter extends ListAdapter<CategoryInfo, CategoryAdapter.CategoryViewHolder> {
    private List<CategoryInfo> internalList = new ArrayList<>();

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public interface OnCategorySettingsClickListener {
        void onSettingsClick(Category category, View anchorView);
    }

    public CategoryAdapter() {
        super(DIFF_CALLBACK);
    }

    @Override
    public void submitList(@Nullable List<CategoryInfo> list) {
        internalList = list == null ? new ArrayList<>() : new ArrayList<>(list);
        super.submitList(internalList);
    }

    private OnCategorySettingsClickListener settingsClickListener;
    private OnStartDragListener dragStartListener;

    public void setOnCategorySettingsClickListener(OnCategorySettingsClickListener listener) {
        this.settingsClickListener = listener;
    }

    public void setOnStartDragListener(OnStartDragListener dragStartListener) {
        this.dragStartListener = dragStartListener;
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
                    return oldItem.category.id == newItem.category.id &&
                            oldItem.category.name.equals(newItem.category.name) &&
                            oldItem.totalTasks == newItem.totalTasks &&
                            oldItem.completedTasks == newItem.completedTasks;
                }
            };

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view, dragStartListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryInfo currentCategoryInfo = getItem(position);
        holder.bind(currentCategoryInfo, settingsClickListener);
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCategoryTitle;
        private final TextView tvCompletedTodos;
        private final ImageView ivOpenCategorySettings;

        @SuppressLint("ClickableViewAccessibility")
        public CategoryViewHolder(@NonNull View itemView, final OnStartDragListener dragStartListener) {
            super(itemView);
            tvCategoryTitle = itemView.findViewById(R.id.tvCategoryTitle);
            tvCompletedTodos = itemView.findViewById(R.id.tvCompletedTodos);
            ivOpenCategorySettings = itemView.findViewById(R.id.ivOpenCategorySettings);
            ImageView ivChangePosition = itemView.findViewById(R.id.ivChangePosition);

            ivChangePosition.setOnTouchListener((v, event) -> {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    if (dragStartListener != null) {
                        dragStartListener.onStartDrag(this);
                    }
                }
                return false;
            });
        }

        @SuppressLint("SetTextI18n")
        public void bind(final CategoryInfo categoryInfo,
                         final OnCategorySettingsClickListener settingsListener) {
            if (categoryInfo == null || categoryInfo.category == null) {
                Log.e("CategoryViewHolder", "bind called with null categoryInfo or category");
                itemView.setVisibility(View.GONE);
                return;
            }
            itemView.setVisibility(View.VISIBLE);

            final Category category = categoryInfo.category;

            tvCategoryTitle.setText(category.name);
            tvCompletedTodos.setText(categoryInfo.completedTasks + "/" + categoryInfo.totalTasks + " task");
            tvCompletedTodos.setVisibility(View.VISIBLE);

            ivOpenCategorySettings.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (settingsListener != null && position != RecyclerView.NO_POSITION) {
                    settingsListener.onSettingsClick(category, v);
                }
            });
        }
    }

    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < 0 || fromPosition >= internalList.size() || toPosition < 0 || toPosition >= internalList.size()) {
            Log.e("CategoryAdapter", "onItemMove error: Invalid positions - from=" + fromPosition + ", to=" + toPosition + ", size=" + internalList.size());
            return;
        }

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(internalList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(internalList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    public List<Category> getCurrentCategoryList() {
        List<Category> categories = new ArrayList<>();
        for (CategoryInfo info : internalList) {
            if (info != null && info.category != null) {
                categories.add(info.category);
            } else {
                Log.w("CategoryAdapter", "  Skipping null CategoryInfo or Category in internalList during save preparation.");
            }
        }
        return categories;
    }

    @Override
    public int getItemCount() {
        return internalList.size();
    }
}