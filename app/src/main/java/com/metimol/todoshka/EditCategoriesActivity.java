package com.metimol.todoshka;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        categoryAdapter.setOnCategorySettingsClickListener((category, anchorView) -> {
            android.widget.Toast.makeText(this, "Settings for: " + category.name, android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void observeCategories() {
        // Observe the LiveData from the ViewModel
        viewModel.getCategories().observe(this, categories -> {
            if (categories != null) {
                categoryAdapter.submitList(categories);
                rvCategories.setVisibility(categories.isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);
            }
        });
    }


    private void createCategory() {
        CreateCategoryBottomSheet bottomSheet = new CreateCategoryBottomSheet();
        bottomSheet.show(getSupportFragmentManager(), CreateCategoryBottomSheet.TAG);
    }
}