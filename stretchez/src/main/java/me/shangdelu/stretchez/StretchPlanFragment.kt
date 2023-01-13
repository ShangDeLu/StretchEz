package me.shangdelu.stretchez

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import me.shangdelu.stretchez.database.StretchPlan
import java.util.*

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
        ViewModelProvider(this)[StretchPlanDetailViewModel::class.java]
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

        //Button to stretching
        stretchPlanStartButton.setOnClickListener {
            val workoutFragment = WorkOutFragment()
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, workoutFragment)
            transaction.commit()
        }

        //Button to view available stretching exercises
        selectActionButton.setOnClickListener {
            val selectActionFragment = SelectActionFragment()
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, selectActionFragment)
            transaction.commit()
        }

        //Button to save current stretch plan
        stretchPlanSaveButton.setOnClickListener {
            if (stretchPlanTitle.text.toString().isEmpty()) {
                Toast.makeText(this.context, R.string.no_title_toast, Toast.LENGTH_LONG).show()
            } else {
                if (argumentOption == 0) { //when plan already exist, option = 0
                    stretchPlanRepository.updateStretchPlan(
                        StretchPlan(
                        id = stretchPlanId!!,
                        title = stretchPlanTitle.text.toString(),
                        description = stretchPlanDescription.text.toString(), duration = 60
                    )
                    )
                    val stretchPlanListFragment = StretchPlanListFragment()
                    val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragment_container, stretchPlanListFragment)
                    transaction.commit()
                } else { //when creating new plan, option = 1
                    stretchPlanRepository.addStretchPlan(
                        StretchPlan(
                            title = stretchPlanTitle.text.toString(),
                            description = stretchPlanDescription.text.toString(), duration = 60
                    )
                    )
                    val stretchPlanListFragment = StretchPlanListFragment()
                    val transaction: FragmentTransaction =
                        parentFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragment_container, stretchPlanListFragment)
                    transaction.commit()
                }
            }
        }

        //Button to return to list of stretch plan
        stretchPlanCancelButton.setOnClickListener {
            val stretchPlanListFragment = StretchPlanListFragment()
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, stretchPlanListFragment)
            transaction.commit()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stretchPlanDetailViewModel.stretchPlanLiveData.observe(
            viewLifecycleOwner)
        { stretchPlan ->
            stretchPlan?.let {
                this.stretchPlan = stretchPlan
                updateUI()
            }
        }
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