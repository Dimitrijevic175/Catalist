package com.example.catalist.cats.api

import com.example.catalist.cats.api.model.CatApiModel
import com.example.catalist.cats.api.model.PhotoApiModel
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CatApi {

    @GET("breeds")
    suspend fun getAllCats(): List<CatApiModel>

    @GET("breeds/{id}")
    suspend fun getCat(
        @Path("id") catId: String,
    ): CatApiModel

    @GET("images/search?breeds_id")
    suspend fun getPhoto(
        @Query("id") cat_id: String,
    ): List<PhotoApiModel>

    @GET("breeds/search")
    suspend fun searchBreeds(
        @Query("q") query: String,
    ): List<CatApiModel>
}