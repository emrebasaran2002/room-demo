package memre.roomdemo.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo")
data class Todo(
    @ColumnInfo(name = "id")
    @PrimaryKey
    val id: Long,

    @ColumnInfo(name = "done")
    val done: Boolean,

    @ColumnInfo(name = "text")
    val text: String
)