package memre.roomdemo

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toolbar
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import memre.roomdemo.data.Todo
import memre.roomdemo.data.TodoRepository

class MainActivity : FragmentActivity() {

    companion object {
        private val TAG = MainActivity::class.simpleName
    }

    private lateinit var mViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set activity contents.
        setContentView(R.layout.main_activity)

        // Prepare activity action bar.
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setActionBar(toolbar)
        setTitle(R.string.mainActivityTitle)

        // Obtain view model reference.
        val viewModelFactory = MainActivityViewModel.Factory(TodoRepository(this))
        val viewModelProvider = ViewModelProvider(this, viewModelFactory)
        mViewModel = viewModelProvider[MainActivityViewModel::class.java]

        // Prepare add to-do button.
        val addTodoButton: FloatingActionButton = findViewById(R.id.addTodoFAB)
        addTodoButton.setOnClickListener {
            mViewModel.addTodo("Hello World!")
        }

        mViewModel.todos.observe(this) { todos: List<Todo> ->
            Log.v(TAG, "TODOS: $todos")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)

        // Get theme colors for enabled/disabled states.
        val enabledColor = getThemeColor(com.google.android.material.R.attr.colorOnPrimary)
        val disabledColor = getThemeColor(com.google.android.material.R.attr.colorControlHighlight)

        // Helper for resolving color from enabled/disabled state.
        fun resolveColor(enabled: Boolean): Int = if (enabled) enabledColor else disabledColor

        // Prepare undo and redo drawables.
        val undoIcon = AppCompatResources.getDrawable(this, R.drawable.material_undo_24)
            ?: throw Resources.NotFoundException("Could not get undo icon.")
        val redoIcon = AppCompatResources.getDrawable(this, R.drawable.material_redo_24)
            ?: throw Resources.NotFoundException("Could not get redo icon.")

        // Prepare undo button.
        val undoBtn = menu.add(R.string.undoMenuItemText)
        undoBtn.setIcon(undoIcon)
        undoBtn.setOnMenuItemClickListener {
            mViewModel.undo()
            true
        }
        mViewModel.canUndo.observe(this) { canUndo ->
            undoBtn.isEnabled = canUndo
            undoIcon.setTint(resolveColor(canUndo))
        }
        undoBtn.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        // Prepare redo button.
        val redoBtn = menu.add(R.string.redoMenuItemText)
        redoBtn.setIcon(redoIcon)
        redoBtn.setOnMenuItemClickListener {
            mViewModel.redo()
            true
        }
        mViewModel.canRedo.observe(this) { canRedo ->
            redoBtn.isEnabled = canRedo
            redoIcon.setTint(resolveColor(canRedo))
        }
        redoBtn.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        return true
    }
}