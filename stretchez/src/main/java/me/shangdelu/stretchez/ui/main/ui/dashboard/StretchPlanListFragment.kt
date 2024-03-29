package me.shangdelu.stretchez.ui.main.ui.dashboard

import android.graphics.Color
import android.os.Bundle
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
import me.shangdelu.stretchez.StretchPlanFragment
import me.shangdelu.stretchez.StretchPlanListViewModel
import me.shangdelu.stretchez.SwipeToDeleteCallBack
import me.shangdelu.stretchez.database.StretchPlan
import java.util.*

/**
 * Required interface for hosting activities
 */
//Use a StretchPlanCallback interface to delegate on-click events from fragments back to its hosting activity.
interface StretchPlanCallbacks {
    //called when click on StretchPlan
    fun onStretchPlanSelected(stretchPlanId: UUID, option: Int)
}

class StretchPlanListFragment : Fragment(), StretchPlanCallbacks {

    private lateinit var stretchPlanRecyclerView: RecyclerView

    private lateinit var stretchPlanCoordinatorLayout: CoordinatorLayout

    private lateinit var bottomNavView: BottomNavigationView

    private var adapter: StretchPlanAdapter? = StretchPlanAdapter(emptyList())

    private val stretchPlanListViewModel: StretchPlanListViewModel by lazy {
        ViewModelProvider(this)[StretchPlanListViewModel::class.java]
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stretch_plan_list, container, false)

        //use requireActivity to find the bottom navigation view that is bind to Activity
        bottomNavView = requireActivity().findViewById(R.id.nav_view)

        stretchPlanCoordinatorLayout = view.findViewById(R.id.stretch_plan_coordinator_layout)

        stretchPlanRecyclerView = view.findViewById(R.id.stretch_plan_recycler_view) as RecyclerView
        stretchPlanRecyclerView.layoutManager = LinearLayoutManager(context)

        stretchPlanRecyclerView.adapter = adapter

        //Swipe to delete existing stretch plans
        val swipeToDeleteCallBack = object : SwipeToDeleteCallBack(context) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val currentId = adapter?.stretchPlans?.get(position)
                currentId?.let { currentPlan ->
                    //get current timestamp of the stretch plan
                    //it.timestamp = System.currentTimeMillis();

                    //delete the stretchExercise contain in current stretchPlan
                    stretchPlanListViewModel.getExercisesOfPlan(currentPlan.id).observe(viewLifecycleOwner) { exerciseList ->
                        exerciseList.forEach {
                            stretchPlanListViewModel.deleteStretchExercise(it)
                        }
                    }
                    //delete the current stretch plan
                    stretchPlanListViewModel.deleteStretchPlan(currentPlan)
                }
                //notify the user that delete is complete
                val deleteMessage = Snackbar.make(stretchPlanCoordinatorLayout,
                    R.string.delete_plan_snackbar, Snackbar.LENGTH_LONG)
                //use bottomNavView to setAnchorView for the snack bar to prevent snack bar got blocked by bottom navigation bar.
                deleteMessage.setActionTextColor(Color.WHITE).setAnchorView(bottomNavView).show()

//                deleteMessage.setAction(R.string.undo_delete_snackbar) {
//                    currentId?.let {
//                        it.timestamp = null
//                        stretchPlanListViewModel.updateStretchPlan(it)
//                    }
//                }
            }
        }
        
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallBack)
        itemTouchHelper.attachToRecyclerView(stretchPlanRecyclerView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()

        stretchPlanListViewModel.stretchPlanListLiveData.observe(viewLifecycleOwner)
        { stretchPlans ->
            stretchPlans?.let {
                //Log.i(TAG, "GOT ${stretchPlans.size} stretchPlans")
                updateUI(stretchPlans)
            }
        }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object: MenuProvider {
            //Populates the Menu instance with the items defined in file.
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_stretch_plan_list, menu)
            }

            //Respond to MenuItem selection by creating a new stretchPlan, saving it to database,
            //and then notifying the parent activity that the new stretchPlan has been selected.
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.new_stretchPlan -> {
                        val stretchPlan = StretchPlan()
                        stretchPlanListViewModel.addStretchPlan(stretchPlan)
                        //argumentOption is 1 as we are creating new stretchPlan
                        onStretchPlanSelected(stretchPlan.id, 1)
                        //return true once the MenuItem is handled, indicating no further processing is necessary.
                        true
                    }
                    //return false when the MenuItem cannot be handled by the MenuProvider.
                    else -> return false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onStretchPlanSelected(stretchPlanId: UUID, option: Int) {
        val arguments = StretchPlanFragment.newInstance(stretchPlanId, option)
        findNavController().navigate(R.id.action_navigation_stretch_plan_list_to_navigation_stretch_plan, arguments)
    }

    private fun updateUI(stretchPlans: List<StretchPlan>) {
        adapter = StretchPlanAdapter(stretchPlans)
        stretchPlanRecyclerView.adapter = adapter
    }

    private inner class StretchPlanHolder(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var stretchPlan: StretchPlan

        private val titleTextView: TextView = itemView.findViewById(R.id.stretch_plan_title)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind (stretchPlan: StretchPlan) {
            this.stretchPlan = stretchPlan
            titleTextView.text = this.stretchPlan.title
            if (stretchPlan.title.isEmpty()) {
                stretchPlanListViewModel.deleteStretchPlan(stretchPlan)
            }

            // if user deleted a stretch plan last more than 30 days, then delete it completely
            if (stretchPlan.timestamp != null && stretchPlan.timestamp!! >= 2592000000) {
                stretchPlanListViewModel.deleteStretchPlan(stretchPlan)
            }
        }

        override fun onClick(v: View?) {
            onStretchPlanSelected(stretchPlan.id, 0)
        }
    }

    private inner class StretchPlanAdapter(var stretchPlans: List<StretchPlan>)
        : RecyclerView.Adapter<StretchPlanHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StretchPlanHolder {
            val view = layoutInflater.inflate(R.layout.list_item_stretch_plan, parent, false)
            return StretchPlanHolder(view)
        }

        override fun onBindViewHolder(holder: StretchPlanHolder, position: Int) {
            val stretchPlan = stretchPlans[position]
            holder.bind(stretchPlan)
        }

        override fun getItemCount() = stretchPlans.size
    }

    companion object {
        fun newInstance(): StretchPlanListFragment {
            return StretchPlanListFragment()
        }
    }
}