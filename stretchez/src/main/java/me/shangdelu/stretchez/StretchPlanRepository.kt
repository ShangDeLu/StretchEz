package me.shangdelu.stretchez

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import me.shangdelu.stretchez.database.*
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "stretchPlan-database"

class StretchPlanRepository private constructor(context: Context) {

    private val database: StretchPlanDatabase = Room.databaseBuilder(
        context.applicationContext,
        StretchPlanDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(migration_1_2)
        .addMigrations(migration_3_4)
        .createFromAsset("database/templates.db") //prepopulate templates into database
        .build()

    private val stretchPlanDao = database.stretchPlanDao()
    private val stretchExerciseDao = database.stretchExerciseDao()
    //use an executor that points to a new thread, so anything execute with executor
    //happens off the main thread.
    private val executor = Executors.newSingleThreadExecutor()

    //StretchPlans Functions
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

    //Stretch Exercise Functions
    fun getExercisesOfPlan(planID: UUID?): LiveData<List<StretchExercise>> = stretchExerciseDao.getExercisesOfPlan(planID)

    fun getTemplateExercises(): LiveData<List<StretchExercise>> = stretchExerciseDao.getTemplateExercises()

    fun getExercise(exerciseID: Int?): LiveData<StretchExercise?> = stretchExerciseDao.getExercise(exerciseID)

    fun getExerciseFromLink(exerciseLink: String?): LiveData<List<StretchExercise>> = stretchExerciseDao.getExerciseFromLink(exerciseLink)

    fun addExerciseToPlan(stretchExercise: StretchExercise) {
        executor.execute {
            stretchExerciseDao.addExerciseToPlan(stretchExercise)
        }
    }

    fun addTemplateExercise(stretchExercise: StretchExercise) {
        executor.execute {
            stretchExerciseDao.addTemplateExercise(stretchExercise)
        }
    }

    fun updateExercise(stretchExercise: StretchExercise) {
        executor.execute {
            stretchExerciseDao.updateExercise(stretchExercise)
        }
    }

    fun deleteExercise(stretchExercise: StretchExercise) {
        executor.execute {
            stretchExerciseDao.deleteExercise(stretchExercise)
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