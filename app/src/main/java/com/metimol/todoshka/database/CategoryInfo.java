package com.metimol.todoshka.database;

import androidx.room.Embedded;

public class CategoryInfo {
    @Embedded
    public Category category;
    public int totalTasks;
    public int completedTasks;
}
