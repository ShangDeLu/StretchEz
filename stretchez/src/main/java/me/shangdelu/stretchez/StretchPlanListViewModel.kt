package me.shangdelu.stretchez

import androidx.lifecycle.ViewModel
import me.shangdelu.stretchez.database.StretchPlan

class StretchPlanListViewModel : ViewModel() {

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