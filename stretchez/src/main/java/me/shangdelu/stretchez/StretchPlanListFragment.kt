package me.shangdelu.stretchez

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import java.util.*

class StretchPlanListFragment : Fragment() {

    /**
     * Required interface for hosting activities
     */
    interface Callbacks {
        fun onStretchPlanSelected(stretchPlanId: UUID, option: Int)
    }

    private var callbacks: Callbacks? = null

    private lateinit var stretchPlanRecyclerView: RecyclerView

    private lateinit var stretchPlanCoordinatorLayout: CoordinatorLayout

    private var adapter: StretchPlanAdapter? = StretchPlanAdapter(emptyList())

    private val stretchPlanListViewModel: StretchPlanListViewModel by lazy {
        ViewModelProviders.of(this)[StretchPlanListViewModel::class.java]
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stretch_plan_list, container, false)

        stretchPlanCoordinatorLayout = view.findViewById(R.id.stretch_plan_coordinator_layout)

        stretchPlanRecyclerView =
            view.findViewById(R.id.stretch_plan_recycler_view) as RecyclerView
        stretchPlanRecyclerView.layoutManager = LinearLayoutManager(context)

        stretchPlanRecyclerView.adapter = adapter

        //Swipe to delete existing stretch plans
        val swipeToDeleteCallBack = object : SwipeToDeleteCallBack(context) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val currentId = adapter?.stretchPlans?.get(position)
                currentId?.let {
                    // get current timestamp of the stretch plan
                    it.timestamp = System.currentTimeMillis();
                    // update the timestamp of current stretch plan
                    stretchPlanListViewModel.updateStretchPlan(it)
                }

                val deleteSnackbar = Snackbar.make(stretchPlanCoordinatorLayout,
                    R.string.delete_plan_snackbar, Snackbar.LENGTH_LONG)

                deleteSnackbar.setAction(R.string.undo_delete_snackbar, View.OnClickListener {
                    currentId?.let {
                        it.timestamp = null
                        stretchPlanListViewModel.updateStretchPlan(it)
                    }
                })

                deleteSnackbar.setActionTextColor(Color.WHITE)
                deleteSnackbar.show()
            }
        }
        
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallBack)
        itemTouchHelper.attachToRecyclerView(stretchPlanRecyclerView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stretchPlanListViewModel.stretchPlanListLiveData.observe(viewLifecycleOwner)
        { stretchPlans ->
            stretchPlans?.let {
                Log.i(TAG, "GOT ${stretchPlans.size} stretchPlans")
                updateUI(stretchPlans)
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_stretch_plan_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_stretchPlan -> {
                val stretchPlan = StretchPlan()
                stretchPlanListViewModel.addStretchPlan(stretchPlan)
                callbacks?.onStretchPlanSelected(stretchPlan.id, 1)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
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
            callbacks?.onStretchPlanSelected(stretchPlan.id, 0)
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