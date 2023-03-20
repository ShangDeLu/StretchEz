package me.shangdelu.stretchez

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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
    private lateinit var argumentSelectAction: Bundle
    private lateinit var argumentWorkOut: Bundle
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
        //length of interval between exercises, default at 5 for now, could be parameter of data class later.
        val interval: Int = 5
        //Put stretchPlanId and argumentOption into a bundle for selectActionFragment
        argumentSelectAction = SelectActionFragment.newInstance(stretchPlanId, argumentOption)
        //Put stretchPlanId, argumentOption and interval into a bundle for WorkOutFragment
        argumentWorkOut = WorkOutFragment.newInstance(stretchPlanId, argumentOption, interval)

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
            //pass the bundle to workOutFragment
            findNavController().navigate(R.id.action_navigation_stretch_plan_to_navigation_work_out, argumentWorkOut)
        }

        //Button to view available stretching exercises
        selectActionButton.setOnClickListener {
            //pass the bundle to selectActionFragment
            findNavController().navigate(
                R.id.action_navigation_stretch_plan_to_navigation_select_action, argumentSelectAction)
        }


        //TODO 1: ask about autofill hints for EditText.
        //TODO 2: consider about whether to use MediaController or not.
        //TODO 3: make sure the VideoView is fixed size, and video resize to fit the VideoView.
        //TODO 4: learn about WebView, and for the first step, use WebView to play a youtube video.


        if (argumentOption == 1) { //when creating new plan
            //hide stretchPlanStartButton and selectActionButton
            stretchPlanStartButton.visibility = View.GONE
            selectActionButton.visibility = View.GONE
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
                    ))
                    findNavController().navigate(R.id.action_navigation_stretch_plan_to_navigation_stretch_plan_list)

                } else { //when creating new plan, option = 1
                    stretchPlanRepository.addStretchPlan(
                        StretchPlan(
                            title = stretchPlanTitle.text.toString(),
                            description = stretchPlanDescription.text.toString(), duration = 60
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
            }
        }
    }

    private fun updateUI() {
        stretchPlanTitle.setText(stretchPlan.title)
        stretchPlanDescription.setText(stretchPlan.description)
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