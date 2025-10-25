package com.metimol.todoshka.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.metimol.todoshka.database.relations.CategoryWithTodos;

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
    List<Category> getAllCategories();

    @Query("SELECT * FROM categories ORDER BY position ASC")
    LiveData<List<Category>> getAllCategoriesLiveData();

    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    Category getCategoryById(int categoryId);

    @Transaction
    @Query("SELECT * FROM categories")
    public List<CategoryWithTodos> getCategoriesWithTodos();

    @Query("SELECT * FROM todos ORDER BY isCompleted ASC, id DESC")
    LiveData<List<ToDo>> getAllTodosLiveData();

    @Query("SELECT * FROM todos WHERE categoryId = :categoryId ORDER BY isCompleted ASC, id DESC")
    LiveData<List<ToDo>> getTodosForCategoryLiveData(int categoryId);

    @Query("SELECT * FROM todos WHERE id = :taskId LIMIT 1")
    LiveData<ToDo> getTodoById(int taskId);

    @Query("SELECT * FROM todos WHERE text LIKE '%' || :searchText || '%' ORDER BY isCompleted ASC, id DESC")
    LiveData<List<ToDo>> searchTodos(String searchText);


    // --- UPDATE ---
    @Update
    void updateToDo(ToDo todo);


    // --- DELETE ---
    @Delete
    void deleteCategory(Category category);

    @Delete
    void deleteToDo(ToDo todo);
}