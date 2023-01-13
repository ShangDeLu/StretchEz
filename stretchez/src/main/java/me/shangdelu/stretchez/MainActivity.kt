package me.shangdelu.stretchez

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import me.shangdelu.stretchez.database.StretchExercise
import me.shangdelu.stretchez.ui.main.StretchStartFragment
import java.util.*

class MainActivity : AppCompatActivity(), StretchPlanListFragment.Callbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (currentFragment == null) { //show the Start fragment if no current Fragment
            val fragment = StretchStartFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commitNow()
        }
    }

    override fun onStretchPlanSelected(stretchPlanId: UUID, option: Int) {
        val fragment = StretchPlanFragment.newInstance(stretchPlanId, option)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // fragment transaction to the back stack
            .commit()
    }

    override fun onExerciseSelected(exerciseID: Int?, option: Int) {
        val fragment = StretchExerciseFragment.newInstance(exerciseID, option)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) //so when press back button, will go back to the list of exercise
            .commit()
    }
}