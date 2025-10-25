package com.metimol.todoshka;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import com.metimol.todoshka.database.ToDo;
import java.util.List;
import java.util.Objects;

public class MainViewModel extends AndroidViewModel {
    private final ToDoRepository repository;

    private final MediatorLiveData<List<ToDo>> tasksLiveData = new MediatorLiveData<>();

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();

    private final MutableLiveData<Integer> currentCategoryId = new MutableLiveData<>(ALL_CATEGORIES_ID);

    private LiveData<List<ToDo>> currentSource;

    public static final int ALL_CATEGORIES_ID = -1;
    public static final String NO_SEARCH = null;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new ToDoRepository(application);

        tasksLiveData.addSource(currentCategoryId, categoryId -> updateDataSource());
        tasksLiveData.addSource(searchQuery, query -> updateDataSource());

        searchQuery.setValue(NO_SEARCH);
    }

    public LiveData<List<ToDo>> getTasks() {
        return tasksLiveData;
    }

    private void updateDataSource() {
        String query = searchQuery.getValue();
        Integer categoryId = currentCategoryId.getValue();

        if (query != null && !query.isEmpty()) {
            switchToNewSource(repository.searchTodos(query));
        }
        else if (categoryId != null) {
            if (categoryId == ALL_CATEGORIES_ID) {
                switchToNewSource(repository.getAllTodos());
            } else {
                switchToNewSource(repository.getTodosForCategory(categoryId));
            }
        }
    }

    private void switchToNewSource(LiveData<List<ToDo>> newSource) {
        if (currentSource != null) {
            tasksLiveData.removeSource(currentSource);
        }
        currentSource = newSource;
        tasksLiveData.addSource(currentSource, tasksLiveData::setValue);
    }

    public void setSearchQuery(String query) {
        if (query != null && !query.isEmpty()) {
            currentCategoryId.setValue(ALL_CATEGORIES_ID);
        }
        if (!Objects.equals(searchQuery.getValue(), query)) {
            searchQuery.setValue(query);
        }
    }

    public void loadTasks(int categoryId) {
        if (!Objects.equals(searchQuery.getValue(), NO_SEARCH)) {
            searchQuery.setValue(NO_SEARCH);
        }
        if (!Objects.equals(currentCategoryId.getValue(), categoryId)) {
            currentCategoryId.setValue(categoryId);
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