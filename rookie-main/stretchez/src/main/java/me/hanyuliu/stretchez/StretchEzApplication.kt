package me.hanyuliu.stretchez

import android.app.Application

class StretchEzApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        StretchPlanRepository.initialize(this)
    }
}