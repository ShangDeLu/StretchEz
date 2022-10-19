package me.shangdelu.stretchez.database

import androidx.lifecycle.LiveData
import androidx.room.*
import me.shangdelu.stretchez.StretchPlan
import java.util.*

@Dao
interface StretchPlanDao {

    @Query("SELECT * FROM stretchPlan WHERE timestamp is null")
    fun getStretchPlans(): LiveData<List<StretchPlan>>

    @Query("SELECT * FROM stretchPlan WHERE id=(:id)")
    fun getStretchPlan(id: UUID): LiveData<StretchPlan?>

    @Update
    fun updateStretchPlan(stretchPlan: StretchPlan)

    @Insert
    fun addStretchPlan(stretchPlan: StretchPlan)

    @Delete
    fun deleteStretchPlan(stretchPlan: StretchPlan)
}