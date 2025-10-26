package com.metimol.todoshka;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import com.metimol.todoshka.database.Category;
import com.metimol.todoshka.database.CategoryInfo;
import com.metimol.todoshka.database.ToDo;
import java.util.List;
import java.util.Objects;

public class MainViewModel extends AndroidViewModel {
    private final ToDoRepository repository;

    private final MediatorLiveData<List<ToDo>> tasksLiveData = new MediatorLiveData<>();
    private final LiveData<List<CategoryInfo>> categoriesWithCountsLiveData;

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentCategoryId = new MutableLiveData<>(ALL_CATEGORIES_ID);

    private LiveData<List<ToDo>> currentSource;

    public static final int ALL_CATEGORIES_ID = -1;
    public static final String NO_SEARCH = null;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new ToDoRepository(application);
        categoriesWithCountsLiveData = repository.getAllCategoriesWithCountsLiveData();

        tasksLiveData.addSource(currentCategoryId, categoryId -> updateDataSource());
        tasksLiveData.addSource(searchQuery, query -> updateDataSource());

        searchQuery.setValue(NO_SEARCH);
    }

    public LiveData<List<ToDo>> getTasks() {
        return tasksLiveData;
    }

    public LiveData<List<CategoryInfo>> getCategoriesWithCounts() {
        return categoriesWithCountsLiveData;
    }

    public LiveData<List<Category>> getCategories() {
        return repository.getAllCategoriesLiveData();
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
        } else {
            switchToNewSource(repository.getAllTodos());
        }
    }


    private void switchToNewSource(LiveData<List<ToDo>> newSource) {
        if (currentSource != null) {
            tasksLiveData.removeSource(currentSource);
        }
        currentSource = newSource;
        if (currentSource != null) {
            tasksLiveData.addSource(currentSource, tasksLiveData::setValue);
        }
    }


    public void setSearchQuery(String query) {
        if (query != null && !query.isEmpty() && !Objects.equals(currentCategoryId.getValue(), ALL_CATEGORIES_ID)) {
            currentCategoryId.setValue(ALL_CATEGORIES_ID);
        }
        if (!Objects.equals(searchQuery.getValue(), query)) {
            searchQuery.setValue(query);
        } else if (query == null || query.isEmpty()){
            if (!Objects.equals(currentCategoryId.getValue(), ALL_CATEGORIES_ID)) {
                updateDataSource();
            }
        }
    }


    public void loadTasks(int categoryId) {
        if (!Objects.equals(searchQuery.getValue(), NO_SEARCH)) {
            searchQuery.setValue(NO_SEARCH);
        }
        if (!Objects.equals(currentCategoryId.getValue(), categoryId)) {
            currentCategoryId.setValue(categoryId);
        } else {
            updateDataSource();
        }
    }

    public void updateTodo(ToDo toDo) {
        repository.update(toDo);
    }

    public void deleteTodo(ToDo toDo) {
        repository.delete(toDo);
    }
}