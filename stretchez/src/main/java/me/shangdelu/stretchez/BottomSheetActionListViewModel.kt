package me.shangdelu.stretchez

import androidx.lifecycle.ViewModel
import me.shangdelu.stretchez.database.StretchExercise

class BottomSheetActionListViewModel : ViewModel() {
    private val bottomSheetActionRepository = StretchPlanRepository.get()
    val bottomSheetActionLiveData = bottomSheetActionRepository.getTemplateExercises()

    //add planID to template exercise selected and add to db
    fun addStretchExercise(exercise: StretchExercise) {
        bottomSheetActionRepository.addExerciseToPlan(exercise)
    }

    //update exercise when changes are made
    fun updateExercise(exercise: StretchExercise) {
        bottomSheetActionRepository.updateExercise(exercise)
    }

    //delete exercise of a plan when exercise is no longer being selected
    fun deleteExercise(exercise: StretchExercise) {
        bottomSheetActionRepository.deleteExercise(exercise)
    }
}