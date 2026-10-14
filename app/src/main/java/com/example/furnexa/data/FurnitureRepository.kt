package com.example.furnexa.data

import com.example.furnexa.SupabaseConfig
import io.github.jan.supabase.postgrest.from

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
}
