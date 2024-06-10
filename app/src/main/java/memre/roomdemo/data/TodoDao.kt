package memre.roomdemo.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class TodoDao {

    @Query("SELECT max(id) + 1 FROM todo")
    protected abstract fun getNextId(): Long?

    @Query("SELECT * FROM todo WHERE id = :id")
    protected abstract fun getTodoById(id: Long): Todo?

    @Query("UPDATE todo SET text = :text, done = :done WHERE id = :id")
    protected abstract fun updateTodo(id: Long, text: String, done: Boolean)

    // ----------- Add To-do ----------- //

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun addTodo(todo: Todo)

    @Transaction
    open fun addTodo(text: String): Long {
        val id = getNextId() ?: 1
        val todo = Todo(id, false, text)
        addTodo(todo)
        return id
    }

    // ----------- Remove To-do ----------- //

    @Delete
    abstract fun deleteTodo(todo: Todo)

    @Transaction
    open fun deleteTodo(id: Long): Todo? {
        val todo = getTodoById(id) ?: return null
        deleteTodo(todo)
        return todo
    }

    // ----------- Edit To-do ----------- //

    @Transaction
    open fun editTodoText(id: Long, text: String): String? {
        val todo = getTodoById(id) ?: return null
        updateTodo(id, text, todo.done)
        return todo.text
    }

    @Transaction
    open fun editTodoDone(id: Long, done: Boolean): Boolean? {
        val todo = getTodoById(id) ?: return null
        updateTodo(id, todo.text, done)
        return todo.done
    }

    // ----------- Listen to To-Dos ----------- //

    @Transaction
    @Query("SELECT * FROM todo ORDER BY id ASC")
    abstract fun getTodos(): LiveData<List<Todo>>
}