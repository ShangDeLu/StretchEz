package me.shangdelu.stretchez.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ StretchPlan::class, StretchExercise::class ], version=4, exportSchema = true)
@TypeConverters(StretchPlanTypeConverters::class)
abstract class StretchPlanDatabase : RoomDatabase() {

    //Connect DAO to the database
    abstract fun stretchPlanDao(): StretchPlanDao
    abstract fun stretchExerciseDao(): StretchExerciseDao
}

val migration_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE StretchPlan ADD COLUMN timestamp INTEGER"
        )
    }
}

val migration_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE StretchExercise ADD COLUMN isTemplate INTEGER DEFAULT 0 NOT NULL"
        )
    }
}
