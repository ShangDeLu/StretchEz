package me.shangdelu.stretchez

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.shangdelu.stretchez.ui.main.ui.dashboard.StretchPlanListFragment
import me.shangdelu.stretchez.ui.main.ui.home.StretchStartFragment
import java.util.*

class MainActivity : AppCompatActivity(){

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
}