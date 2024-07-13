package com.example.catalist.cats.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class CatApiModel (
    val id: String,
    val name: String,
    val temperament: String,
    val origin: String,
    val description: String,
    val life_span: String,
    val alt_names: String? = "Nema alternativnog imena",
    @SerialName("adaptability") val adaptability: Int,
    @SerialName("affection_level") val affection_level: Int,
    @SerialName("stranger_friendly") val stranger_friendly: Int,
    @SerialName("dog_friendly") val dog_friendly: Int,
    @SerialName("energy_level") val energy_level: Int,
    @SerialName("social_needs") val social_needs: Int,
    @SerialName("health_issues") val health_issues: Int,
    val intelligence: Int,
    val rare: Int,
    val wikipedia_url: String?="ne postoji",
    var reference_image_id: String?=null,
    val weight: Weight,
    )

@Serializable
data class PhotoApiModel(
    @SerialName("id") val photoId: String,
    val url: String,
    val width: Int,
    var height: Int
)

@Serializable
data class Weight(
    val imperial: String,
    val metric : String
)
