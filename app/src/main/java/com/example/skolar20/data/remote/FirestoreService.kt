
package com.example.skolar20.data.model.remote


import com.google.gson.Gson
import com.google.gson.JsonObject
import com.example.skolar20.BuildConfig
import com.example.skolar20.data.model.Tutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor

object FirestoreService {
    private val client: OkHttpClient by lazy {
        val log = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        OkHttpClient.Builder().addInterceptor(log).build()
    }

    // Reads /tutors via Firestore REST. Requires test-mode reads or appropriate rules.
    suspend fun fetchTutors(): List<Tutor> = withContext(Dispatchers.IO) {
        val projectId = BuildConfig.FIREBASE_PROJECT_ID
        val apiKey = BuildConfig.FIREBASE_WEB_API_KEY

        val base = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/tutors"
        val url = if (apiKey.isNotBlank()) "$base?key=$apiKey" else base

        val req = Request.Builder().url(url).get().build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}: ${resp.message}")
            val body = resp.body?.string() ?: throw Exception("Empty response body")
            val json = Gson().fromJson(body, JsonObject::class.java)

            val docs = json.getAsJsonArray("documents") ?: return@use emptyList<Tutor>()
            docs.map { el ->
                val doc = el.asJsonObject
                val namePath = doc.get("name").asString                   // .../tutors/{docId}
                val tutorId = namePath.substringAfterLast("/")
                val fields = doc.getAsJsonObject("fields")

                fun str(k: String) = fields.getAsJsonObject(k)?.get("stringValue")?.asString ?: ""
                fun dbl(k: String) = fields.getAsJsonObject(k)?.get("doubleValue")?.asDouble
                    ?: fields.getAsJsonObject(k)?.get("integerValue")?.asString?.toDoubleOrNull() ?: 0.0
                fun arr(k: String) = fields.getAsJsonObject(k)
                    ?.getAsJsonObject("arrayValue")
                    ?.getAsJsonArray("values")
                    ?.mapNotNull { it.asJsonObject.get("stringValue")?.asString } ?: emptyList()

                Tutor(
                    tutorId = tutorId,
                    name = str("name"),
                    expertise = arr("expertise"),
                    qualifications = str("qualifications"),
                    rate = dbl("rate"),
                    location = str("location")
                )
            }
        }
    }
}
