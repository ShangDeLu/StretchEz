package me.shangdelu.stretchez.database

import androidx.room.TypeConverter
import java.util.*

class StretchPlanTypeConverters {
    //Room can only read primitive types

    @TypeConverter
    fun toUUID(uuid: String?): UUID? {
        //if uuid is null, return null
        return uuid?.let {
            UUID.fromString(it)
        }
    }

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

}