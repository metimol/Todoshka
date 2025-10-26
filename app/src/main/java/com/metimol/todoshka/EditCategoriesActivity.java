package com.metimol.todoshka;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.metimol.todoshka.database.Category;
import com.metimol.todoshka.database.ToDo;

public class EditCategoriesActivity extends AppCompatActivity implements ConfirmDeleteDialog.ConfirmDeleteListener {

    private MainViewModel viewModel;
    private CategoryAdapter categoryAdapter;
    private RecyclerView rvCategories;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_category_activity);

        ImageView ivBack = findViewById(R.id.ivBack);
        LinearLayout llAddCategory = findViewById(R.id.llAddCategory);
        rvCategories = findViewById(R.id.rvCategories);
        ImageView ivDone = findViewById(R.id.ivDone);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setupRecyclerView();
        observeCategories();

        ivBack.setOnClickListener(v -> finish());
        llAddCategory.setOnClickListener(v -> createCategory());
        ivDone.setOnClickListener(v -> finish());

        var editCategoriesLayout = findViewById(R.id.edit_categories_activity_screen);
        ViewCompat.setOnApplyWindowInsetsListener(editCategoriesLayout, (view, insets) -> {
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

    private void setupRecyclerView() {
        categoryAdapter = new CategoryAdapter();
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setAdapter(categoryAdapter);

        categoryAdapter.setOnCategorySettingsClickListener(this::showCategoryPopupMenu);
    }

    private void observeCategories() {
        viewModel.getCategoriesWithCounts().observe(this, categoryInfos -> {
            if (categoryInfos != null) {
                categoryAdapter.submitList(categoryInfos);
                rvCategories.setVisibility(categoryInfos.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void showCategoryPopupMenu(Category category, View anchorView) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.item_category_settings, null);

        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setElevation(10f);

        LinearLayout deleteAction = popupView.findViewById(R.id.action_delete_category);
        deleteAction.setOnClickListener(v -> {
            popupWindow.dismiss();
            showConfirmDeleteDialog(category);
        });

        int xOffset = -popupView.getWidth() - 150;
        int yOffset = -anchorView.getHeight();

        popupWindow.showAsDropDown(anchorView, xOffset, yOffset);
    }

    private void createCategory() {
        CreateCategoryBottomSheet bottomSheet = new CreateCategoryBottomSheet();
        bottomSheet.show(getSupportFragmentManager(), CreateCategoryBottomSheet.TAG);
    }

    private void showConfirmDeleteDialog(Category category) {
        ConfirmDeleteDialog dialog = ConfirmDeleteDialog.newInstance(category);
        dialog.show(getSupportFragmentManager(), ConfirmDeleteDialog.TAG);
    }

    @Override
    public void onDeleteConfirmed(Category category) {
        Log.d("EditCategoriesActivity", "Deletion confirmed for category: " + category.name);
        viewModel.deleteCategory(category);
    }

    @Override
    public void onDeleteConfirmed(ToDo task) {
        // Not used in this activity
    }
}