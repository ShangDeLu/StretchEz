package me.shangdelu.stretchez

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
}