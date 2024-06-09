package memre.roomdemo

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import memre.roomdemo.data.Todo
import memre.roomdemo.data.TodoRepository

class MainActivity : FragmentActivity() {

    companion object {
        private val TAG = MainActivity::class.simpleName
    }

    private lateinit var mAddTodoButton : FloatingActionButton
    private lateinit var mTodoRecyclerView: RecyclerView

    private lateinit var mViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setTitle(R.string.mainActivityTitle)

        val viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        val viewModelProvider = ViewModelProvider(this, viewModelFactory)
        mViewModel = viewModelProvider[MainActivityViewModel::class.java]

        mAddTodoButton = findViewById(R.id.addTodoFAB)
        mTodoRecyclerView = findViewById(R.id.todoRecyclerView)

        mAddTodoButton.setOnClickListener {
            mViewModel.addTodo("Hello World!")
        }

        mViewModel.todos.observe(this) { todos: List<Todo> ->
            Log.v(TAG, "TODOS: $todos")
        }
    }
}