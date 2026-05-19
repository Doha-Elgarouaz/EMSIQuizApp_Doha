package com.doha.emsiquizapp

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Client Supabase global.
 * Utilisation de 'by lazy' pour éviter un crash au démarrage si l'URL est invalide.
 */
val supabase: SupabaseClient by lazy {
    createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }
}
