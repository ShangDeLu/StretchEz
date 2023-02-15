package me.shangdelu.stretchez

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import me.shangdelu.stretchez.database.StretchExercise
import me.shangdelu.stretchez.database.StretchPlan
import java.util.*

private const val TAG = "SelectActionFragment"
private const val ARG_PLAN_ID = "plan_id"

class SelectActionFragment : Fragment() {

    private lateinit var actionRecyclerView: RecyclerView
    private lateinit var stretchPlan: StretchPlan
    private lateinit var selectActionRepository: StretchPlanRepository
    //Initialize the adapter with an empty list since the fragment have to wait for results from
    //the database before it can populate the recycler view.
    private var adapter: ActionAdapter? = ActionAdapter(emptyList())
    private var argumentOption: Int = 0
    private var stretchPlanID: UUID? = null
    //Associate the Fragment with the ViewModel
    private val selectActionListViewModel: SelectActionListViewModel by lazy {
        ViewModelProvider(this)[SelectActionListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stretchPlan = StretchPlan()
        //Retrieve stretchPlanID from the fragment arguments
        stretchPlanID = arguments?.getSerializable(ARG_PLAN_ID) as UUID?
        selectActionRepository = StretchPlanRepository.get()
        argumentOption = arguments?.getInt("Option") ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_action_list, container, false)

        actionRecyclerView = view.findViewById(R.id.action_recycler_view) as RecyclerView
        //RecyclerView requires a layoutManager to work, otherwise it will crash
        //LinearLayoutManager will position every item in a list vertically, and defines how scrolling work
        actionRecyclerView.layoutManager = LinearLayoutManager(context)
        actionRecyclerView.adapter = adapter

        //Swipe to delete/deselect selected actions
        val swipeToDeleteCallBack = object : SwipeToDeleteCallBack(context) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val currentId = adapter?.actions?.get(position)
                selectActionListViewModel.deleteExercise(currentId!!)

                val deleteSnackbar = Snackbar.make(actionRecyclerView,
                    R.string.remove_action_snackbar, Snackbar.LENGTH_LONG)

                deleteSnackbar.setActionTextColor(Color.WHITE)
                deleteSnackbar.show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallBack)
        itemTouchHelper.attachToRecyclerView(actionRecyclerView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()

        //Used to register an observer on the LiveData instance and tie the life of the observation
        //to the life of another component, such as an activity or fragment.
        selectActionListViewModel.getExercisesOfPlan(stretchPlanID).observe(viewLifecycleOwner)
        //Observer implementation, responsible for reacting to new data from the LiveData.
        //This code block executed whenever the LiveData's list gets updated.
        { actions ->
            actions?.let {
                Log.i(TAG, "Got actions ${actions.size}")
                updateUI(actions)
            }
        }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object: MenuProvider {
            //Populates the Menu instance with the item defines in file.
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_action_list, menu)
            }

            //Respond to MenuItem selection by open the bottom sheet fragment to select action.
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.new_action -> {
                        showBottomSheet()
                        true
                    }
                    //TODO: Fix the app crashing from returning
                    else -> return onMenuItemSelected(menuItem)
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showBottomSheet() {
        //pass a bundle with stretchPlanID, argumentOption, and counts of current list of
        //selected actions
        val bottomSheet = adapter?.let {
            BottomSheetActionListFragment.newInstance(stretchPlanID, argumentOption, it.itemCount)
        }
        bottomSheet?.show(this.parentFragmentManager, "ModalBottomSheet")
    }


    private fun updateUI(actions: List<StretchExercise>) {
        adapter = ActionAdapter(actions)
        actionRecyclerView.adapter = adapter
    }

    //RecyclerView never creates Views by themselves.
    //It always creates ViewHolders, which bring their itemViews along.
    //We can made the ViewHolder as the OnClickListener for its view.
    private inner class ActionHolder(view: View) : RecyclerView.ViewHolder(view) {
        private lateinit var action: StretchExercise

        private val titleTextView: TextView = itemView.findViewById(R.id.action_title)

        //Code the binding in the holder, the adapter should know as little as possible about
        //the inner workings and details of the view holder.
        fun bind(action: StretchExercise) {
            this.action = action
            titleTextView.text = this.action.exerciseName

            //TODO 1: Add a new BottomSheet Fragment
            //TODO 2: Implement an Add Button on Top right of SelectActionFragment
            //When Click the Add Button, BottomSheetFragment should pop up,
            //and present all template actions for user to choose.
            //User should only be able to choose one action per time
            //TODO 3: Add new data to DB when exercise been selected
            //TODO 4: Delete data from DB when exercise are no longer selected
            //Use Swipe to Delete Feature
            //TODO 5: SelectActionFragment should be a recycler view that shows all selected actions in order
            //TODO 6: SelectActionFragment should determine if current plan is new or existing plan
            //TODO 7: For existing plan, load exercises that are pre-selected and in right order

        }
    }

    private inner class ActionAdapter(var actions: List<StretchExercise>)
        : RecyclerView.Adapter<ActionHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ActionHolder {
            val view = layoutInflater.inflate(R.layout.list_item_action, parent, false)
            return ActionHolder(view)
        }

        override fun getItemCount() = actions.size

        override fun onBindViewHolder(holder: ActionHolder, position: Int) {
            val action = actions[position]
            holder.bind(action)

        }

    }

    companion object {
        fun newInstance(stretchPlanID: UUID?, option: Int): Bundle {
            //This Bundle contains key-value pairs that work just like the intent extras of an Activity.
            //Each pair is known as an argument.
            return Bundle().apply {
                putSerializable(ARG_PLAN_ID, stretchPlanID)
                putInt("Option", option)
            }
        }
    }
}