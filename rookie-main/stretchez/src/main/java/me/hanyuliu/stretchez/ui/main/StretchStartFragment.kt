package me.hanyuliu.stretchez.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.Nullable
import androidx.fragment.app.FragmentTransaction
import me.hanyuliu.stretchez.R
import me.hanyuliu.stretchez.StretchPlanListFragment
import me.hanyuliu.stretchez.WorkOutFragment

class StretchStartFragment : Fragment() {

    private lateinit var stretchButton: Button


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_stretch_start, container, false)

        stretchButton = view.findViewById(R.id.stretch_btn) as Button

//        stretchButton.setOnClickListener {
//            val workoutFragment = WorkOutFragment()
//            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
//            transaction.replace(R.id.fragment_container, workoutFragment)
//            transaction.commit()
//        }

        stretchButton.setOnClickListener {
            val stretchPlanListFragment = StretchPlanListFragment()
            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.fragment_container, stretchPlanListFragment)
            transaction.commit()
        }

        return view
    }


    companion object {
        fun newInstance() = StretchStartFragment()
    }
}