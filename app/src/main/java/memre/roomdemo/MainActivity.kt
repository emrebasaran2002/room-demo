package memre.roomdemo

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.floatingactionbutton.FloatingActionButton
import memre.roomdemo.data.Todo
import memre.roomdemo.data.TodoRepository

class MainActivity : FragmentActivity() {

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
            showTodoDialog()
        }

        // Prepare to-do RecyclerView.
        val todoRecyclerView: RecyclerView = findViewById(R.id.todoRecyclerView)
        val todoAdapter = TodoAdapter()
        todoRecyclerView.adapter = todoAdapter
        todoRecyclerView.layoutManager = LinearLayoutManager(this)
        ItemTouchHelper(TodoItemTouchCallback()).attachToRecyclerView(todoRecyclerView)
        mViewModel.todos.observe(this) { data ->
            todoAdapter.data = data
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

    // Suppressing lint because, while notifyDataSetChanged is not the most efficient,
    // we are following the MVC architecture taught in CS349.
    @SuppressLint("NotifyDataSetChanged")
    private inner class TodoAdapter : RecyclerView.Adapter<TodoAdapter.TodoHolder>() {

        private var _data: List<Todo> = listOf()
        var data: List<Todo>
            get() = _data
            set(newData) {
                _data = newData
                notifyDataSetChanged()
            }

        inner class TodoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            // Bound to-do.
            var todo: Todo? = null

            // References for views.
            val checkBox: MaterialCheckBox = itemView.findViewById(R.id.todoCheckbox)
            val editButton: ImageButton = itemView.findViewById(R.id.todoEditButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoHolder {
            val itemView = layoutInflater.inflate(R.layout.todo_view, parent, false)
            return TodoHolder(itemView)
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: TodoHolder, position: Int) {
            val todo = data[position]
            holder.todo = todo

            // Prepare to-do checkbox.
            val checkBox = holder.checkBox
            checkBox.text = todo.text
            // Hack: In order to avoid a circular call chain, we unset any previous
            // listener (recall view holders are reused), set the initial checked
            // status, and then set the new listener.
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = todo.done
            checkBox.setOnCheckedChangeListener { _, checked ->
                mViewModel.editTodoDone(todo.id, checked)
            }

            // Prepare edit to-do button.
            holder.editButton.setOnClickListener {
                showTodoDialog(todo)
            }
        }
    }

    private inner class TodoItemTouchCallback : ItemTouchHelper.SimpleCallback(
        0, // No support for dragging.
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT // Support for swiping.
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            source: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val todo = (viewHolder as TodoAdapter.TodoHolder).todo
                ?: throw IllegalStateException("Unbound view swiped.")
            mViewModel.deleteTodo(todo.id)
        }
    }

    @SuppressLint("InflateParams")
    private fun showTodoDialog(todo: Todo? = null) {
        // Inflate dialog's content view.
        val view = layoutInflater.inflate(R.layout.todo_dialog, null)
        val editText: EditText = view.findViewById(R.id.todoDialogEditText)
        todo?.let { editText.setText(it.text) }

        // Determine title for dialog.
        val titleStringRes =
            if (todo == null) {
                R.string.addNewTodoDialogTitle
            } else {
                R.string.editTodoDialogTitle
            }

        // Callback for user confirmation.
        fun confirm() {
            val newText = editText.text.toString().trim()

            // Validate against empty text.
            if (newText.isEmpty()) {
                Toast.makeText(this, R.string.todoDialogEmptyTextToast,
                    Toast.LENGTH_SHORT).show()
                return
            }

            if (todo == null) {
                // If new to-do dialog, add to-do.
                mViewModel.addTodo(newText)
            } else {
                // If edit to-do dialog, edit to-do if new text is different.
                if (todo.text == newText) {
                    Toast.makeText(this, R.string.editTodoDialogSameTextToast,
                        Toast.LENGTH_SHORT).show()
                } else {
                    mViewModel.editTodoText(todo.id, newText)
                }
            }
        }

        AlertDialog.Builder(this)
            .setTitle(titleStringRes)
            .setView(view)
            .setPositiveButton(R.string.confirmBtnText) { _, _ ->
                confirm()
            }
            .setNegativeButton(R.string.cancelBtnText) { _, _ ->
                // Nothing to do, dialog will simply close.
            }
            .show()
    }
}