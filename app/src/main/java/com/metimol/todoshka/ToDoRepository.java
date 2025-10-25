package com.metimol.todoshka;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.metimol.todoshka.database.AppDatabase;
import com.metimol.todoshka.database.ToDo;
import com.metimol.todoshka.database.ToDoDao;
import java.util.List;

public class ToDoRepository {
    private final ToDoDao toDoDao;
    private final LiveData<List<ToDo>> allTodos;

    public ToDoRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        toDoDao = db.toDoDao();
        allTodos = toDoDao.getAllTodosLiveData();
    }

    public LiveData<List<ToDo>> getAllTodos() {
        return allTodos;
    }

    public LiveData<List<ToDo>> getTodosForCategory(int categoryId) {
        return toDoDao.getTodosForCategoryLiveData(categoryId);
    }

    public LiveData<ToDo> getTodoById(int taskId) {
        return toDoDao.getTodoById(taskId);
    }

    public LiveData<List<ToDo>> searchTodos(String searchText) {
        return toDoDao.searchTodos(searchText);
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
}