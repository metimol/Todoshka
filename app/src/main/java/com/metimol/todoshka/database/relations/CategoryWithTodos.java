package com.metimol.todoshka.database.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.metimol.todoshka.database.Category;
import com.metimol.todoshka.database.ToDo;

import java.util.List;

public class CategoryWithTodos {
    @Embedded
    public Category category;

    @Relation(
            parentColumn = "id",
            entityColumn = "categoryId"
    )
    public List<ToDo> todos;
}
