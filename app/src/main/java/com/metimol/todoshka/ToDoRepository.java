package com.metimol.todoshka;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.metimol.todoshka.database.AppDatabase;
import com.metimol.todoshka.database.Category;
import com.metimol.todoshka.database.CategoryInfo;
import com.metimol.todoshka.database.ToDo;
import com.metimol.todoshka.database.ToDoDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ToDoRepository {
    private final ToDoDao toDoDao;
    private final LiveData<List<ToDo>> allTodos;
    private final LiveData<List<Category>> allCategories;
    private final LiveData<List<CategoryInfo>> allCategoriesWithCounts;
    private final ExecutorService databaseWriteExecutor;

    public ToDoRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        toDoDao = db.toDoDao();
        allTodos = toDoDao.getAllTodosLiveData();
        allCategories = toDoDao.getAllCategoriesLiveData();
        databaseWriteExecutor = AppDatabase.databaseWriteExecutor;

        MediatorLiveData<List<CategoryInfo>> categoriesWithCountsMediator = new MediatorLiveData<>();

        categoriesWithCountsMediator.addSource(allCategories, categories -> {
            List<ToDo> todos = allTodos.getValue();
            if (categories != null) {
                categoriesWithCountsMediator.setValue(computeCategoryInfo(categories, todos));
            }
        });

        categoriesWithCountsMediator.addSource(allTodos, todos -> {
            List<Category> categories = allCategories.getValue();
            if (categories != null) {
                categoriesWithCountsMediator.setValue(computeCategoryInfo(categories, todos));
            }
        });

        allCategoriesWithCounts = categoriesWithCountsMediator;
    }

    private List<CategoryInfo> computeCategoryInfo(List<Category> categories, List<ToDo> todos) {
        if (categories == null) {
            return new ArrayList<>();
        }

        List<CategoryInfo> categoryInfos = new ArrayList<>();
        for (Category category : categories) {
            CategoryInfo info = new CategoryInfo();
            info.category = category;
            int total = 0;
            int completed = 0;
            if (todos != null) {
                for (ToDo todo : todos) {
                    if (todo.categoryId == category.id) {
                        total++;
                        if (todo.isCompleted) {
                            completed++;
                        }
                    }
                }
            }
            info.totalTasks = total;
            info.completedTasks = completed;
            categoryInfos.add(info);
        }
        return categoryInfos;
    }

    public LiveData<List<ToDo>> getAllTodos() {
        return allTodos;
    }

    public LiveData<List<ToDo>> getTodosForCategory(int categoryId) {
        return toDoDao.getTodosForCategoryLiveData(categoryId);
    }

    public LiveData<List<ToDo>> searchTodos(String searchText) {
        return toDoDao.searchTodos("%" + searchText + "%");
    }

    public void update(ToDo toDo) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            toDoDao.updateToDo(toDo);
        });
    }

    public void delete(ToDo toDo) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            toDoDao.deleteToDo(toDo);
        });
    }

    public void updateCategoriesOrder(List<Category> categories) {
        databaseWriteExecutor.execute(() -> {
            for (int i = 0; i < categories.size(); i++) {
                categories.get(i).position = i + 1;
            }
            toDoDao.updateCategories(categories);
        });
    }

    public void deleteCategory(Category category) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            toDoDao.deleteCategory(category);
        });
    }

    public LiveData<List<Category>> getAllCategoriesLiveData() {
        return allCategories;
    }

    public LiveData<List<CategoryInfo>> getAllCategoriesWithCountsLiveData() {
        return allCategoriesWithCounts;
    }
}
