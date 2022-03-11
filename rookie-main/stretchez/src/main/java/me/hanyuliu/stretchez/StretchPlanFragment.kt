package me.hanyuliu.stretchez

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer
import java.util.*
import kotlin.properties.Delegates

private const val TAG = "StretchPlanFragment"
private const val ARG_STRETCH_PLAN_ID = "stretchPlan_id"

class StretchPlanFragment : Fragment() {

    private lateinit var stretchPlan: StretchPlan
    private lateinit var stretchPlanTitle: EditText
    private lateinit var stretchPlanDescription: EditText
    private lateinit var stretchPlanStartButton: Button
    private lateinit var selectActionButton: Button
    private lateinit var stretchPlanSaveButton: Button
    private lateinit var stretchPlanCancelButton: Button
    private lateinit var stretchPlanRepository: StretchPlanRepository
    private var argumentOption: Int = 0
    private var stretchPlanId: UUID? = null
    private val stretchPlanDetailViewModel: StretchPlanDetailViewModel by lazy {
        ViewModelProviders.of(this).get(StretchPlanDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stretchPlan = StretchPlan()
        stretchPlanId = arguments?.getSerializable(ARG_STRETCH_PLAN_ID) as UUID?
        stretchPlanId?.let {
            stretchPlanDetailViewModel.loadStretchPlan(it)
        }
        stretchPlanRepository = StretchPlanRepository.get()
        argumentOption = arguments?.getInt("Option") ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stretch_plan, container, false)

        stretchPlanTitle = view.findViewById(R.id.stretch_plan_title) as EditText
        stretchPlanDescription = view.findViewById(R.id.stretch_plan_description) as EditText
        stretchPlanStartButton = view.findViewById(R.id.stretch_plan_start) as Button
        selectActionButton = view.findViewById(R.id.select_action) as Button
        stretchPlanSaveButton = view.findViewById(R.id.stretch_plan_save) as Button
        stretchPlanCancelButton = view.findViewById(R.id.stretch_plan_cancel) as Button

        stretchPlanStartButton.setOnClickListener {
            val workoutFragment = WorkOutFragment()
            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.fragment_container, workoutFragment)
            transaction.commit()
        }

        selectActionButton.setOnClickListener {
            val selectActionFragment = SelectActionFragment()
            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.fragment_container, selectActionFragment)
            transaction.commit()
        }

        stretchPlanSaveButton.setOnClickListener {
            if (stretchPlanTitle.text.toString().isEmpty()) {
                Toast.makeText(this.context, R.string.no_title_toast, Toast.LENGTH_LONG).show()
            } else {
                if (argumentOption == 0) {
                    stretchPlanRepository.updateStretchPlan(StretchPlan(
                        id = stretchPlanId!!,
                        title = stretchPlanTitle.text.toString(),
                        description = stretchPlanDescription.text.toString(), duration = 60
                    ))
                    val stretchPlanListFragment = StretchPlanListFragment()
                    val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
                    transaction.replace(R.id.fragment_container, stretchPlanListFragment)
                    transaction.commit()
                } else {
                    stretchPlanRepository.addStretchPlan(StretchPlan(
                            title = stretchPlanTitle.text.toString(),
                            description = stretchPlanDescription.text.toString(), duration = 60
                    ))
                    val stretchPlanListFragment = StretchPlanListFragment()
                    val transaction: FragmentTransaction =
                        requireFragmentManager().beginTransaction()
                    transaction.replace(R.id.fragment_container, stretchPlanListFragment)
                    transaction.commit()
                }
            }
        }

        stretchPlanCancelButton.setOnClickListener {
            val stretchPlanListFragment = StretchPlanListFragment()
            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.fragment_container, stretchPlanListFragment)
            transaction.commit()
        }

        var fragOption = arguments?.getInt("Option")

        // When Open Existing StretchPlan in RecyclerView
        if (fragOption == 0) {
//            stretchPlanSaveButton.visibility = View.INVISIBLE
//            stretchPlanCancelButton.visibility = View.INVISIBLE

        // Creating New StretchPlan
        } else {
            stretchPlanStartButton.visibility = View.INVISIBLE

        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stretchPlanDetailViewModel.stretchPlanLiveData.observe(
            viewLifecycleOwner,
            Observer { stretchPlan ->
                stretchPlan?.let {
                    this.stretchPlan = stretchPlan
//                    Log.i(tag, "UUID is: " + stretchPlan.id)
//                    Log.i(tag, "Current UUID is: " + stretchPlanId)
                    updateUI()
                }
            })
    }

    private fun updateUI() {
        stretchPlanTitle.setText(stretchPlan.title)
        stretchPlanDescription.setText(stretchPlan.description)
    }

    companion object {
        // Fragment Arguments pass plan ID from MainActivity to StretchPlanFragment
        fun newInstance(stretchPlanId: UUID, option: Int): StretchPlanFragment {
            val args = Bundle().apply {
                putSerializable(ARG_STRETCH_PLAN_ID, stretchPlanId)
                putInt("Option", option)
            }
            return StretchPlanFragment().apply {
                arguments = args
            }
        }
    }

}