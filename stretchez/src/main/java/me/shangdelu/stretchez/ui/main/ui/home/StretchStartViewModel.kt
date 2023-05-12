package me.shangdelu.stretchez.ui.main.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import me.shangdelu.stretchez.StretchPlanRepository
import me.shangdelu.stretchez.database.StretchExercise

//Manage the database query
class StretchStartViewModel : ViewModel() {

    private val templateRepository: StretchPlanRepository = StretchPlanRepository.get()
    private val exerciseLinkLiveData = MutableLiveData<String?>()

    //A live data transformation is a way to set up a trigger-response relationship between
    //two LiveData objects.
    var templateLiveData: LiveData<List<StretchExercise>> =
        exerciseLinkLiveData.switchMap { exerciseLink ->
        templateRepository.getExerciseFromLink(exerciseLink)}

    //tell the ViewModel exerciseLink
    fun loadExerciseLink(exerciseLink: String) {
        exerciseLinkLiveData.value = exerciseLink
    }
}