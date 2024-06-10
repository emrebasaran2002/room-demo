package memre.roomdemo.data

import androidx.lifecycle.LiveData

interface ITodoRepository {
    fun addTodo(text: String): Long
    fun addTodo(todo: Todo)
    fun deleteTodo(id: Long): Todo?
    fun deleteTodo(todo: Todo)
    fun editTodoText(id: Long, text: String): String?
    fun editTodoDone(id: Long, done: Boolean): Boolean?
    fun getTodos(): LiveData<List<Todo>>
}