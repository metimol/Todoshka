package com.metimol.todoshka.database;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "todos",
        foreignKeys = @ForeignKey(entity = Category.class,
                parentColumns = "id",
                childColumns = "categoryId",
                onDelete = ForeignKey.CASCADE))
public class ToDo implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String text;
    @ColumnInfo(defaultValue = "MEDIUM")
    public String priority;
    @ColumnInfo(name = "creation_date")
    public Date creationDate;
    @ColumnInfo(defaultValue = "0")
    public boolean isCompleted;
    public int categoryId;

    public ToDo() {}

    protected ToDo(Parcel in) {
        id = in.readInt();
        text = in.readString();
        priority = in.readString();
        long tmpCreationDate = in.readLong();
        creationDate = tmpCreationDate == -1 ? null : new Date(tmpCreationDate);
        isCompleted = in.readByte() != 0;
        categoryId = in.readInt();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(text);
        dest.writeString(priority);
        dest.writeLong(creationDate != null ? creationDate.getTime() : -1);
        dest.writeByte((byte) (isCompleted ? 1 : 0));
        dest.writeInt(categoryId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ToDo> CREATOR = new Creator<ToDo>() {
        @Override
        public ToDo createFromParcel(Parcel in) {
            return new ToDo(in);
        }

        @Override
        public ToDo[] newArray(int size) {
            return new ToDo[size];
        }
    };
}