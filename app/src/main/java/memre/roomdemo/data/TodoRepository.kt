package memre.roomdemo.data

import android.content.Context
import androidx.lifecycle.LiveData

class TodoRepository private constructor(private val dao: TodoDao) : ITodoRepository {

    // Constructor to instantiate a production instance of TodoRepository.
    constructor(context: Context): this(TodoDatabase.getInstance(context).getDao())

    companion object {
        // Factory function to get a repository implementation that uses an
        // in-memory fake database dependency.
        fun getTestInstance(context: Context): ITodoRepository =
            TodoRepository(TodoDatabase.getTestInstance(context).getDao())
    }

    override fun addTodo(text: String): Long {
        return dao.addTodo(text)
    }

    override fun addTodo(todo: Todo) {
        dao.addTodo(todo)
    }

    override fun deleteTodo(id: Long): Todo? {
        return dao.deleteTodo(id)
    }

    override fun deleteTodo(todo: Todo) {
        dao.deleteTodo(todo)
    }

    override fun editTodoText(id: Long, text: String): String? {
        return dao.editTodoText(id, text)
    }

    override fun editTodoDone(id: Long, done: Boolean): Boolean? {
        return dao.editTodoDone(id, done)
    }

    override fun getTodos(): LiveData<List<Todo>> {
        return dao.getTodos()
    }
}