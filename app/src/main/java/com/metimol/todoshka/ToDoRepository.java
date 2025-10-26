package com.metimol.todoshka;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.metimol.todoshka.database.AppDatabase;
import com.metimol.todoshka.database.Category;
import com.metimol.todoshka.database.ToDo;
import com.metimol.todoshka.database.ToDoDao;
import java.util.List;

public class ToDoRepository {
    private final ToDoDao toDoDao;
    private final LiveData<List<ToDo>> allTodos;
    private final LiveData<List<Category>> allCategories;

    public ToDoRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        toDoDao = db.toDoDao();
        allTodos = toDoDao.getAllTodosLiveData();
        allCategories = toDoDao.getAllCategoriesLiveData();
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

    public LiveData<List<Category>> getAllCategoriesLiveData() {
        return allCategories;
    }
}
