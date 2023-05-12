package me.shangdelu.stretchez.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class StretchExercise (@PrimaryKey(autoGenerate = true) var exerciseID: Int = 0,
                            //Don't set planID as ForeignKey since template Exercise have no planID,
                            // and planID cannot be null as ForeignKey
                            var planID: UUID? = null,
                            var exerciseName: String = "",
                            var exerciseDescription: String = "",
                            var exerciseDuration: Int = 30,
                            var exerciseLink: String = "",
                            var orderNumber: Int = -1,
                            //boolean check parameter for whether an exercise is a template,
                            //0 means it's not a template, 1 means it is a template
                            var isTemplate: Int = 0)