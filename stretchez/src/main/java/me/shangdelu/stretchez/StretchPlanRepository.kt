package me.shangdelu.stretchez

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import me.shangdelu.stretchez.database.StretchPlanDatabase
import me.shangdelu.stretchez.database.migration_1_2
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "stretchPlan-database"

class StretchPlanRepository private constructor(context: Context) {

    private val database: StretchPlanDatabase = Room.databaseBuilder(
        context.applicationContext,
        StretchPlanDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(migration_1_2)
        .build()

    private val stretchPlanDao = database.stretchPlanDao()
    private val executor = Executors.newSingleThreadExecutor()

    fun getStretchPlans(): LiveData<List<StretchPlan>> = stretchPlanDao.getStretchPlans()
    
    fun getStretchPlan(id: UUID): LiveData<StretchPlan?> = stretchPlanDao.getStretchPlan(id)

    fun updateStretchPlan(stretchPlan: StretchPlan) {
        executor.execute {
            stretchPlanDao.updateStretchPlan(stretchPlan)
        }
    }

    fun addStretchPlan(stretchPlan: StretchPlan) {
        executor.execute {
            stretchPlanDao.addStretchPlan(stretchPlan)
        }
    }

    fun deleteStretchPlan(stretchPlan: StretchPlan) {
        executor.execute {
            stretchPlanDao.deleteStretchPlan((stretchPlan))
        }
    }

    companion object {
        private var INSTANCE: StretchPlanRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = StretchPlanRepository(context)
            }
        }

        fun get(): StretchPlanRepository {
            return INSTANCE ?:
            throw IllegalStateException("StretchPlanRepository must be initialized")
        }
    }
}