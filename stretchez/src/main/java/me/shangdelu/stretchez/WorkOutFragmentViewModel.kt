package me.shangdelu.stretchez

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import me.shangdelu.stretchez.database.StretchExercise
import java.util.*

class WorkOutFragmentViewModel : ViewModel() {
    private val workOutRepository = StretchPlanRepository.get()

    fun getExercisesOfPlan(planID: UUID?): LiveData<List<StretchExercise>> {
        return workOutRepository.getExercisesOfPlan(planID)
    }
}