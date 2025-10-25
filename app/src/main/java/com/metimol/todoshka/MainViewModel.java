package com.metimol.todoshka;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.metimol.todoshka.database.ToDo;
import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private final ToDoRepository repository;

    private final MediatorLiveData<List<ToDo>> tasksLiveData = new MediatorLiveData<>();

    private LiveData<List<ToDo>> allTasksSource;
    private LiveData<List<ToDo>> categoryTasksSource;

    public static final int ALL_CATEGORIES_ID = -1;
    private int currentCategoryId = ALL_CATEGORIES_ID;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new ToDoRepository(application);
        loadTasks(currentCategoryId);
    }

    public LiveData<List<ToDo>> getTasks() {
        return tasksLiveData;
    }

    public void loadTasks(int categoryId) {
        currentCategoryId = categoryId;

        if (categoryTasksSource != null) {
            tasksLiveData.removeSource(categoryTasksSource);
            categoryTasksSource = null;
        }
        if (allTasksSource != null) {
            tasksLiveData.removeSource(allTasksSource);
            allTasksSource = null;
        }

        if (categoryId == ALL_CATEGORIES_ID) {
            allTasksSource = repository.getAllTodos();
            tasksLiveData.addSource(allTasksSource, tasksLiveData::setValue);
        } else {
            categoryTasksSource = repository.getTodosForCategory(categoryId);
            tasksLiveData.addSource(categoryTasksSource, tasksLiveData::setValue);
        }
    }

    public LiveData<ToDo> getTodoById(int taskId) {
        return repository.getTodoById(taskId);
    }

    public void updateTodo(ToDo toDo) {
        repository.update(toDo);
    }

    public void deleteTodo(ToDo toDo) {
        repository.delete(toDo);
    }
}
