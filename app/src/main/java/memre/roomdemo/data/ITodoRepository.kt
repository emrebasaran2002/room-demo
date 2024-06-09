package memre.roomdemo.data

import androidx.lifecycle.LiveData

interface ITodoRepository {
    fun addTodo(todo: Todo)
    fun deleteTodo(id: Int)
    fun editTodoText(id: Int, text: String)
    fun editTodoDone(id: Int, done: Boolean)
    fun getTodos(): LiveData<List<Todo>>
}