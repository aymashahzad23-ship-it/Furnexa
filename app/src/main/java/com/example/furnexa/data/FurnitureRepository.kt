package com.example.furnexa.data

import com.example.furnexa.SupabaseConfig
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FurnitureInput(
    @SerialName("name") val name: String,
    @SerialName("category") val category: String,
    @SerialName("description") val description: String,
    @SerialName("price_pkr") val pricePkr: Int,
    @SerialName("width_cm") val widthCm: Double,
    @SerialName("depth_cm") val depthCm: Double,
    @SerialName("height_cm") val heightCm: Double,
    @SerialName("style") val style: String,
    @SerialName("colour") val colour: String,
    @SerialName("room_type") val roomType: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("model_3d_url") val model3dUrl: String?
)

class FurnitureRepository {
    private val postgrest = SupabaseConfig.client.from("furniture")

    suspend fun getFurnitureList(): Result<List<Furniture>> {
        return runCatching {
            postgrest.select().decodeList<Furniture>()
        }
    }

    suspend fun getFurnitureById(id: Int): Result<Furniture?> {
        return runCatching {
            postgrest.select {
                filter {
                    eq("id", id)
                }
            }.decodeSingleOrNull<Furniture>()
        }
    }

    suspend fun insertFurniture(item: FurnitureInput): Result<Unit> {
        return runCatching {
            postgrest.insert(item)
        }
    }

    suspend fun updateFurniture(id: Int, item: FurnitureInput): Result<Unit> {
        return runCatching {
            postgrest.update(item) {
                filter {
                    eq("id", id)
                }
            }
        }
    }

    suspend fun deleteFurniture(id: Int): Result<Unit> {
        return runCatching {
            postgrest.delete {
                filter {
                    eq("id", id)
                }
            }
        }
    }
}
