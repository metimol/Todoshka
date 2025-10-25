package com.metimol.todoshka;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.metimol.todoshka.database.ToDo;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TaskAdapter extends ListAdapter<ToDo, TaskAdapter.TaskViewHolder> {

    private final OnTaskCheckedListener checkedListener;
    private final OnTaskClickListener clickListener;
    private final SimpleDateFormat dateFormat;

    public interface OnTaskCheckedListener {
        void onTaskChecked(ToDo task, boolean isChecked);
    }

    public interface OnTaskClickListener {
        void onTaskClick(ToDo task);
    }

    public TaskAdapter(OnTaskCheckedListener checkedListener, OnTaskClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.checkedListener = checkedListener;
        this.clickListener = clickListener;
        this.dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
    }

    private static final DiffUtil.ItemCallback<ToDo> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ToDo>() {
                @Override
                public boolean areItemsTheSame(@NonNull ToDo oldItem, @NonNull ToDo newItem) {
                    return oldItem.id == newItem.id;
                }
                @Override
                public boolean areContentsTheSame(@NonNull ToDo oldItem, @NonNull ToDo newItem) {
                    return oldItem.text.equals(newItem.text) &&
                            oldItem.isCompleted == newItem.isCompleted &&
                            (oldItem.priority == null ? newItem.priority == null : oldItem.priority.equals(newItem.priority)) &&
                            (oldItem.creationDate == null ? newItem.creationDate == null : oldItem.creationDate.equals(newItem.creationDate));
                }
            };

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view, checkedListener, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        ToDo currentTask = getItem(position);
        holder.bind(currentTask, dateFormat);
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox cbTaskCompleted;
        private final TextView tvTaskTitle;
        private final TextView tvTaskDate;
        private final ImageView ivTaskPriority;
        private final Context context;
        private final OnTaskCheckedListener checkedListener;

        public TaskViewHolder(@NonNull View itemView, OnTaskCheckedListener checkedListener, OnTaskClickListener clickListener) {
            super(itemView);
            this.checkedListener = checkedListener;

            cbTaskCompleted = itemView.findViewById(R.id.cbTaskCompleted);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskDate = itemView.findViewById(R.id.tvTaskDate);
            ivTaskPriority = itemView.findViewById(R.id.ivTaskPriority);
            context = itemView.getContext();

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (clickListener != null && position != RecyclerView.NO_POSITION) {
                    assert getBindingAdapter() != null;
                    clickListener.onTaskClick(((TaskAdapter) getBindingAdapter()).getItem(position));
                }
            });
        }

        public void bind(ToDo task, SimpleDateFormat dateFormat) {
            tvTaskTitle.setText(task.text);
            cbTaskCompleted.setOnCheckedChangeListener(null);
            cbTaskCompleted.setChecked(task.isCompleted);

            if (task.creationDate != null) {
                tvTaskDate.setText(dateFormat.format(task.creationDate));
                tvTaskDate.setVisibility(View.VISIBLE);
            } else {
                tvTaskDate.setVisibility(View.GONE);
            }

            updateTextStyle(task.isCompleted);
            updatePriorityFlag(task.priority, task.isCompleted);

            cbTaskCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (checkedListener != null) {
                    checkedListener.onTaskChecked(task, isChecked);
                }
                updateTextStyle(isChecked);
                updatePriorityFlag(task.priority, isChecked);
            });
        }

        private void updateTextStyle(boolean isCompleted) {
            if (isCompleted) {
                tvTaskTitle.setPaintFlags(tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTaskTitle.setTextColor(ContextCompat.getColor(context, R.color.terminal_steel));
                tvTaskDate.setPaintFlags(tvTaskDate.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                tvTaskTitle.setPaintFlags(tvTaskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTaskTitle.setTextColor(ContextCompat.getColor(context, R.color.white));
                tvTaskDate.setPaintFlags(tvTaskDate.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }

        private void updatePriorityFlag(String priority, boolean isCompleted) {
            if (isCompleted || priority == null) {
                ivTaskPriority.setVisibility(View.INVISIBLE);
                return;
            }

            ivTaskPriority.setVisibility(View.VISIBLE);
            try {
                CreateTaskBottomSheet.Priority p = CreateTaskBottomSheet.Priority.valueOf(priority);
                int drawableResId = switch (p) {
                    case HIGH -> R.drawable.ic_priority_high;
                    case MEDIUM -> R.drawable.ic_priority_medium;
                    case LOW -> R.drawable.ic_priority_low;
                };
                ivTaskPriority.setImageResource(drawableResId);
            } catch (IllegalArgumentException e) {
                ivTaskPriority.setVisibility(View.INVISIBLE);
                android.util.Log.w("TaskAdapter", "Unknown priority: " + priority);
            }
        }
    }
}
