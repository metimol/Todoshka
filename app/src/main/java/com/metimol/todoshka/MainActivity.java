package com.metimol.todoshka;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.metimol.todoshka.database.AppDatabase;
import com.metimol.todoshka.database.Category;
import com.metimol.todoshka.database.ToDo;
import com.metimol.todoshka.database.ToDoDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskCheckedListener, TaskAdapter.OnTaskClickListener {
    public static final String USER_NAME_KEY = "UserName";

    private ToDoDao toDoDao;

    private TextView tvTitle;
    private LinearLayout chipContainer;
    private SharedPreferences sharedPreferences;

    private MainViewModel viewModel;
    private RecyclerView rvTasks;
    private TaskAdapter taskAdapter;
    private LinearLayout emptyStateLayout;
    private Chip chipAllTask;
    private EditText etSearch;
    private ImageView ivClearIcon;
    private HorizontalScrollView chipScrollView;

    private final List<Chip> categoryChips = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        sharedPreferences = getSharedPreferences(GetStartedActivity.PREFS_NAME, Context.MODE_PRIVATE);


        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        toDoDao = db.toDoDao();

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        tvTitle = findViewById(R.id.tvTitle);
        chipContainer = findViewById(R.id.chipContainer);
        ImageView ivSettings = findViewById(R.id.ivSettings);
        FloatingActionButton fab = findViewById(R.id.fab);
        ImageView chipAdd = findViewById(R.id.chipAdd);
        etSearch = findViewById(R.id.etSearch);
        ivClearIcon = findViewById(R.id.ivClearIcon);
        chipScrollView = findViewById(R.id.chipScrollView);

        rvTasks = findViewById(R.id.rvTasks);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        chipAllTask = findViewById(R.id.chipAllTask);

        chipAllTask.setChecked(true);
        setUserName();

        var main_layout = findViewById(R.id.main_activity_screen);

        ViewCompat.setOnApplyWindowInsetsListener(main_layout, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return WindowInsetsCompat.CONSUMED;
        });

        setupRecyclerView();
        setupCategoryObserver();
        setupTaskObserver();
        setupSearch();

        getSupportFragmentManager().setFragmentResultListener(
                CreateTaskBottomSheet.REQUEST_KEY,
                this,
                (requestKey, bundle) -> {
                    if (fab != null) {
                        fab.show();
                    }
                }
        );

        getSupportFragmentManager().setFragmentResultListener(
                CreateCategoryBottomSheet.REQUEST_KEY,
                this,
                (requestKey, bundle) -> {
                    android.util.Log.d("MainActivity", "CreateCategoryBottomSheet dismissed");
                }
        );


        ivSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        fab.setOnClickListener(v -> {
            fab.hide();
            CreateTaskBottomSheet bottomSheet = new CreateTaskBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), CreateTaskBottomSheet.TAG);
        });

        chipAdd.setOnClickListener(v -> {
            CreateCategoryBottomSheet bottomSheet = new CreateCategoryBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), CreateCategoryBottomSheet.TAG);
        });

        chipAllTask.setOnClickListener(v -> {
            etSearch.setText("");
            viewModel.loadTasks(MainViewModel.ALL_CATEGORIES_ID);
            updateChipSelection(chipAllTask);
        });

        ivClearIcon.setOnClickListener(v -> {
            etSearch.setText("");
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString().trim();
                viewModel.setSearchQuery(searchText.isEmpty() ? MainViewModel.NO_SEARCH : searchText);

                if (searchText.isEmpty()) {
                    ivClearIcon.setVisibility(View.GONE);
                    chipScrollView.setVisibility(View.VISIBLE);
                    if (!isAnyChipChecked()) {
                        chipAllTask.setChecked(true);
                        viewModel.loadTasks(MainViewModel.ALL_CATEGORIES_ID);
                    }
                } else {
                    ivClearIcon.setVisibility(View.VISIBLE);
                    chipScrollView.setVisibility(View.GONE);
                    // Снимаем выбор со всех чипов при поиске
                    updateChipSelection(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private boolean isAnyChipChecked() {
        if (chipAllTask != null && chipAllTask.isChecked()) {
            return true;
        }
        for (Chip chip : categoryChips) {
            if (chip.isChecked()) {
                return true;
            }
        }
        return false;
    }


    @Override
    protected void onResume() {
        super.onResume();
        setUserName();
    }

    private void setUserName() {
        if (tvTitle != null && sharedPreferences != null) {
            String userName = sharedPreferences.getString(USER_NAME_KEY, "User");
            tvTitle.setText(userName);
        }
    }


    private void setupCategoryObserver() {
        toDoDao.getAllCategoriesLiveData().observe(this, categories -> {
            Integer previouslySelectedCategoryId = getSelectedCategoryId();

            for (Chip chip : categoryChips) {
                chipContainer.removeView(chip);
            }
            categoryChips.clear();

            boolean restoredSelection = false;
            for (Category category : categories) {
                Chip newChip = addCategoryChip(category);
                if (previouslySelectedCategoryId != null && previouslySelectedCategoryId.equals(category.id)) {
                    newChip.setChecked(true);
                    restoredSelection = true;
                }
            }

            if (etSearch.getText().toString().trim().isEmpty()) {
                if (!restoredSelection) {
                    chipAllTask.setChecked(true);
                    if (!Objects.equals(viewModel.currentCategoryId.getValue(), MainViewModel.ALL_CATEGORIES_ID)) {
                        viewModel.loadTasks(MainViewModel.ALL_CATEGORIES_ID);
                    }
                } else {
                    chipAllTask.setChecked(false);
                }
            } else {
                chipAllTask.setChecked(false);
            }
        });
    }

    private Integer getSelectedCategoryId() {
        for (Chip chip : categoryChips) {
            if (chip.isChecked()) {
                return (Integer) chip.getTag();
            }
        }
        return null;
    }

    @SuppressLint("SetTextI18n")
    private void setupTaskObserver() {
        viewModel.getTasks().observe(this, tasks -> {
            boolean isSearching = !etSearch.getText().toString().trim().isEmpty();
            if (tasks == null || tasks.isEmpty()) {
                rvTasks.setVisibility(View.GONE);
                emptyStateLayout.setVisibility(View.VISIBLE);
                TextView tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
                TextView tvEmptySubtitle = findViewById(R.id.tvEmptySubtitle);
                if (isSearching) {
                    tvEmptyTitle.setText(R.string.nothing_found);
                    tvEmptySubtitle.setText(R.string.try_different_search);
                } else {
                    tvEmptyTitle.setText(R.string.empty_taskbox);
                    tvEmptySubtitle.setText(R.string.empty_taskbox_hint);
                }

            } else {
                rvTasks.setVisibility(View.VISIBLE);
                emptyStateLayout.setVisibility(View.GONE);
                taskAdapter.submitList(tasks);
                if (!isSearching && !rvTasks.canScrollVertically(-1)) {
                    rvTasks.scrollToPosition(0);
                }
            }
        });
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(this, this);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        rvTasks.setAdapter(taskAdapter);

        taskAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if (positionStart == 0 && etSearch.getText().toString().trim().isEmpty() && !rvTasks.canScrollVertically(-1)) {
                    rvTasks.scrollToPosition(0);
                }
            }
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
            }
        });
    }

    private Chip addCategoryChip(Category category) {
        Context context = this;
        LayoutInflater inflater = LayoutInflater.from(context);

        Chip newChip = (Chip) inflater.inflate(R.layout.chip_category, chipContainer, false);
        newChip.setText(category.name);
        newChip.setTag(category.id);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMarginStart((int) Utils.dpToPx(context, 8));
        newChip.setLayoutParams(params);

        newChip.setOnClickListener(v -> {
            etSearch.setText("");
            viewModel.loadTasks(category.id);
            updateChipSelection(newChip);
        });

        chipContainer.addView(newChip);
        categoryChips.add(newChip);
        return newChip;
    }


    private void updateChipSelection(Chip selectedChip) {
        if (chipAllTask != null && chipAllTask != selectedChip) {
            chipAllTask.setChecked(false);
        }
        for (Chip chip : categoryChips) {
            if (chip != null && chip != selectedChip) {
                chip.setChecked(false);
            }
        }
        if (selectedChip != null && !selectedChip.isChecked()) {
            selectedChip.setChecked(true);
        } else if (selectedChip == null && chipAllTask != null) {
            chipAllTask.setChecked(false);
        }
    }


    @Override
    public void onTaskChecked(ToDo task, boolean isChecked) {
        task.isCompleted = isChecked;
        viewModel.updateTodo(task);
        if (isChecked) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(50);
            }
        }
    }

    @Override
    public void onTaskClick(ToDo task) {
        Intent intent = new Intent(MainActivity.this, TaskInfoActivity.class);
        intent.putExtra(TaskInfoActivity.EXTRA_TASK, task);
        startActivity(intent);
    }
}
