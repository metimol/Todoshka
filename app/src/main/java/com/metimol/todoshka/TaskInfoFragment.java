package com.metimol.todoshka;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.metimol.todoshka.database.Category;
import com.metimol.todoshka.database.ToDo;
import com.metimol.todoshka.database.ToDoDao;
import com.metimol.todoshka.database.AppDatabase;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

public class TaskInfoFragment extends Fragment implements ConfirmDeleteDialog.ConfirmDeleteListener {
    private static final String TAG = "TaskInfoFragment";
    private final ExecutorService databaseWriteExecutor = AppDatabase.databaseWriteExecutor;

    private TextView tvTaskTitle, tvCategoryValue, tvDateValue, tvPriorityValue;
    private ImageView ivPriorityIcon;

    private ToDoDao toDoDao;
    private ToDo currentTask;
    private SimpleDateFormat dateFormat;
    private MainViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentTask = getArguments().getParcelable("task");
        }
        dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        AppDatabase db = AppDatabase.getDatabase(requireContext());
        toDoDao = db.toDoDao();

        tvTaskTitle = view.findViewById(R.id.tvTaskTitle);
        tvCategoryValue = view.findViewById(R.id.tvCategoryValue);
        tvDateValue = view.findViewById(R.id.tvDateValue);
        tvPriorityValue = view.findViewById(R.id.tvPriorityValue);
        ivPriorityIcon = view.findViewById(R.id.ivPriorityIcon);
        ImageView ivBack = view.findViewById(R.id.ivBack);
        ImageView ivDelete = view.findViewById(R.id.ivDelete);

        if (currentTask != null) {
            displayTaskInfo(currentTask);
            fetchAndDisplayCategoryName(currentTask.categoryId);
        } else {
            Log.e(TAG, "Error: currentTask is null.");
            Toast.makeText(requireContext(), getString(R.string.cannot_load_task), Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
        }

        ivBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        ivDelete.setOnClickListener(v -> {
            if (currentTask != null) {
                ConfirmDeleteDialog dialog = ConfirmDeleteDialog.newInstance(currentTask);
                dialog.show(getChildFragmentManager(), ConfirmDeleteDialog.TAG);
            } else {
                Toast.makeText(requireContext(), getString(R.string.cannot_remove_task), Toast.LENGTH_SHORT).show();
            }
        });

        var task_info_layout = view.findViewById(R.id.task_info_fragment_screen);
        ViewCompat.setOnApplyWindowInsetsListener(task_info_layout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void displayTaskInfo(ToDo task) {
        tvTaskTitle.setText(task.text);

        if (task.creationDate != null) {
            tvDateValue.setText(dateFormat.format(task.creationDate));
        } else {
            tvDateValue.setText("N/A");
        }

        if (task.priority != null) {
            setPriorityIconAndText(task.priority);
        } else {
            tvPriorityValue.setText("N/A");
            ivPriorityIcon.setVisibility(ImageView.INVISIBLE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void setPriorityIconAndText(String priorityStr) {
        try {
            CreateTaskBottomSheet.Priority p = CreateTaskBottomSheet.Priority.valueOf(priorityStr);
            tvPriorityValue.setText(priorityStr.substring(0, 1).toUpperCase() + priorityStr.substring(1).toLowerCase());
            ivPriorityIcon.setImageResource(p.getDrawableResId());
            ivPriorityIcon.setVisibility(ImageView.VISIBLE);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Unknown priority: " + priorityStr);
            tvPriorityValue.setText("N/A");
            ivPriorityIcon.setVisibility(ImageView.INVISIBLE);
        }
    }

    private void fetchAndDisplayCategoryName(int categoryId) {
        databaseWriteExecutor.execute(() -> {
            Category category = toDoDao.getCategoryById(categoryId);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (category != null) {
                        tvCategoryValue.setText(category.name);
                    } else {
                        Log.w(TAG, "Category with ID " + categoryId + " not found.");
                        tvCategoryValue.setText("N/A");
                    }
                });
            }
        });
    }

    @Override
    public void onDeleteConfirmed(ToDo task) {
        viewModel.deleteTodo(task);
        if (getView() != null) {
            Navigation.findNavController(requireView()).popBackStack();
        }
    }

    @Override
    public void onDeleteConfirmed(Category category) { }

    @Override
    public void onDeleteCompletedTasksConfirmed() { }
}