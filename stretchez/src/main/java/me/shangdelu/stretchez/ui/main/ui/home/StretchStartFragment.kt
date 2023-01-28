package me.shangdelu.stretchez.ui.main.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import me.shangdelu.stretchez.R
import me.shangdelu.stretchez.databinding.FragmentStretchStartBinding

class StretchStartFragment : Fragment() {

    private lateinit var binding: FragmentStretchStartBinding

//    private lateinit var piButton: Button
//
//    private lateinit var uploadButton: Button
//
//    private lateinit var uploadStretchPlan: Button


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {

        binding = FragmentStretchStartBinding.inflate(inflater, container, false)

//        piButton = view.findViewById(R.id.pi_btn) as Button
//
//        uploadButton = view.findViewById(R.id.upload_btn) as Button
//
//        uploadStretchPlan = view.findViewById(R.id.upload_stretchPlan_btn) as Button //set the button for upload stretchPlan

        //Button to the list of stretch plans
        binding.stretchBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_stretch_plan_list)
        }

        //Button to the list of stretching exercises
        binding.exerciseBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_exercise_list)
        }

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


    companion object {
        fun newInstance() = StretchStartFragment()
    }
}