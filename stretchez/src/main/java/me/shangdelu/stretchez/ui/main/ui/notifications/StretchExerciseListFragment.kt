package me.shangdelu.stretchez.ui.main.ui.notifications

import android.app.ProgressDialog.show
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import me.shangdelu.stretchez.R
import me.shangdelu.stretchez.StretchExerciseFragment
import me.shangdelu.stretchez.StretchExerciseListViewModel
import me.shangdelu.stretchez.SwipeToDeleteCallBack
import me.shangdelu.stretchez.database.StretchExercise
import me.shangdelu.stretchez.ui.main.BottomNavigationMainActivity
import java.util.*

/**
 * Required interface for hosting activities
 */
//Use an ExerciseCallback interface to delegate on-click events from fragments back to its hosting activity.
interface ExerciseCallbacks {
    //called when click on StretchingExercise
    fun onExerciseSelected(exerciseID: Int?, option: Int)
}


class StretchExerciseListFragment : Fragment(), ExerciseCallbacks {

    private lateinit var stretchExerciseRecyclerView: RecyclerView

    private lateinit var stretchExerciseCoordinatorLayout: CoordinatorLayout

    private lateinit var bottomNavView: BottomNavigationView

    //Initialize the adapter with an empty list since the fragment have to wait for results from
    //the database before it can populate the recycler view.
    private var exerciseAdapter: StretchExerciseAdapter? = StretchExerciseAdapter(emptyList())

    private val stretchExerciseViewModel: StretchExerciseListViewModel by lazy {
        ViewModelProvider(this)[StretchExerciseListViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stretch_exercise_list, container, false)

        //use requireActivity to find the bottom navigation view that is bind to Activity
        bottomNavView = requireActivity().findViewById(R.id.nav_view)

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
                //check the isTemplate of the Stretch Exercise
                if (currentId?.isTemplate == 1) { //if isTemplate is 1, current exercise is a template.
                    //template exercise should be non-deletable, move current exercise back to the initial position
                    exerciseAdapter?.notifyItemChanged(viewHolder.bindingAdapterPosition)

                    //Use a snack bar to notify user template is non-deletable
                    val templateMessage = Snackbar.make(stretchExerciseCoordinatorLayout,
                        R.string.template_exercise_snackbar, Snackbar.LENGTH_LONG)
                    //use bottomNavView to setAnchorView for the snack bar to prevent snack bar got blocked by bottom navigation bar.
                    templateMessage.setActionTextColor(Color.WHITE).setAnchorView(bottomNavView).show()
                } else { //if isTemplate is 0, current exercise is not a template.
                    //exercise is deletable, delete the exercise
                    stretchExerciseViewModel.deleteExercise(currentId!!)
                    //notify user the delete is success.
                    val deleteMessage = Snackbar.make(stretchExerciseCoordinatorLayout,
                        R.string.delete_exercise_snackbar, Snackbar.LENGTH_LONG)
                    //use bottomNavView to setAnchorView for the snack bar to prevent snack bar got blocked by bottom navigation bar.
                    deleteMessage.setActionTextColor(Color.WHITE).setAnchorView(bottomNavView).show()
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallBack)
        itemTouchHelper.attachToRecyclerView(stretchExerciseRecyclerView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()

        //Used to register an observer on the LiveData instance and tie the life of the observation
        //to the life of another component, such as an activity or fragment.
        stretchExerciseViewModel.stretchExerciseLiveData.observe(viewLifecycleOwner)
        //Observer implementation, responsible for reacting to new data from the LiveData.
        //This code block executed whenever the LiveData's list gets updated.
        { stretchExercises ->
            stretchExercises?.let {
                //Log.i(TAG, "Got stretchExercises ${stretchExercises.size}")
                updateUI(stretchExercises)
            }
        }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            //Populates the Menu instance with the items defined in file.
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_stretch_exercise_list, menu)
            }
            //Respond to MenuItem selection by creating a new exercise, saving it to database, and then
            //notifying the parent activity that the new exercise has been selected.
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.new_stretch_exercise -> {
                        val exercise = StretchExercise()
                        stretchExerciseViewModel.addStretchExercise(exercise)
                        //argumentOption is 1 as we are creating new exercise
                        onExerciseSelected(exercise.exerciseID, 1)
                        //return true once the MenuItem is handled, indicating no further processing is necessary.
                        true
                    }
                    else -> return onMenuItemSelected(menuItem)
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onExerciseSelected(exerciseID: Int?, option: Int) {
        val arguments = StretchExerciseFragment.newInstance(exerciseID, option)
        findNavController().navigate(R.id.action_navigation_exercise_list_to_navigation_exercise, arguments)
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
            //call onExerciseSelected from ExerciseCallbacks interface when individual exercise is clicked.
            //argument option is 0 as we are clicking on existing exercise
            onExerciseSelected(stretchExercise.exerciseID, 0)
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