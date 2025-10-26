package com.metimol.todoshka;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.metimol.todoshka.database.Category;
import com.metimol.todoshka.database.ToDo;
import com.metimol.todoshka.database.ToDoDao;
import com.metimol.todoshka.database.AppDatabase;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

public class TaskInfoActivity extends AppCompatActivity implements ConfirmDeleteDialog.ConfirmDeleteListener {
    public static final String EXTRA_TASK = "com.metimol.todoshka.TASK";
    private static final String TAG = "TaskInfoActivity";
    private final ExecutorService databaseWriteExecutor = AppDatabase.databaseWriteExecutor;

    private TextView tvTaskTitle, tvCategoryValue, tvDateValue, tvPriorityValue;
    private ImageView ivPriorityIcon;

    private ToDoDao toDoDao;
    private ToDo currentTask;
    private SimpleDateFormat dateFormat;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_info);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        toDoDao = db.toDoDao();

        tvTaskTitle = findViewById(R.id.tvTaskTitle);
        tvCategoryValue = findViewById(R.id.tvCategoryValue);
        tvDateValue = findViewById(R.id.tvDateValue);
        tvPriorityValue = findViewById(R.id.tvPriorityValue);
        ivPriorityIcon = findViewById(R.id.ivPriorityIcon);
        ImageView ivBack = findViewById(R.id.ivBack);
        ImageView ivDelete = findViewById(R.id.ivDelete);

        dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

        if (getIntent().hasExtra(EXTRA_TASK)) {
            currentTask = getIntent().getParcelableExtra(EXTRA_TASK);
            if (currentTask != null) {
                displayTaskInfo(currentTask);
                fetchAndDisplayCategoryName(currentTask.categoryId);
            } else {
                Log.e(TAG, "Error: Retrieve task null object from Intent.");
                Toast.makeText(this, "Cannot load info about task", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Log.e(TAG, "Error: Intent doesn't content task data.");
            Toast.makeText(this, "Cannot load info about task", Toast.LENGTH_SHORT).show();
            finish();
        }

        ivBack.setOnClickListener(v -> finish());

        ivDelete.setOnClickListener(v -> {
            if (currentTask != null) {
                ConfirmDeleteDialog dialog = ConfirmDeleteDialog.newInstance(currentTask);
                dialog.show(getSupportFragmentManager(), ConfirmDeleteDialog.TAG);
            } else {
                Toast.makeText(TaskInfoActivity.this, "Cannot remove task", Toast.LENGTH_SHORT).show();
            }
        });

        var task_info_layout = findViewById(R.id.task_info_activity_screen);
        ViewCompat.setOnApplyWindowInsetsListener(task_info_layout, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
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
            runOnUiThread(() -> {
                if (category != null) {
                    tvCategoryValue.setText(category.name);
                } else {
                    Log.w(TAG, "Category with ID " + categoryId + " not found.");
                    tvCategoryValue.setText("N/A");
                }
            });
        });
    }

    @Override
    public void onDeleteConfirmed(ToDo task) {
        viewModel.deleteTodo(task);
        finish();
    }

    @Override
    public void onDeleteConfirmed(Category category) {
        // Not used in this activity
    }
}
