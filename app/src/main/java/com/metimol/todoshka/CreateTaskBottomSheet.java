package com.metimol.todoshka;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import com.metimol.todoshka.database.AppDatabase;
import com.metimol.todoshka.database.Category;
import com.metimol.todoshka.database.ToDo;
import com.metimol.todoshka.database.ToDoDao;

import java.util.Date;
import java.util.List;

public class CreateTaskBottomSheet extends BottomSheetDialogFragment {
    public static final String TAG = "CreateTaskBottomSheet";
    public static final String REQUEST_KEY = "taskBottomSheetDismissed";

    public enum Priority {
        LOW(R.drawable.ic_priority_low),
        MEDIUM(R.drawable.ic_priority_medium),
        HIGH(R.drawable.ic_priority_high);

        private final int drawableResId;

        Priority(@DrawableRes int drawableResId) {
            this.drawableResId = drawableResId;
        }

        public int getDrawableResId() {
            return drawableResId;
        }
    }

    private EditText etNewTask;
    private TextView tvSelectedCategory;
    private List<Category> categories;
    private Category selectedCategory;
    private ImageButton selectedPriority;
    private Priority currentPriority;

    private ToDoDao toDoDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_task, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            @SuppressLint("DiscouragedApi") View bottomSheet = bottomSheetDialog.findViewById(
                    bottomSheetDialog.getContext().getResources().getIdentifier(
                            "design_bottom_sheet", "id", bottomSheetDialog.getContext().getPackageName()
                    )
            );

            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppDatabase db = AppDatabase.getDatabase(requireContext());
        toDoDao = db.toDoDao();

        etNewTask = view.findViewById(R.id.etNewTask);
        tvSelectedCategory = view.findViewById(R.id.tvSelectedCategory);
        LinearLayout llCategorySelector = view.findViewById(R.id.llCategorySelector);
        MaterialButton btnAddTask = view.findViewById(R.id.btnAddTask);
        selectedPriority = view.findViewById(R.id.ibSelectedPriority);

        currentPriority = Priority.MEDIUM;
        selectedPriority.setImageResource(currentPriority.getDrawableResId());

        loadCategories();

        llCategorySelector.setOnClickListener(v -> showCategoryMenu());

        etNewTask.requestFocus();

        btnAddTask.setOnClickListener(v -> {
            String taskText = etNewTask.getText().toString().trim();

            if (taskText.isEmpty()) {
                etNewTask.setError("Task cannot be empty");
            } else {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    ToDo toDo = new ToDo();
                    toDo.categoryId = selectedCategory.id;
                    toDo.text = taskText;
                    toDo.priority = currentPriority.name();
                    toDo.creationDate = new Date();
                    toDo.isCompleted = false;

                    toDoDao.insertToDo(toDo);
                    requireActivity().runOnUiThread(this::dismiss);
                });
            }
        });

        selectedPriority.setOnClickListener(v -> selectPriority());
    }

    @SuppressLint("SetTextI18n")
    private void loadCategories() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            categories = toDoDao.getAllCategoriesInternal();

            requireActivity().runOnUiThread(() -> {
                if (categories != null && !categories.isEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        selectedCategory = categories.getFirst();
                    } else if (!categories.isEmpty()) {
                        selectedCategory = categories.get(0);
                    }
                    if (selectedCategory != null) {
                        tvSelectedCategory.setText(selectedCategory.name);
                    } else {
                        tvSelectedCategory.setText("No Category");
                    }
                } else {
                    tvSelectedCategory.setText("No Category");
                }
            });
        });
    }

    private void showCategoryMenu() {
        if (categories == null || categories.isEmpty()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_category_selector, null);

        RecyclerView rvCategories = dialogView.findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));

        int selectedPosition = 0;
        if (selectedCategory != null) {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).id == selectedCategory.id) {
                    selectedPosition = i;
                    break;
                }
            }
        }

        AlertDialog dialog = builder.setView(dialogView).create();

        CategorySelectorAdapter adapter = new CategorySelectorAdapter(categories, selectedPosition,
                (category, position) -> {
                    selectedCategory = category;
                    tvSelectedCategory.setText(category.name);
                    dialog.dismiss();
                });

        rvCategories.setAdapter(adapter);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
    }

    private void selectPriority() {
        switch (currentPriority) {
            case LOW:
                currentPriority = Priority.MEDIUM;
                break;
            case MEDIUM:
                currentPriority = Priority.HIGH;
                break;
            case HIGH:
                currentPriority = Priority.LOW;
                break;
        }
        selectedPriority.setImageResource(currentPriority.getDrawableResId());
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        getParentFragmentManager().setFragmentResult(REQUEST_KEY, new Bundle());
    }
}