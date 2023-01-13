package me.shangdelu.stretchez

import android.app.Application

class StretchEzApplication : Application() {

    //Set up the repository initialization once the application is ready.
    override fun onCreate() {
        super.onCreate()
        StretchPlanRepository.initialize(this)
    }
}