package me.shangdelu.stretchez

import androidx.lifecycle.ViewModel
import me.shangdelu.stretchez.database.StretchExercise

class StretchExerciseListViewModel: ViewModel() {

    private val stretchExerciseRepository = StretchPlanRepository.get()
    val stretchExerciseLiveData = stretchExerciseRepository.getTemplateExercises()

    fun addStretchExercise(exercise: StretchExercise) {
        stretchExerciseRepository.addExerciseToPlan(exercise)
    }

    fun addTemplateExercise(exercise: StretchExercise) {
        stretchExerciseRepository.addTemplateExercise(exercise)
    }

    fun updateExercise(exercise: StretchExercise) {
        stretchExerciseRepository.updateExercise(exercise)
    }

    fun deleteExercise(exercise: StretchExercise) {
        stretchExerciseRepository.deleteExercise(exercise)
    }


}