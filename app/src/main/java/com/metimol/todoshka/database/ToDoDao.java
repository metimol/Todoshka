package com.metimol.todoshka.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

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

    @Query("SELECT * FROM todos ORDER BY isCompleted ASC, creation_date DESC, id DESC")
    LiveData<List<ToDo>> getAllTodosLiveData();

    @Query("SELECT * FROM todos WHERE categoryId = :categoryId ORDER BY isCompleted ASC, creation_date DESC, id DESC")
    LiveData<List<ToDo>> getTodosForCategoryLiveData(int categoryId);

    @Query("SELECT * FROM todos WHERE text LIKE :searchText ORDER BY isCompleted ASC, creation_date DESC, id DESC")
    LiveData<List<ToDo>> searchTodos(String searchText);


    // --- UPDATE ---
    @Update
    void updateToDo(ToDo todo);


    // --- DELETE ---
    @Delete
    void deleteToDo(ToDo todo);
}