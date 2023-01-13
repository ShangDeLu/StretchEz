package me.shangdelu.stretchez

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.os.Build.VERSION_CODES.S
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import me.shangdelu.stretchez.database.StretchExercise

class StretchExerciseListFragment : Fragment() {

    private var callbacks: StretchPlanListFragment.Callbacks? = null

    private lateinit var stretchExerciseRecyclerView: RecyclerView

    private lateinit var stretchExerciseCoordinatorLayout: CoordinatorLayout

    //Initialize the adapter with an empty list since the fragment have to wait for results from
    //the database before it can populate the recycler view.
    private var exerciseAdapter: StretchExerciseAdapter? = StretchExerciseAdapter(emptyList())

    private val stretchExerciseViewModel: StretchExerciseListViewModel by lazy {
        ViewModelProvider(this)[StretchExerciseListViewModel::class.java]
    }

    //Activity is a subclass of Context, so the Context object passed to onAttach is the activity
    //instance hosting the fragment.
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as StretchPlanListFragment.Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //tell FragmentManager that this fragment needs to receive menu callbacks.
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stretch_exercise_list, container, false)

        stretchExerciseRecyclerView = view.findViewById(R.id.stretch_exercise_recycler_view)
        //RecyclerView requires a layoutManager to work, otherwise it will crash
        //LinearLayoutManager will position every item in a list vertically, and defines how scrolling work
        stretchExerciseRecyclerView.layoutManager = LinearLayoutManager(context)
        stretchExerciseRecyclerView.adapter = exerciseAdapter

        stretchExerciseCoordinatorLayout = view.findViewById(R.id.stretch_exercise_coordinator_layout)

        //Swipe to delete existing stretch exercises
        val swipeToDeleteCallBack = object : SwipeToDeleteCallBack(context) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val currentId = exerciseAdapter?.stretchExercises?.get(position)
                stretchExerciseViewModel.deleteExercise(currentId!!)

                val deleteSnackbar = Snackbar.make(stretchExerciseCoordinatorLayout,
                    R.string.delete_exercise_snackbar, Snackbar.LENGTH_LONG)

                //TODO: Implement the UNDO part for swipe to delete


                deleteSnackbar.setActionTextColor(Color.WHITE)
                deleteSnackbar.show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallBack)
        itemTouchHelper.attachToRecyclerView(stretchExerciseRecyclerView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Used to register an observer on the LiveData instance and tie the life of the observation
        //to the life of another component, such as an activity or fragment.
        stretchExerciseViewModel.stretchExerciseLiveData.observe(viewLifecycleOwner)
        //Observer implementation, responsible for reacting to new data from the LiveData.
        //This code block executed whenever the LiveData's list gets updated.
        { stretchExercises ->
            stretchExercises?.let {
                Log.i(TAG, "Got stretchExercises ${stretchExercises.size}")
                updateUI(stretchExercises)
            }
        }
    }

    //Set the variable to null because afterward you cannot access the activity or
    //count on the activity continuing to exist.
    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    //Populates the Menu instance with the items defined in file.
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_stretch_exercise_list, menu)
    }

    //Respond to MenuItem selection by creating a new exercise, saving it to database, and then
    //notifying the parent activity that the new exercise has been selected.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_stretch_exercise -> {
                val exercise = StretchExercise()
                stretchExerciseViewModel.addStretchExercise(exercise)
                //argumentOption is 1 as we are creating new exercise
                callbacks?.onExerciseSelected(exercise.exerciseID, 1)
                //return true once the MenuItem is handled, indicating no further processing is necessary.
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    //Set up StretchExerciseListFragment's UI
    private fun updateUI(stretchExercises: List<StretchExercise>) {

        exerciseAdapter = StretchExerciseAdapter(stretchExercises)
        stretchExerciseRecyclerView.adapter = exerciseAdapter
    }

    //RecyclerView never creates Views by themselves.
    //It always creates ViewHolders, which bring their itemViews along.
    //We can made the ViewHolder as the OnClickListener for its view.
    private inner class StretchExerciseHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var stretchExercise: StretchExercise
        //find the title textview
        val titleTextView: TextView = itemView.findViewById(R.id.stretch_exercise_title)

        init {
            itemView.setOnClickListener(this)
        }

        //Code the binding in the holder, the adapter should know as little as possible about
        //the inner workings and details of the view holder.
        fun bind(stretchExercise: StretchExercise) {
            this.stretchExercise = stretchExercise
            titleTextView.text = this.stretchExercise.exerciseName

            if (stretchExercise.exerciseName.isEmpty()) { //delete any stretchExercise without a name
                stretchExerciseViewModel.deleteExercise(stretchExercise)
            }
        }

        override fun onClick(v: View) {
            //call onExerciseSelected from callbacks interface when individual exercise is clicked.
            //argument option is 1 as we are clicking on existing exercise
            callbacks?.onExerciseSelected(stretchExercise.exerciseID, 0)
        }

    }

    //RecyclerView does not create ViewHolder directly, instead it asks an adapter.
    //An adapter is a controller object between the RecyclerView and the data set that RecyclerView should display.
    private inner class StretchExerciseAdapter(var stretchExercises: List<StretchExercise>)
        : RecyclerView.Adapter<StretchExerciseHolder>() {
        //Responsible for creating a view to display, wrapping the view in a view holder and returning the result
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StretchExerciseHolder {
            val view = layoutInflater.inflate(R.layout.list_item_stretch_exercise, parent, false)
            return StretchExerciseHolder(view)
        }

        //Gives the number of items in the list of stretchExercises.
        override fun getItemCount() = stretchExercises.size

        //Responsible for populating a given holder with the stretchExercise from the given position.
        override fun onBindViewHolder(holder: StretchExerciseHolder, position: Int) {
            val stretchExercise = stretchExercises[position]

            holder.bind(stretchExercise)
        }
    }

    companion object {
        fun newInstance(): StretchExerciseListFragment {
            return StretchExerciseListFragment()
        }
    }
}