package me.shangdelu.stretchez

import androidx.lifecycle.ViewModel
import me.shangdelu.stretchez.database.StretchExercise

class SelectActionListViewModel : ViewModel() {
    private val selectActionRepository = StretchPlanRepository.get()
    val selectActionLiveData = selectActionRepository.getTemplateExercises()

    //add planID to template exercise selected and add to db
    fun addStretchExercise(exercise: StretchExercise) {
        selectActionRepository.addExerciseToPlan(exercise)
    }

    //delete exercise of a plan when exercise is no longer being selected
    fun deleteExercise(exercise: StretchExercise) {
        selectActionRepository.deleteExercise(exercise)
    }

}