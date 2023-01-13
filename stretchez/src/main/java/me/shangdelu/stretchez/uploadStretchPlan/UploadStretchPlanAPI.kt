package me.shangdelu.stretchez.uploadStretchPlan

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UploadStretchPlanAPI {
    @POST("api/stretchPlan")
    fun uploadStretchPlan(@Body contents: UploadStretchPlan): Call<Void>
}