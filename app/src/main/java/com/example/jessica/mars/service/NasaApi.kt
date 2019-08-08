package com.example.jessica.mars.service

import com.example.jessica.mars.models.PhotoList
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Call


interface NasaApi {
    @GET("mars-photos/api/v1/rovers/{rover}/photos?sol=1000&api_key=tcm6yZNYShvDgQmQw5tPA6wSHBCOLfkTC7U0ytKG")
    fun getPhotos(@Path("rover") rover: String) : Call<PhotoList>
}