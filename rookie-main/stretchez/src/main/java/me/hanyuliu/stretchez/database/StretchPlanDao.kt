package me.hanyuliu.stretchez.database

import androidx.lifecycle.LiveData
import androidx.room.*
import me.hanyuliu.stretchez.StretchPlan
import java.util.*

@Dao
interface StretchPlanDao {

    @Query("SELECT * FROM stretchPlan WHERE timestamp is null")
//    fun getStretchPlans(): List<StretchPlan>
    fun getStretchPlans(): LiveData<List<StretchPlan>>

    @Query("SELECT * FROM stretchPlan WHERE id=(:id)")
//    fun getStretchPlan(id: UUID): StretchPlan?
    fun getStretchPlan(id: UUID): LiveData<StretchPlan?>

    @Update
    fun updateStretchPlan(stretchPlan: StretchPlan)

    @Insert
    fun addStretchPlan(stretchPlan: StretchPlan)

    @Delete
    fun deleteStretchPlan(stretchPlan: StretchPlan)
}