package com.metimol.todoshka;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.metimol.todoshka.database.Category;

public class EditCategoriesActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private CategoryAdapter categoryAdapter;
    private RecyclerView rvCategories;

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
        Log.d("PopupMenu", "Showing popup menu for category: " + category.name);
    }

    private void createCategory() {
        CreateCategoryBottomSheet bottomSheet = new CreateCategoryBottomSheet();
        bottomSheet.show(getSupportFragmentManager(), CreateCategoryBottomSheet.TAG);
    }
}
