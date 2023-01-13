package me.shangdelu.stretchez

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SelectActionFragment : Fragment() {

    private lateinit var actionRecyclerView: RecyclerView
    private var adapter: ActionAdapter? = null

    private val selectActionListViewModel: SelectActionListViewModel by lazy {
        ViewModelProvider(this)[SelectActionListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_action_list, container, false)

        actionRecyclerView = view.findViewById(R.id.action_recycler_view) as RecyclerView
        actionRecyclerView.layoutManager = LinearLayoutManager(context)

        updateUI()

        return view
    }


    private fun updateUI() {
        val actions = selectActionListViewModel.actions
        adapter = ActionAdapter(actions)
        actionRecyclerView.adapter = adapter
    }

    private inner class ActionHolder(view: View) : RecyclerView.ViewHolder(view) {
        private lateinit var action: Action

        private val titleTextView: TextView = itemView.findViewById(R.id.action_title)
        private val checkBox: CheckBox = itemView.findViewById(R.id.action_checkbox)

        fun bind(action: Action) {
            this.action = action
            titleTextView.text = this.action.title
            titleTextView.setOnClickListener {
                //checkBox to select stretching exercises to put into stretch plan
                checkBox.isChecked = !checkBox.isChecked
            }
        }
    }

    private inner class ActionAdapter(var actions: List<Action>)
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
        fun newInstance(): SelectActionFragment {
            return SelectActionFragment()
        }
    }
}