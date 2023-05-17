package me.shangdelu.stretchez

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import me.shangdelu.stretchez.database.StretchExercise
import me.shangdelu.stretchez.database.StretchPlan
import java.util.*

class StretchPlanListViewModel : ViewModel() {

    private val stretchPlanRepository = StretchPlanRepository.get()
    val stretchPlanListLiveData = stretchPlanRepository.getStretchPlans()

    fun addStretchPlan(stretchPlan: StretchPlan) {
        stretchPlanRepository.addStretchPlan(stretchPlan)
    }

    fun deleteStretchPlan(stretchPlan: StretchPlan) {
        stretchPlanRepository.deleteStretchPlan(stretchPlan)
    }

    fun getExercisesOfPlan(planID: UUID): LiveData<List<StretchExercise>> {
        return stretchPlanRepository.getExercisesOfPlan(planID)
    }

    fun deleteStretchExercise(stretchExercise: StretchExercise) {
        stretchPlanRepository.deleteExercise(stretchExercise)
    }

    fun updateStretchPlan(stretchPlan: StretchPlan) {
        stretchPlanRepository.updateStretchPlan(stretchPlan)
    }
}