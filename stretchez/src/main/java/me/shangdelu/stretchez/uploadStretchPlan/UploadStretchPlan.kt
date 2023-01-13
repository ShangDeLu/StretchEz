package me.shangdelu.stretchez.uploadStretchPlan

import java.util.UUID

data class UploadStretchPlan(var planID: UUID, var userID: UUID,
                             var title: String = "", var description: String = "",
                             var duration: Int = 0, var timeStamp: Long? = null) //use timeStamp to match setter of StretchPlan class on remote