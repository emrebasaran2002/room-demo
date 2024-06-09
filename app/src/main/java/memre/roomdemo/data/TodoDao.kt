package memre.roomdemo.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TodoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addTodo(todo: Todo)

    @Query("DELETE FROM todo WHERE id = :id")
    fun deleteTodo(id: Int)

    @Query("UPDATE todo SET text = :text WHERE id = :id")
    fun editTodoText(id: Int, text: String)

    @Query("UPDATE todo SET done = :done WHERE id = :id")
    fun editTodoDone(id: Int, done: Boolean)

    @Query("SELECT * FROM todo ORDER BY timestamp ASC")
    fun getTodos(): LiveData<List<Todo>>
}