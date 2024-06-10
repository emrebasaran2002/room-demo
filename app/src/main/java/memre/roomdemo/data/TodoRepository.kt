package memre.roomdemo.data

import android.content.Context
import androidx.lifecycle.LiveData

class TodoRepository(context: Context) : ITodoRepository {

    private val dao: TodoDao = TodoDatabase.getInstance(context).getDao()

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