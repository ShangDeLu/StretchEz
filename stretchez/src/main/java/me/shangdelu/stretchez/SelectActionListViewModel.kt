package me.shangdelu.stretchez

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import me.shangdelu.stretchez.database.StretchExercise
import java.util.*

class SelectActionListViewModel : ViewModel() {
    private val selectActionRepository = StretchPlanRepository.get()

    fun getExercisesOfPlan(planID: UUID?): LiveData<List<StretchExercise>> {
        return selectActionRepository.getExercisesOfPlan(planID)
    }

    //add planID to template exercise selected and add to db
    fun addStretchExercise(exercise: StretchExercise) {
        selectActionRepository.addExerciseToPlan(exercise)
    }

    //update exercise when changes are made
    fun updateExercise(exercise: StretchExercise) {
        selectActionRepository.updateExercise(exercise)
    }

    //delete exercise of a plan when exercise is no longer being selected
    fun deleteExercise(exercise: StretchExercise) {
        selectActionRepository.deleteExercise(exercise)
    }

}