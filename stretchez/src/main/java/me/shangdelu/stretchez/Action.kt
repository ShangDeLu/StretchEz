package me.shangdelu.stretchez

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Action(@PrimaryKey val id: UUID = UUID.randomUUID(),
                  var title: String = "", var description: String = "")