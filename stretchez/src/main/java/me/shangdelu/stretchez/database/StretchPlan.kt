package me.shangdelu.stretchez.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class StretchPlan(@PrimaryKey val id: UUID = UUID.randomUUID(),
                       var title: String = "",
                       var description: String = "",
                       var duration: Int = 0,
                       var timestamp: Long? = null)
//timestamp is only given when the plan is moved to recycle bin