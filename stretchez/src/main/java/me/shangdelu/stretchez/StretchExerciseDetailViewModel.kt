package me.shangdelu.stretchez

import androidx.lifecycle.*
import me.shangdelu.stretchez.database.StretchExercise

//Manage the database query
class StretchExerciseDetailViewModel : ViewModel() {

    private val exerciseRepository = StretchPlanRepository.get()
    private val exerciseIDLiveData = MutableLiveData<Int?>()

    //Implement a relationship where changing the exerciseID triggers new database query.
    //Stores the ID of the exercise currently displayed (or about to be displayed)
    var exerciseLiveData: LiveData<StretchExercise?> =
        //A live data transformation is a way to set up a trigger-response relationship between
        //two LiveData objects.

        //The old androidx.lifecycle when transformation is not removed yet
//        Transformations.switchMap(exerciseIDLiveData) { exerciseID ->
//            exerciseRepository.getExercise(exerciseID)}

        //Trying the new way of using switchMap as androidx.lifecycle.transformation got removed
        exerciseIDLiveData.switchMap { exerciseID ->
            exerciseRepository.getExercise(exerciseID)}

    //tell the ViewModel which exercise to be load
    fun loadExercise(exerciseID: Int?) {
        exerciseIDLiveData.value = exerciseID
    }

    fun saveExercise(exercise: StretchExercise) {
        exerciseRepository.updateExercise(exercise)
    }
}