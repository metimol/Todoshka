package com.metimol.todoshka.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Transaction;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface ToDoDao {

    // --- CREATE ---
    @Insert
    void insertCategory(Category category);

    @Insert
    void insertToDo(ToDo todo);


    // --- READ ---
    @Query("SELECT * FROM categories ORDER BY position ASC")
    List<Category> getAllCategoriesInternal();

    @Query("SELECT COUNT(*) FROM todos WHERE categoryId = :categoryId")
    int getTotalTaskCountForCategory(int categoryId);

    @Query("SELECT COUNT(*) FROM todos WHERE categoryId = :categoryId AND isCompleted = 1")
    int getCompletedTaskCountForCategory(int categoryId);

    @Transaction
    default List<CategoryInfo> getAllCategoriesWithCounts() {
        List<Category> categories = getAllCategoriesInternal();
        List<CategoryInfo> categoryInfos = new ArrayList<>();
        for (Category category : categories) {
            CategoryInfo info = new CategoryInfo();
            info.category = category;
            info.totalTasks = getTotalTaskCountForCategory(category.id);
            info.completedTasks = getCompletedTaskCountForCategory(category.id);
            categoryInfos.add(info);
        }
        return categoryInfos;
    }

    @Query("SELECT * FROM categories ORDER BY position ASC")
    LiveData<List<Category>> getAllCategoriesLiveData();

    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    Category getCategoryById(int categoryId);

    @Query("SELECT * FROM todos ORDER BY isCompleted ASC, creation_date DESC, id DESC")
    LiveData<List<ToDo>> getAllTodosLiveData();

    @Query("SELECT * FROM todos WHERE categoryId = :categoryId ORDER BY isCompleted ASC, creation_date DESC, id DESC")
    LiveData<List<ToDo>> getTodosForCategoryLiveData(int categoryId);

    @Query("SELECT * FROM todos WHERE text LIKE :searchText ORDER BY isCompleted ASC, creation_date DESC, id DESC")
    LiveData<List<ToDo>> searchTodos(String searchText);


    // --- UPDATE ---
    @Update
    void updateToDo(ToDo todo);

    @Update
    void updateCategory(Category category);

    @Update
    void updateCategories(List<Category> categories);


    // --- DELETE ---
    @Delete
    void deleteToDo(ToDo todo);

    @Delete
    void deleteCategory(Category category);
}
