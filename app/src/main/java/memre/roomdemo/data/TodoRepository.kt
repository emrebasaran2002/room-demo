package memre.roomdemo.data

import android.content.Context
import androidx.lifecycle.LiveData

class TodoRepository(context: Context) : ITodoRepository {

    private val dao: TodoDao = TodoDatabase.getInstance(context).getDao()

    override fun addTodo(todo: Todo) {
        dao.addTodo(todo)
    }

    override fun deleteTodo(id: Int) {
        dao.deleteTodo(id)
    }

    override fun editTodoText(id: Int, text: String) {
        dao.editTodoText(id, text)
    }

    override fun editTodoDone(id: Int, done: Boolean) {
        dao.editTodoDone(id, done)
    }

    override fun getTodos(): LiveData<List<Todo>> {
        return dao.getTodos()
    }
}