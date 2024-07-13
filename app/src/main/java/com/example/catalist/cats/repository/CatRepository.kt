package com.example.catalist.cats.repository

import com.example.catalist.cats.api.CatApi
import com.example.catalist.cats.api.model.CatApiModel
import com.example.catalist.cats.api.model.PhotoApiModel
import rs.edu.raf.rma6.networking.retrofit

object CatRepository {

    private val catApi: CatApi = retrofit.create(CatApi::class.java)

    private val photoAPi: CatApi = retrofit.create(CatApi::class.java)


    suspend fun fetchAllCats(): List<CatApiModel> {
        val users = catApi.getAllCats()

        return users
    }

    suspend fun fetchCatsByQuery(query: String): List<CatApiModel> {
        val cats = catApi.searchBreeds(query = query)
        // Simulacija dohvatanja mačaka filtriranih prema upitu
        return cats.filter {
            it.name.contains(query, ignoreCase = true) ||
                    (it.alt_names?.contains(query, ignoreCase = true) ?: false) ||
                    it.temperament!!.contains(query, ignoreCase = true)
        }
    }

    suspend fun getCatPhoto(cat_id: String) : List<PhotoApiModel>{
        return photoAPi.getPhoto(cat_id = cat_id)
    }

    suspend fun getCatById(id: String) :CatApiModel{
        val cat= catApi.getCat(catId = id)

        return cat;
    }

}