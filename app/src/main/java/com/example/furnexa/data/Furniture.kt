package com.example.furnexa.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Furniture(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("category")
    val category: String,
    @SerialName("description")
    val description: String,
    @SerialName("price_pkr")
    val pricePkr: Int,
    @SerialName("width_cm")
    val widthCm: Double = 0.0,
    @SerialName("depth_cm")
    val depthCm: Double = 0.0,
    @SerialName("height_cm")
    val heightCm: Double = 0.0,
    @SerialName("style")
    val style: String = "",
    @SerialName("colour")
    val colour: String = "",
    @SerialName("room_type")
    val roomType: String = "",
    @SerialName("image_url")
    val imageUrl: String = "",
    @SerialName("model_3d_url")
    val model3dUrl: String? = null
)
