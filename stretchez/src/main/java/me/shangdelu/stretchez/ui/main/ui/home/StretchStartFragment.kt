package me.shangdelu.stretchez.ui.main.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import me.shangdelu.stretchez.R
import me.shangdelu.stretchez.StretchExerciseDetailViewModel
import me.shangdelu.stretchez.StretchPlanRepository
import me.shangdelu.stretchez.database.StretchExercise
import me.shangdelu.stretchez.databinding.FragmentStretchStartBinding

class StretchStartFragment : Fragment() {

    private lateinit var binding: FragmentStretchStartBinding
    private lateinit var stretchExerciseRepository: StretchPlanRepository
    //set a flag for if template already exist
    private var templateExist = false

    //Associate the Fragment with the ViewModel
    private val stretchStartViewModel: StretchStartViewModel by lazy {
        ViewModelProvider(this)[StretchStartViewModel::class.java]
    }

//    private lateinit var piButton: Button
//
//    private lateinit var uploadButton: Button
//
//    private lateinit var uploadStretchPlan: Button


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {

        binding = FragmentStretchStartBinding.inflate(inflater, container, false)

        //initialize StretchExerciseRepository to add Template Exercise
        stretchExerciseRepository = StretchPlanRepository.get()

        //get the resource path of the templates
        val template1Link = "android.resource://" + requireContext().packageName + "/" + R.raw.stretch1
        val template2Link = "android.resource://" + requireContext().packageName + "/" + R.raw.stretch2
        val template3Link = "android.resource://" + requireContext().packageName + "/" + R.raw.stretch3

        //if checkTemplateExist returns false, means template do not exist, then hard code it into the database
        if (!checkTemplateExist(template1Link)) {
            //Hardcode templates into the database
            stretchExerciseRepository.addTemplateExercise(
                StretchExercise(
                    exerciseName = "Trapezius Stretch",
                    exerciseDescription = "Stretching the Trapezius muscle is a great way to relieve neck, shoulder, upper and middle back pain.",
                    exerciseLink = template1Link,
                    exerciseDuration = 30
                )
            )
        }
        //if checkTemplateExist returns false, means template do not exist, then hard code it into the database
        if (!checkTemplateExist(template2Link)) {
            //Hardcode templates into the database
            stretchExerciseRepository.addTemplateExercise(
                StretchExercise(
                    exerciseName = "Levator Scapulae Stretch",
                    exerciseDescription = "Stretching the Levator Scapulae muscle to relieve neck pain, and make you more resistant to stiff neck and neck pain",
                    exerciseLink = template2Link,
                    exerciseDuration = 30
                )
            )
        }
        //if checkTemplateExist returns false, means template do not exist, then hard code it into the database
        if (!checkTemplateExist(template3Link)) {
            //Hardcode templates into the database
            stretchExerciseRepository.addTemplateExercise(
                StretchExercise(
                    exerciseName = "Forearm Stretch",
                    exerciseDescription = "Stretch your forearms to increase their flexibility, and reduce the risk of injury",
                    exerciseLink = template3Link,
                    exerciseDuration = 30
                )
            )
        }

//        piButton = view.findViewById(R.id.pi_btn) as Button
//
//        uploadButton = view.findViewById(R.id.upload_btn) as Button
//
//        uploadStretchPlan = view.findViewById(R.id.upload_stretchPlan_btn) as Button //set the button for upload stretchPlan

        //Button to the list of stretch plans
//        binding.stretchBtn.setOnClickListener {
//            findNavController().navigate(R.id.action_navigation_home_to_navigation_stretch_plan_list)
//        }
//
//        //Button to the list of stretching exercises
//        binding.exerciseBtn.setOnClickListener {
//            findNavController().navigate(R.id.action_navigation_home_to_navigation_exercise_list)
//        }

//        //Button to the fragment testing data retrieving from remote
//        piButton.setOnClickListener {
//            val raspberryFragment = RaspberryFragment()
//            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
//            transaction.replace(R.id.fragment_container, raspberryFragment)
//            transaction.commit()
//        }
//
//        //Button to the fragment testing uploading data to remote
//        uploadButton.setOnClickListener {
//            val uploadTestFragment = UploadTestFragment()
//            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
//            transaction.replace(R.id.fragment_container, uploadTestFragment)
//            transaction.commit()
//        }
//
//        //Button to fragment testing uploading StretchPlan to remote database
//        uploadStretchPlan.setOnClickListener {
//            val uploadStretchPlanFragment = UploadStretchPlanFragment()
//            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
//            transaction.replace(R.id.fragment_container, uploadStretchPlanFragment)
//            transaction.commit()
//        }

        return binding.root
    }


    private fun checkTemplateExist(exerciseLink: String): Boolean {
        //load the given exerciseLink
        exerciseLink.let {
            stretchStartViewModel.loadExerciseLink(exerciseLink)
        }
        //use ViewModel to check if LiveData returned is empty or not
        stretchStartViewModel.templateLiveData.observe(viewLifecycleOwner) { templates ->
            templateExist = if (templates.isNotEmpty()) {
                println(templates[0])
                //if templates is not empty, meaning template already exist, change the flag to true
                true
            } else {
                //otherwise, templates is empty, meaning template do not exist, flag remain false
                false
            }
        }
        return templateExist
    }

    companion object {
        fun newInstance() = StretchStartFragment()
    }
}