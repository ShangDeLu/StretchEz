package me.hanyuliu.stretchez

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import me.hanyuliu.stretchez.ui.main.StretchStartFragment
import java.util.*

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity(), StretchPlanListFragment.Callbacks{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (currentFragment == null) {
            val fragment = StretchStartFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commitNow()
        }
    }

    override fun onStretchPlanSelected(stretchPlanId: UUID, option: Int) {
//        Log.d(TAG, "MainActivity.onStretchPlanSelected: $stretchPlanId")
//        val fragment = StretchPlanFragment()
        val fragment = StretchPlanFragment.newInstance(stretchPlanId, option)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // fragment transaction to the back stack
            .commit()
    }
}