package com.metimol.todoshka;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import com.metimol.todoshka.database.AppDatabase;
import com.metimol.todoshka.database.Category;
import com.metimol.todoshka.database.ToDoDao;

import java.util.List;

public class CreateCategoryBottomSheet extends BottomSheetDialogFragment {
    public static final String TAG = "CreateCategoryBottomSheet";
    public static final String REQUEST_KEY = "categoryBottomSheetDismissed";

    private EditText etNewCategory;
    private ToDoDao toDoDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_category, container, false);
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

        etNewCategory = view.findViewById(R.id.etNewCategory);
        MaterialButton btnAddCategory = view.findViewById(R.id.btnAddCategory);

        btnAddCategory.setOnClickListener(v -> addCategory());

        etNewCategory.requestFocus();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        getParentFragmentManager().setFragmentResult(REQUEST_KEY, new Bundle());
    }

    private void addCategory() {
        String categoryName = etNewCategory.getText().toString();
        if (categoryName.isEmpty()) {
            etNewCategory.setError("Category cannot be empty");
        } else {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                List<Category> categories = toDoDao.getAllCategoriesInternal();
                Category category = new Category();

                category.name = categoryName;
                category.position = categories.size() + 1;
                toDoDao.insertCategory(category);

                requireActivity().runOnUiThread(this::dismiss);
            });
        }
    }
}
