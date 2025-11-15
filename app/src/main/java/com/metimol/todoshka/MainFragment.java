package com.metimol.todoshka;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
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

public class MainFragment extends Fragment implements TaskAdapter.OnTaskCheckedListener, TaskAdapter.OnTaskClickListener {
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireActivity().getSharedPreferences(GetStartedActivity.PREFS_NAME, Context.MODE_PRIVATE);

        AppDatabase db = AppDatabase.getDatabase(requireContext());
        toDoDao = db.toDoDao();

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        tvTitle = view.findViewById(R.id.tvTitle);
        chipContainer = view.findViewById(R.id.chipContainer);
        ImageView ivSettings = view.findViewById(R.id.ivSettings);
        FloatingActionButton fab = view.findViewById(R.id.fab);
        ImageView chipAdd = view.findViewById(R.id.chipAdd);
        etSearch = view.findViewById(R.id.etSearch);
        ivClearIcon = view.findViewById(R.id.ivClearIcon);
        chipScrollView = view.findViewById(R.id.chipScrollView);

        rvTasks = view.findViewById(R.id.rvTasks);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        chipAllTask = view.findViewById(R.id.chipAllTask);

        chipAllTask.setChecked(true);
        setUserName();

        var main_layout = view.findViewById(R.id.main_fragment_screen);

        ViewCompat.setOnApplyWindowInsetsListener(main_layout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return WindowInsetsCompat.CONSUMED;
        });

        setupRecyclerView();
        setupSearch();

        getParentFragmentManager().setFragmentResultListener(
                CreateTaskBottomSheet.REQUEST_KEY,
                this,
                (requestKey, bundle) -> {
                    if (fab != null) {
                        fab.show();
                    }
                }
        );

        getParentFragmentManager().setFragmentResultListener(
                CreateCategoryBottomSheet.REQUEST_KEY,
                this,
                (requestKey, bundle) -> {
                    android.util.Log.d("MainFragment", "CreateCategoryBottomSheet dismissed");
                }
        );

        ivSettings.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_mainFragment_to_settingsFragment);
        });

        fab.setOnClickListener(v -> {
            fab.hide();
            CreateTaskBottomSheet bottomSheet = new CreateTaskBottomSheet();

            Integer currentCatId = viewModel.currentCategoryId.getValue();
            if (currentCatId == null) {
                currentCatId = MainViewModel.ALL_CATEGORIES_ID;
            }

            Bundle args = new Bundle();
            args.putInt(CreateTaskBottomSheet.ARG_CURRENT_CATEGORY_ID, currentCatId);
            bottomSheet.setArguments(args);

            bottomSheet.show(getParentFragmentManager(), CreateTaskBottomSheet.TAG);
        });

        chipAdd.setOnClickListener(v -> {
            CreateCategoryBottomSheet bottomSheet = new CreateCategoryBottomSheet();
            bottomSheet.show(getParentFragmentManager(), CreateCategoryBottomSheet.TAG);
        });

        chipAllTask.setOnClickListener(v -> {
            etSearch.setText("");
            viewModel.loadTasks(MainViewModel.ALL_CATEGORIES_ID);
            updateChipSelection(chipAllTask);
        });

        ivClearIcon.setOnClickListener(v -> {
            etSearch.setText("");
        });

        viewModel.getTasks().observe(getViewLifecycleOwner(), taskObserver);
        toDoDao.getAllCategoriesLiveData().observe(getViewLifecycleOwner(), categoryObserver);
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
    public void onResume() {
        super.onResume();
        setUserName();
    }

    private void setUserName() {
        if (tvTitle != null && sharedPreferences != null) {
            String userName = sharedPreferences.getString(USER_NAME_KEY, "User");
            tvTitle.setText(userName);
        }
    }

    private final androidx.lifecycle.Observer<List<ToDo>> taskObserver = tasks -> {
        boolean isSearching = !etSearch.getText().toString().trim().isEmpty();
        View view = getView();
        if (view == null) return;

        if (tasks == null || tasks.isEmpty()) {
            rvTasks.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            TextView tvEmptyTitle = view.findViewById(R.id.tvEmptyTitle);
            TextView tvEmptySubtitle = view.findViewById(R.id.tvEmptySubtitle);
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
    };

    private final androidx.lifecycle.Observer<List<Category>> categoryObserver = categories -> {
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
    };

    private Integer getSelectedCategoryId() {
        for (Chip chip : categoryChips) {
            if (chip.isChecked()) {
                return (Integer) chip.getTag();
            }
        }
        return null;
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(this, this);
        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
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
        Context context = requireContext();
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
            Vibrator vibrator = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                VibrationEffect effect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE);
                vibrator.vibrate(effect);
            }
        }
    }

    @Override
    public void onTaskClick(ToDo task) {
        Bundle args = new Bundle();
        args.putParcelable("task", task);
        Navigation.findNavController(requireView()).navigate(R.id.action_mainFragment_to_taskInfoFragment, args);
    }
}