package memre.roomdemo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import memre.roomdemo.data.ITodoRepository
import memre.roomdemo.data.Todo
import memre.roomdemo.data.TodoRepository
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TodoRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var todoRepository: ITodoRepository

    private lateinit var todosObserver: Observer<List<Todo>>
    private lateinit var todosObservable: LiveData<List<Todo>>
    private lateinit var todos: List<Todo>

    @Before
    fun setup() {
        // Get test instance of repository.
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        todoRepository = TodoRepository.getTestInstance(context)

        // Set-up todos list to be updated, for use in tests.
        todos = listOf()
        todosObserver = Observer { todos = it }
        todosObservable = todoRepository.getTodos().apply { observeForever(todosObserver) }
    }

    @After
    fun teardown() {
        todosObservable.removeObserver(todosObserver)
    }

    @Test
    fun addTodoFromTextTest() {
        val id1 = todoRepository.addTodo("Hello")
        val id2 = todoRepository.addTodo("World")
        val id3 = todoRepository.addTodo("Boom!")

        assert(todos.size == 3)
        // This should be the exact order of the to-dos!
        assert(todos[0] == Todo(id1, false, "Hello"))
        assert(todos[1] == Todo(id2, false, "World"))
        assert(todos[2] == Todo(id3, false, "Boom!"))
    }

    @Test
    fun addTodoDirectlyTest() {
        val todoApple = Todo(id=5, true, "Apple")
        val todoOrange = Todo(id=2, false, "Orange")
        todoRepository.addTodo(todoApple)
        todoRepository.addTodo(todoOrange)

        // Recall to-dos are reported sorted by id.
        assert(todos == listOf(todoOrange, todoApple))
    }

    @Test
    fun deleteTodoByIdTest_WithExistingTodo() {
        val todo1 = Todo(id=4, false, "Hello world!")
        val todo2 = Todo(id=10, true, "Hello Earth!")
        val todo3 = Todo(id=15, true, "Hello Mars!")

        todoRepository.addTodo(todo1)
        todoRepository.addTodo(todo2)
        todoRepository.addTodo(todo3)
        assert(todos.size == 3)

        val deletedTodo = todoRepository.deleteTodo(10)
        assert(deletedTodo == todo2)
        assert(todos == listOf(todo1, todo3))
    }

    @Test
    fun deleteTodoByIdTest_WithNonExistingTodo() {
        todoRepository.addTodo(Todo(id=1, false, "Cool stuff!"))
        todoRepository.addTodo(Todo(id=2, true, "Nice."))
        val prevTodos = todos

        val deletedTodo = todoRepository.deleteTodo(10)
        assert(deletedTodo == null)
        assert(todos == prevTodos)
    }

    @Test
    fun deleteTodoDirectlyTest() {
        val todo1 = Todo(id=3, true, "CS349 rocks!")
        val todo2 = Todo(id=12, false, "CS241 rocks also!")
        val todo3 = Todo(id=13, false, "CS341 rocks, too, but it will be deleted...")

        todoRepository.addTodo(todo1)
        todoRepository.addTodo(todo2)
        todoRepository.addTodo(todo3)
        assert(todos.size == 3)

        todoRepository.deleteTodo(todo3)
        assert(todos == listOf(todo1, todo2))
    }

    @Test
    fun editTodoTextTest_withExistingTodo() {
        val todo1 = Todo(id=56, true, "My name is Emre.")
        val todo2 = Todo(id=23, false, "My name is Joe.")
        todoRepository.addTodo(todo1)
        todoRepository.addTodo(todo2)

        val prevText = todoRepository.editTodoText(56, "I am Emre.")
        assert(prevText == "My name is Emre.")
        // Also verifies other to-dos, if any, are unaffected.
        assert(todos == listOf(todo2, todo1.copy(text = "I am Emre.")))
    }

    @Test
    fun editTodoTextTest_withNonExistingTodo() {
        todoRepository.addTodo(Todo(id=2, false, "Hi!"))
        todoRepository.addTodo(Todo(id=3, true, "Howdy."))
        val prevTodos = todos

        val prevText = todoRepository.editTodoText(10, "Ignored!")
        assert(prevText == null)
        assert(todos == prevTodos)
    }

    @Test
    fun editTodoDone_withExistingTodo() {
        val todo1 = Todo(id=4, true, "Uncle")
        val todo2 = Todo(id=5, false, "Bob")
        todoRepository.addTodo(todo1)
        todoRepository.addTodo(todo2)

        val prevDone = todoRepository.editTodoDone(4, false)
        assert(prevDone == true)
        // Also verifies other to-dos, if any, are unaffected.
        assert(todos == listOf(todo1.copy(done = false), todo2))
    }

    @Test
    fun editTodoDone_withNonExistingTodo() {
        todoRepository.addTodo(Todo(id=44, false, "Trevor is a boy,"))
        todoRepository.addTodo(Todo(id=99, false, "Jane is a girl."))
        val prevTodos = todos

        val prevDone = todoRepository.editTodoDone(3, true)
        assert(prevDone == null)
        assert(todos == prevTodos)
    }
}