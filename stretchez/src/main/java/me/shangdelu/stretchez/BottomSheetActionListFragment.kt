package me.shangdelu.stretchez

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import me.shangdelu.stretchez.database.StretchExercise
import me.shangdelu.stretchez.database.StretchPlan
import java.util.*

private const val ARG_PLAN_ID = "plan_id"

class BottomSheetActionListFragment : BottomSheetDialogFragment() {

    private lateinit var bottomSheetActionRecyclerView: RecyclerView
    private lateinit var stretchPlan: StretchPlan
    private lateinit var bottomSheetActionRepository: StretchPlanRepository
    //Initialize the adapter with an empty list since the fragment have to wait for results from
    //the database before it can populate the recycler view.
    private var adapter: ActionAdapter? = ActionAdapter(emptyList())
    private var argumentOption: Int = 0
    private var stretchPlanID: UUID? = null
    private var lastIndex: Int = 0 //The lastIndex of current list of selected actions.
    //Associate the Fragment with the ViewModel
    private val bottomSheetActionListViewModel: BottomSheetActionListViewModel by lazy {
        ViewModelProvider(this)[BottomSheetActionListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stretchPlan = StretchPlan()
        //Retrieve stretchPlanID from the fragment arguments
        stretchPlanID = arguments?.getSerializable(ARG_PLAN_ID) as UUID?
        bottomSheetActionRepository = StretchPlanRepository.get()
        argumentOption = arguments?.getInt("Option") ?: 0
        lastIndex = arguments?.getInt("ListLength") ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bottom_sheet_action_list, container, false)

        bottomSheetActionRecyclerView = view.findViewById(R.id.bottom_sheet_action_recycler_view) as RecyclerView
        //RecyclerView requires a layoutManager to work, otherwise it will crash
        //LinearLayoutManager will position every item in a list vertically, and defines how scrolling work
        bottomSheetActionRecyclerView.layoutManager = LinearLayoutManager(context)
        bottomSheetActionRecyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Used to register an observer on the LiveData instance and tie the life of the observation
        //to the life of another component, such as an activity or fragment.
        bottomSheetActionListViewModel.bottomSheetActionLiveData.observe(viewLifecycleOwner)
        //Observer implementation, responsible for reacting to new data from the LiveData.
        //This code block executed whenever the LiveData's list gets updated.
        { actions ->
            actions?.let {
                Log.i(TAG, "Got actions ${actions.size}")
                updateUI(actions)
            }
        }
    }


    private fun updateUI(actions: List<StretchExercise>) {
        adapter = ActionAdapter(actions)
        bottomSheetActionRecyclerView.adapter = adapter
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
            titleTextView.setOnClickListener {
                //add a new data to DB that have everything same with the selected template,
                //but with planID and orderNumber.
                bottomSheetActionRepository.addExerciseToPlan(
                    StretchExercise(
                        exerciseName = this.action.exerciseName,
                        exerciseDescription = this.action.exerciseDescription,
                        exerciseDuration = this.action.exerciseDuration,
                        exerciseLink = this.action.exerciseLink,
                        orderNumber = lastIndex + 1,
                        planID = stretchPlanID
                    )
                )
                //Dismiss the bottom sheet fragment after user select an action
                dismiss()
            }
        }
    }

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
        private const val TAG = "ModalBottomSheet"
        fun newInstance(stretchPlanID: UUID?, option: Int, listLength: Int): Bundle {
            //This Bundle contains key-value pairs that work just like the intent extras of an Activity.
            //Each pair is known as an argument.
            return Bundle().apply {
                putSerializable(ARG_PLAN_ID, stretchPlanID)
                putInt("Option", option)
                putInt("ListLength", listLength)
            }
        }
    }
}