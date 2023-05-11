package me.shangdelu.stretchez

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import me.shangdelu.stretchez.database.StretchPlan
import java.util.*

private const val TAG = "StretchPlanFragment"
private const val ARG_STRETCH_PLAN_ID = "stretchPlan_id"
private const val TITLE_ERROR = "Plan Title cannot be empty"

class StretchPlanFragment : Fragment() {

    private lateinit var stretchPlan: StretchPlan
    private lateinit var stretchPlanTitle: TextInputEditText
    private lateinit var stretchPlanDescription: TextInputEditText
    private lateinit var stretchPlanStartButton: Button
    private lateinit var selectActionButton: Button
    private lateinit var stretchPlanSaveButton: Button
    private lateinit var stretchPlanCancelButton: Button
    private lateinit var stretchPlanRepository: StretchPlanRepository
    private lateinit var argumentSelectAction: Bundle
    private lateinit var argumentWorkOut: Bundle
    private var argumentOption: Int = 0
    private var stretchPlanId: UUID? = null
    //create an array that stores the options of seconds for interval of StretchPlan
    private var planIntervals = Array(6) {(it + 1) * 5}
    //Spinner for interval of StretchPlan
    private lateinit var intervalSpinner: Spinner
    //TextInputLayout for StretchPlan Title Input
    private lateinit var stretchPlanTitleContainer: TextInputLayout

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
        //Put stretchPlanId and argumentOption into a bundle for selectActionFragment
        argumentSelectAction = SelectActionFragment.newInstance(stretchPlanId, argumentOption)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stretch_plan, container, false)

        stretchPlanTitle = view.findViewById(R.id.stretch_plan_title_editText) as TextInputEditText
        stretchPlanDescription = view.findViewById(R.id.stretch_plan_description_editText) as TextInputEditText
        stretchPlanStartButton = view.findViewById(R.id.stretch_plan_start) as Button
        selectActionButton = view.findViewById(R.id.select_action) as Button
        stretchPlanSaveButton = view.findViewById(R.id.stretch_plan_save) as Button
        stretchPlanCancelButton = view.findViewById(R.id.stretch_plan_cancel) as Button

        intervalSpinner = view.findViewById(R.id.stretch_plan_interval_spinner) as Spinner
        stretchPlanTitleContainer = view.findViewById(R.id.stretch_plan_title_container) as TextInputLayout

        //create the instance of ArrayAdapter having the list of minutes and seconds)
        val intervalAdapter = ArrayAdapter(this.requireContext(), android.R.layout.simple_spinner_item, planIntervals)

        //set simple layout resource file for each item of spinner
        intervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        //set the ArrayAdapter data on the spinner which binds data to spinner
        intervalSpinner.adapter = intervalAdapter

        //set the focus listener for plan title input
        planTitleFocusListener()

        //Button to stretching
        stretchPlanStartButton.setOnClickListener {
            //pass the bundle to workOutFragment
            findNavController().navigate(R.id.action_navigation_stretch_plan_to_navigation_work_out, argumentWorkOut)
        }

        //Button to view available stretching exercises
        selectActionButton.setOnClickListener {
            //pass the bundle to selectActionFragment
            findNavController().navigate(
                R.id.action_navigation_stretch_plan_to_navigation_select_action, argumentSelectAction)
        }


        if (argumentOption == 1) { //when creating new plan
            //hide stretchPlanStartButton and selectActionButton
            stretchPlanStartButton.visibility = View.GONE
            selectActionButton.visibility = View.GONE
        }

        //Button to save current stretch plan
        stretchPlanSaveButton.setOnClickListener {
            //get the item selected on intervalSpinner and save it as Int
            val interval = intervalSpinner.selectedItem as Int

            if (stretchPlanTitle.text.toString().isEmpty()) {
                //if title input is empty, show an error message to the user
                stretchPlanTitleContainer.error = TITLE_ERROR
            } else {
                if (argumentOption == 0) { //when plan already exist, option = 0
                    stretchPlanRepository.updateStretchPlan(
                        StretchPlan(
                            id = stretchPlanId!!,
                            title = stretchPlanTitle.text.toString(),
                            description = stretchPlanDescription.text.toString(),
                            duration = interval
                    ))
                    findNavController().navigate(R.id.action_navigation_stretch_plan_to_navigation_stretch_plan_list)

                } else { //when creating new plan, option = 1
                    stretchPlanRepository.addStretchPlan(
                        StretchPlan(
                            title = stretchPlanTitle.text.toString(),
                            description = stretchPlanDescription.text.toString(),
                            duration = interval
                    ))
                    findNavController().navigate(R.id.action_navigation_stretch_plan_to_navigation_stretch_plan_list)
                }
            }
        }

        //Button to return to list of stretch plan
        stretchPlanCancelButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_stretch_plan_to_navigation_stretch_plan_list)
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
                //Put stretchPlanId, argumentOption and interval into a bundle for WorkOutFragment
                argumentWorkOut = WorkOutFragment.newInstance(stretchPlanId, argumentOption, stretchPlan.duration)
            }
        }
    }

    private fun planTitleFocusListener() {
        //use setOnFocusChangeListener on the StretchPlan Title input
        stretchPlanTitle.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) { //once the title input lose focus
                //check the state of title input
                if (stretchPlanTitle.text.toString().isNotEmpty()) {
                    //if title input is not empty, stop showing the error message
                    stretchPlanTitleContainer.isErrorEnabled = false
                } else {
                    //if title input is empty, show an error message to the user
                    stretchPlanTitleContainer.error = TITLE_ERROR
                }
            }
        }
    }

    private fun updateUI() {
        stretchPlanTitle.setText(stretchPlan.title)
        stretchPlanDescription.setText(stretchPlan.description)
        //Use stretchPlan.duration to calculate the index of the interval in the array and pass it to the spinner
        intervalSpinner.setSelection((stretchPlan.duration / 5) - 1)
    }

    companion object {
        fun newInstance(stretchPlanId: UUID, option: Int): Bundle {
            return Bundle().apply {
                putSerializable(ARG_STRETCH_PLAN_ID, stretchPlanId)
                putInt("Option", option)
            }
        }
    }

}