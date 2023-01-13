package me.shangdelu.stretchez.database

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Dao
interface StretchPlanDao {

    //If timestamp is not null, the StretchPlan has been "deleted"/moved to garbage bin
    @Query("SELECT * FROM StretchPlan WHERE timestamp is null")
    fun getStretchPlans(): LiveData<List<StretchPlan>>

    @Query("SELECT * FROM StretchPlan WHERE id=(:id)")
    fun getStretchPlan(id: UUID): LiveData<StretchPlan?>

    @Update
    fun updateStretchPlan(stretchPlan: StretchPlan)

    @Insert
    fun addStretchPlan(stretchPlan: StretchPlan)

    @Delete
    fun deleteStretchPlan(stretchPlan: StretchPlan)
}