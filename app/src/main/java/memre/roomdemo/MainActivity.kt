package memre.roomdemo

import android.os.Bundle
import android.util.Log
import android.widget.Toolbar
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
}