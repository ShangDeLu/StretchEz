package me.shangdelu.stretchez.uploadTest

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UploadTestAPI {
    @POST("api/upload")
    fun uploadContents(@Body contents: UploadData): Call<Void>
}