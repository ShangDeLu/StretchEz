package me.hanyuliu.stretchez

import androidx.lifecycle.ViewModel

class StretchPlanListViewModel : ViewModel() {

//    val stretchPlans = mutableListOf<StretchPlan>()
//
//    init {
//        for (i in 0 until 100) {
//            val stretchPlan = StretchPlan()
//            stretchPlan.title = "Stretch Plan #$i"
//            stretchPlans += stretchPlan
//        }
//    }

    private val stretchPlanRepository = StretchPlanRepository.get()
    val stretchPlanListLiveData = stretchPlanRepository.getStretchPlans()

    fun addStretchPlan(stretchPlan: StretchPlan) {
        stretchPlanRepository.addStretchPlan(stretchPlan)
    }

    fun deleteStretchPlan(stretchPlan: StretchPlan) {
        stretchPlanRepository.deleteStretchPlan(stretchPlan)
    }

    fun updateStretchPlan(stretchPlan: StretchPlan) {
        stretchPlanRepository.updateStretchPlan(stretchPlan)
    }
}