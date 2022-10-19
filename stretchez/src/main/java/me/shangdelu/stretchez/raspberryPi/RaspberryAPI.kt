package me.shangdelu.stretchez.raspberryPi

import retrofit2.Call
import retrofit2.http.GET

interface RaspberryAPI {
    @GET("api/users")
    fun fetchContents(): Call<List<RaspberryItem>>
}