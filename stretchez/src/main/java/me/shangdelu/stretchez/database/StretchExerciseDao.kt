package me.shangdelu.stretchez.database

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Dao
interface StretchExerciseDao {
    //Use LiveData to pass data between threads and between different parts of the application.
    //LiveData is a data holder class, and Room is built to work with LiveData.

    @Query("SELECT * FROM StretchExercise WHERE planID=(:planID) ORDER BY orderNumber ASC")
    //use planID to get all the exercises in that plan
    fun getExercisesOfPlan(planID: UUID?): LiveData<List<StretchExercise>>

    @Query("SELECT * FROM StretchExercise WHERE planID is null")
    //template exercise do not have planID
    fun getTemplateExercises(): LiveData<List<StretchExercise>>

    @Query("SELECT * FROM StretchExercise WHERE exerciseID=(:exerciseID)")
    fun getExercise(exerciseID: Int?): LiveData<StretchExercise?>

    @Insert
    fun addExerciseToPlan(stretchExercise: StretchExercise)

    @Insert
    fun addTemplateExercise(stretchExercise: StretchExercise)

    @Update
    fun updateExercise(stretchExercise: StretchExercise)

    @Delete
    fun deleteExercise(stretchExercise: StretchExercise)
}