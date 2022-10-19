package me.shangdelu.stretchez.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import me.shangdelu.stretchez.R
import me.shangdelu.stretchez.StretchPlanListFragment
import me.shangdelu.stretchez.raspberryPi.RaspberryFragment
import me.shangdelu.stretchez.uploadTest.UploadTestFragment

class StretchStartFragment : Fragment() {

    private lateinit var stretchButton: Button

    private lateinit var piButton: Button

    private lateinit var uploadButton: Button


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_stretch_start, container, false)

        stretchButton = view.findViewById(R.id.stretch_btn) as Button

        piButton = view.findViewById(R.id.pi_btn) as Button

        uploadButton = view.findViewById(R.id.upload_btn) as Button

        //Button to the list of stretching plan
        stretchButton.setOnClickListener {
            val stretchPlanListFragment = StretchPlanListFragment()
            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.fragment_container, stretchPlanListFragment)
            transaction.commit()
        }

        //Button to the fragment testing data retrieving from remote
        piButton.setOnClickListener {
            val raspberryFragment = RaspberryFragment()
            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.fragment_container, raspberryFragment)
            transaction.commit()
        }

        //Button to the fragment testing uploading data to remote
        uploadButton.setOnClickListener {
            val uploadTestFragment = UploadTestFragment()
            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.fragment_container, uploadTestFragment)
            transaction.commit()
        }

        return view
    }


    companion object {
        fun newInstance() = StretchStartFragment()
    }
}