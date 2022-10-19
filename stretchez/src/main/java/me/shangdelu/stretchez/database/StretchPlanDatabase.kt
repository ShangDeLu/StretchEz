package me.shangdelu.stretchez.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import me.shangdelu.stretchez.StretchPlan

@Database(entities = [ StretchPlan::class ], version=2)
@TypeConverters(StretchPlanTypeConverters::class)
abstract class StretchPlanDatabase : RoomDatabase() {

    abstract fun stretchPlanDao(): StretchPlanDao
}

val migration_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE StretchPlan ADD COLUMN timestamp INTEGER"
        )
    }
}