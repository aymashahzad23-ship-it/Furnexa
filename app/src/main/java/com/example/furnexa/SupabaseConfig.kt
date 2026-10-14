package com.example.furnexa

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseConfig {
    /**
     * Supabase Project URL for Furnexa.
     */
    const val SUPABASE_URL = "https://rbhdeevpqukrbrhfojlm.supabase.co"

    /**
     * TODO: Add your Supabase publishable/anon key here or via local configuration.
     * Do not use service-role or secret keys.
     */
    const val SUPABASE_ANON_KEY = "sb_publishable_wI5RzjIRHzlz53ybsGou_A_4Pb82TfC"

    val client by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
}
