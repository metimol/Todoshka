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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.metimol.todoshka.database.Category;
import com.metimol.todoshka.database.ToDo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EditCategoriesActivity extends AppCompatActivity implements
        ConfirmDeleteDialog.ConfirmDeleteListener, CategoryAdapter.OnStartDragListener {

    private static final String TAG = "EditCategoriesActivity";

    private MainViewModel viewModel;
    private CategoryAdapter categoryAdapter;
    private RecyclerView rvCategories;
    private PopupWindow popupWindow;
    private ItemTouchHelper itemTouchHelper;

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
        setupItemTouchHelper();

        ivBack.setOnClickListener(v -> finish());
        llAddCategory.setOnClickListener(v -> createCategory());
        ivDone.setOnClickListener(v -> {
            saveCategoryOrder();
            finish();
        });

        var editCategoriesLayout = findViewById(R.id.edit_categories_activity_screen);
        ViewCompat.setOnApplyWindowInsetsListener(editCategoriesLayout, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryAdapter();
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setAdapter(categoryAdapter);
        categoryAdapter.setOnCategorySettingsClickListener(this::showCategoryPopupMenu);
        categoryAdapter.setOnStartDragListener(this);
    }

    private void setupItemTouchHelper() {
        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                if (fromPosition == RecyclerView.NO_POSITION || toPosition == RecyclerView.NO_POSITION) {
                    return false;
                }
                categoryAdapter.onItemMove(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return false;
            }
        };

        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(rvCategories);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        if (itemTouchHelper != null) {
            itemTouchHelper.startDrag(viewHolder);
        }
    }

    private void observeCategories() {
        viewModel.getCategoriesWithCounts().observe(this, categoryInfos -> {
            if (categoryInfos != null) {
                String orderLog = categoryInfos.stream()
                        .map(info -> info.category.name + "(pos:" + info.category.position + ")")
                        .collect(Collectors.joining(", "));

                categoryAdapter.submitList(categoryInfos);
                rvCategories.setVisibility(categoryInfos.isEmpty() ? View.GONE : View.VISIBLE);
            } else {
                categoryAdapter.submitList(new ArrayList<>());
                rvCategories.setVisibility(View.GONE);
            }
        });
    }

    private void saveCategoryOrder() {
        List<Category> updatedCategories = categoryAdapter.getCurrentCategoryList();
        if (updatedCategories != null && !updatedCategories.isEmpty()) {
            String orderLog = updatedCategories.stream()
                    .map(cat -> cat.name + "(id:" + cat.id + ")")
                    .collect(Collectors.joining(", "));

            viewModel.updateCategoriesOrder(updatedCategories);
        } else {
            Log.w(TAG, "saveCategoryOrder: Adapter list is empty, nothing to save.");
        }
    }

    private void showCategoryPopupMenu(Category category, View anchorView) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View popupView = inflater.inflate(R.layout.item_category_settings, null);

        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setElevation(10f);

        LinearLayout deleteAction = popupView.findViewById(R.id.action_delete_category);
        deleteAction.setOnClickListener(v -> {
            popupWindow.dismiss();
            showConfirmDeleteDialog(category);
        });

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = popupView.getMeasuredWidth();
        int xOffset = -popupWidth - (int) Utils.dpToPx(this, 10);
        int yOffset = -anchorView.getHeight() / 2 - popupView.getMeasuredHeight() / 2;

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
        if (categoryAdapter.getItemCount() > 1) {
            viewModel.deleteCategory(category);
        } else {
            Toast.makeText(this, "You must have at least one category.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteConfirmed(ToDo task) { }

    @Override
    protected void onPause() {
        super.onPause();
        saveCategoryOrder();
    }
}
