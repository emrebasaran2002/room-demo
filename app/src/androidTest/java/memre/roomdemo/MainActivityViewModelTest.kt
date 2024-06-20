package memre.roomdemo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import memre.roomdemo.data.Todo
import memre.roomdemo.data.TodoRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.ranges.downTo

@RunWith(AndroidJUnit4::class)
class MainActivityViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: MainActivityViewModel

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        viewModel = MainActivityViewModel(TodoRepository.getTestInstance(context))
    }

    // Helper for getting the current value of a LiveData observable. Unfortunately,
    // using getValue() does not guarantee receiving the latest value, hence the
    // need for this workaround.
    private fun <T : Any> LiveData<T>.current(): T {
        var value: T? = null

        // CountDownLatch to block calling thread until observer has set the value.
        val latch = CountDownLatch(1)
        val observer = Observer<T> { observedValue ->
            if (value == null) {
                value = observedValue
                latch.countDown()
            }
        }
        observeForever(observer)

        try {
            // Await with an arbitrary timeout of 1 second. If the timeout expires,
            // the value will not have been set after this returns, so the following
            // null check will throw a TimeoutException.
            latch.await(1, TimeUnit.SECONDS)
            return value ?: throw TimeoutException()
        } finally {
            removeObserver(observer)
        }
    }

    private val todos: List<Todo> get() = viewModel.todos.current()
    private val canUndo: Boolean get() = viewModel.canUndo.current()
    private val canRedo: Boolean get() = viewModel.canRedo.current()

    private fun assertTodos(vararg expectedTodos: Pair<String, Boolean>) {
        assert(todos.size == expectedTodos.size)
        for (i in todos.indices) {
            assert(todos[i].text == expectedTodos[i].first)
            assert(todos[i].done == expectedTodos[i].second)
        }
    }

    @Test
    fun basicFunctionsTest() {
        // Add some to-dos.
        viewModel.addTodo("This app rocks, dude!")
        viewModel.addTodo("Really, amazing work! :)")

        assertTodos(
            "This app rocks, dude!" to false,
            "Really, amazing work! :)" to false
        )

        // Check first to-do.
        viewModel.editTodoDone(todos[0].id, true)

        assertTodos(
            "This app rocks, dude!" to true,
            "Really, amazing work! :)" to false
        )

        // Edit second to-do.
        viewModel.editTodoText(todos[1].id, "Fantastic work, my friend.")

        assertTodos(
            "This app rocks, dude!" to true,
            "Fantastic work, my friend." to false
        )

        // Delete first to-do.
        viewModel.deleteTodo(todos[0].id)

        assertTodos(
            "Fantastic work, my friend." to false
        )

        // Check first to-do.
        viewModel.editTodoDone(todos[0].id, true)

        assertTodos(
            "Fantastic work, my friend." to true
        )

        // Add new to-do.
        viewModel.addTodo("I am programmer!!!")

        assertTodos(
            "Fantastic work, my friend." to true,
            "I am programmer!!!" to false
        )
    }

    @Test
    fun addUndoRedoTest() {
        viewModel.addTodo("Hello")
        viewModel.addTodo("World")

        assertTodos(
            "Hello" to false,
            "World" to false
        )

        viewModel.undo()

        assertTodos(
            "Hello" to false
        )

        viewModel.redo()

        assertTodos(
            "Hello" to false,
            "World" to false
        )
    }

    @Test
    fun deleteUndoRedoTest() {
        viewModel.addTodo("First")
        viewModel.addTodo("Second")
        viewModel.addTodo("Third")

        assertTodos(
            "First" to false,
            "Second" to false,
            "Third" to false
        )

        // Delete from middle to verify that undoing restores the correct
        // to-do position in the list.
        viewModel.deleteTodo(todos[1].id)

        assertTodos(
            "First" to false,
            "Third" to false
        )

        viewModel.undo()

        assertTodos(
            "First" to false,
            "Second" to false,
            "Third" to false
        )

        viewModel.redo()

        assertTodos(
            "First" to false,
            "Third" to false
        )
    }

    @Test
    fun editTodoTextUndoRedoTest() {
        viewModel.addTodo("Apple")
        viewModel.addTodo("Orange")

        assertTodos(
            "Apple" to false,
            "Orange" to false
        )

        viewModel.editTodoText(todos[0].id, "Banana")

        assertTodos(
            "Banana" to false,
            "Orange" to false
        )

        viewModel.undo()

        assertTodos(
            "Apple" to false,
            "Orange" to false
        )

        viewModel.redo()

        assertTodos(
            "Banana" to false,
            "Orange" to false
        )
    }

    @Test
    fun editTodoDoneUndoRedoTest() {
        viewModel.addTodo("Broccoli")
        viewModel.addTodo("Carrot")

        assertTodos(
            "Broccoli" to false,
            "Carrot" to false
        )

        viewModel.editTodoDone(todos[0].id, true)

        assertTodos(
            "Broccoli" to true,
            "Carrot" to false
        )

        viewModel.undo()

        assertTodos(
            "Broccoli" to false,
            "Carrot" to false
        )

        viewModel.redo()

        assertTodos(
            "Broccoli" to true,
            "Carrot" to false
        )
    }

    @Test
    fun actionFollowingRedoTest() {
        assert(!canUndo)
        assert(!canRedo)

        viewModel.addTodo("Emre")
        viewModel.addTodo("Josh")
        viewModel.addTodo("Aynur")
        viewModel.editTodoText(todos[1].id, "Duru")
        viewModel.editTodoDone(todos[0].id, true)
        viewModel.addTodo("Mustafa")

        assertTodos(
            "Emre" to true,
            "Duru" to false,
            "Aynur" to false,
            "Mustafa" to false
        )
        assert(canUndo)
        assert(!canRedo)

        viewModel.undo()
        viewModel.undo()

        assertTodos(
            "Emre" to false,
            "Duru" to false,
            "Aynur" to false
        )
        assert(canUndo)
        assert(canRedo)

        viewModel.editTodoText(todos[2].id, "Mom")

        assertTodos(
            "Emre" to false,
            "Duru" to false,
            "Mom" to false
        )
        assert(canUndo)
        assert(!canRedo)

        viewModel.undo()

        assertTodos(
            "Emre" to false,
            "Duru" to false,
            "Aynur" to false
        )

        viewModel.undo()

        assertTodos(
            "Emre" to false,
            "Josh" to false,
            "Aynur" to false
        )

        viewModel.undo()

        assertTodos(
            "Emre" to false,
            "Josh" to false
        )

        viewModel.undo()

        assertTodos(
            "Emre" to false
        )

        viewModel.undo()

        assertTodos(/* Empty */)
        assert(!canUndo)
        assert(canRedo)
    }

    @Test
    fun undoLimitTest() {
        for (i in 1..15) {
            viewModel.addTodo("TODO $i")
            viewModel.deleteTodo(todos[0].id)
        }

        assertTodos(/* Empty */)
        assert(canUndo)
        assert(!canRedo)

        for (i in 15 downTo 6) {
            assert(canUndo)
            viewModel.undo()
            assertTodos("TODO $i" to false)
            viewModel.undo()
            assertTodos(/* Empty */)
        }

        // This will have exhausted the undo limit.
        assert(!canUndo)

        for (i in 6..15) {
            assert(canRedo)
            viewModel.redo()
            assertTodos("TODO $i" to false)
            viewModel.redo()
            assertTodos(/* Empty */)
        }
    }
}