package me.shangdelu.stretchez.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import me.shangdelu.stretchez.R
import me.shangdelu.stretchez.StretchExerciseListFragment
import me.shangdelu.stretchez.StretchPlanListFragment
import me.shangdelu.stretchez.raspberryPi.RaspberryFragment
import me.shangdelu.stretchez.uploadStretchPlan.UploadStretchPlanFragment
import me.shangdelu.stretchez.uploadTest.UploadTestFragment

class StretchStartFragment : Fragment() {

    private lateinit var stretchButton: Button

    private lateinit var exerciseButton: Button

//    private lateinit var piButton: Button
//
//    private lateinit var uploadButton: Button
//
//    private lateinit var uploadStretchPlan: Button


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_stretch_start, container, false)

        stretchButton = view.findViewById(R.id.stretch_btn) as Button

        exerciseButton = view.findViewById(R.id.exercise_btn) as Button


//        piButton = view.findViewById(R.id.pi_btn) as Button
//
//        uploadButton = view.findViewById(R.id.upload_btn) as Button
//
//        uploadStretchPlan = view.findViewById(R.id.upload_stretchPlan_btn) as Button //set the button for upload stretchPlan

        //Button to the list of stretch plans
        stretchButton.setOnClickListener {
            val stretchPlanListFragment = StretchPlanListFragment()
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, stretchPlanListFragment)
            transaction.commit()
        }

        //Button to the list of stretching exercises
        exerciseButton.setOnClickListener {
            val stretchExerciseListFragment = StretchExerciseListFragment()
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, stretchExerciseListFragment)
            transaction.commit()
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

        return view
    }


    companion object {
        fun newInstance() = StretchStartFragment()
    }
}