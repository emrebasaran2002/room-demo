package memre.roomdemo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import memre.roomdemo.data.ITodoRepository
import memre.roomdemo.data.Todo
import java.util.LinkedList

/**
 * ViewModel for MainActivity. Implements undo/redo using the reverse
 * undo technique taught in CS349.
 */
class MainActivityViewModel(private val todoRepository: ITodoRepository): ViewModel() {

    class Factory(private val repositoryImpl: ITodoRepository): ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainActivityViewModel(repositoryImpl) as T
        }
    }

    companion object {
        // Max Undo limit, to constrain the memory consumption of the app.
        private const val UNDO_LIMIT = 20

        private val TAG = MainActivityViewModel::class.simpleName
    }

    private interface ICommand {
        fun doCommand()
        fun undoCommand()
    }

    // -------------------- Undo/Redo management -------------------- //

    private val undoStack = LinkedList<ICommand>()
    private val _canUndo = MutableLiveData(false)

    private val redoStack = LinkedList<ICommand>()
    private val _canRedo = MutableLiveData(false)

    private fun execute(command: ICommand) {
        // Clear redo stack.
        redoStack.clear()
        _canRedo.value = false

        // Add to undo stack.
        undoStack.push(command)
        if (undoStack.size > UNDO_LIMIT) {
            undoStack.removeLast()
        }
        _canUndo.value = true

        // Execute command.
        command.doCommand()
    }

    val canUndo: LiveData<Boolean>
        get() = _canUndo

    fun undo() {
        if (undoStack.isEmpty()) {
            Log.e(TAG, "undo() called when not available!")
            return
        }

        // Remove command from undo stack.
        val command = undoStack.pop()
        _canUndo.value = undoStack.isNotEmpty()

        command.undoCommand()

        // Add command to redo stack.
        redoStack.push(command)
        _canRedo.value = true
    }

    val canRedo: LiveData<Boolean>
        get() = _canRedo

    fun redo() {
        if (redoStack.isEmpty()) {
            Log.e(TAG, "redo() called when not available!")
            return
        }

        // Remove command from redo stack.
        val command = redoStack.pop()
        _canRedo.value = redoStack.isNotEmpty()

        command.doCommand()

        // Add command to undo stack.
        undoStack.push(command)
        _canUndo.value = true
    }

    // -------------------- Data Operations -------------------- //

    val todos: LiveData<List<Todo>>
        get() = todoRepository.getTodos()

    fun addTodo(text: String) {
        val addCommand = object : ICommand {
            // Identifier for the added to-do, remembered for undo.
            private var todoId: Long? = null

            override fun doCommand() {
                todoId = todoRepository.addTodo(text)
            }

            override fun undoCommand() {
                todoId?.let { todoRepository.deleteTodo(it) }
            }
        }
        execute(addCommand)
    }

    fun deleteTodo(id: Long) {
        val deleteCommand = object : ICommand {
            // Remember deleted to-do for undo.
            private var deletedTodo: Todo? = null

            override fun doCommand() {
                deletedTodo = todoRepository.deleteTodo(id)
            }

            override fun undoCommand() {
                deletedTodo?.let { todoRepository.addTodo(it) }
            }
        }
        execute(deleteCommand)
    }

    fun editTodoText(id: Long, text: String) {
        val editTodoTextCommand = object : ICommand {
            // Remember previous text for undo.
            private var previousText: String? = null

            override fun doCommand() {
                previousText = todoRepository.editTodoText(id, text)
            }

            override fun undoCommand() {
                previousText?.let { todoRepository.editTodoText(id, it) }
            }
        }
        execute(editTodoTextCommand)
    }

    fun editTodoDone(id: Long, done: Boolean) {
        val editTodoDoneCommand = object : ICommand {
            // Remember previous done for undo.
            private var previousDone: Boolean? = null

            override fun doCommand() {
                previousDone = todoRepository.editTodoDone(id, done)
            }

            override fun undoCommand() {
                previousDone?.let { todoRepository.editTodoDone(id, it) }
            }
        }
        execute(editTodoDoneCommand)
    }
}