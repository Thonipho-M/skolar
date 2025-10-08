package com.example.skolar20.data.remote

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.example.skolar20.BuildConfig
import com.example.skolar20.data.model.Booking
import com.example.skolar20.data.model.BookingCreate
import com.example.skolar20.data.model.Tutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.time.Instant

object FirestoreService {
    private val gson = Gson()

    private val client: OkHttpClient by lazy {
        val log = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        OkHttpClient.Builder().addInterceptor(log).build()
    }

    // ---------- Tutors (existing) ----------
    suspend fun fetchTutors(): List<Tutor> = withContext(Dispatchers.IO) {
        val base = "https://firestore.googleapis.com/v1/projects/${BuildConfig.FIREBASE_PROJECT_ID}/databases/(default)/documents/tutors"
        val url = if (BuildConfig.FIREBASE_WEB_API_KEY.isNotBlank()) "$base?key=${BuildConfig.FIREBASE_WEB_API_KEY}" else base

        val req = Request.Builder().url(url).get().build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}: ${resp.message}")
            val body = resp.body?.string() ?: throw Exception("Empty response body")
            val json = gson.fromJson(body, JsonObject::class.java)
            val docs = json.getAsJsonArray("documents") ?: return@use emptyList<Tutor>()
            docs.map { el ->
                val doc = el.asJsonObject
                val namePath = doc.get("name").asString
                val tutorId = namePath.substringAfterLast("/")
                val fields = doc.getAsJsonObject("fields")
                fun str(k: String) = fields.getAsJsonObject(k)?.get("stringValue")?.asString ?: ""
                fun dbl(k: String) = fields.getAsJsonObject(k)?.get("doubleValue")?.asDouble
                    ?: fields.getAsJsonObject(k)?.get("integerValue")?.asString?.toDoubleOrNull() ?: 0.0
                fun arr(k: String) = fields.getAsJsonObject(k)?.getAsJsonObject("arrayValue")
                    ?.getAsJsonArray("values")?.mapNotNull { it.asJsonObject.get("stringValue")?.asString } ?: emptyList()

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

    // ---------- Bookings (new) ----------
    private fun bookingToFields(b: BookingCreate): JsonObject {
        fun s(v: String) = JsonObject().apply { addProperty("stringValue", v) }
        fun ts(instant: Instant) = JsonObject().apply { addProperty("timestampValue", instant.toString()) }

        return JsonObject().apply {
            add("userId", s(b.userId))
            add("tutorId", s(b.tutorId))
            b.tutorName?.let { add("tutorName", s(it)) }
            add("subject", s(b.subject))
            add("bookingTime", ts(b.bookingTime)) // RFC3339 UTC
            add("status", s("requested"))
            b.notes?.let { add("notes", s(it)) }
        }
    }

    suspend fun createBooking(b: BookingCreate, idToken: String): String = withContext(Dispatchers.IO) {
        val base = "https://firestore.googleapis.com/v1/projects/${BuildConfig.FIREBASE_PROJECT_ID}/databases/(default)/documents/bookings"
        val url = base + if (BuildConfig.FIREBASE_WEB_API_KEY.isNotBlank()) "?key=${BuildConfig.FIREBASE_WEB_API_KEY}" else ""

        val payload = JsonObject().apply { add("fields", bookingToFields(b)) }
        val req = Request.Builder()
            .url(url)
            .post(gson.toJson(payload).toRequestBody("application/json; charset=utf-8".toMediaType()))
            .header("Authorization", "Bearer $idToken")
            .build()

        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("Create failed: HTTP ${resp.code} ${resp.message}")
            val body = resp.body?.string() ?: throw Exception("Empty response")
            val json = gson.fromJson(body, JsonObject::class.java)
            json.get("name")?.asString?.substringAfterLast("/") ?: "(unknown-id)"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchBookingsForUser(userId: String, idToken: String?): List<Booking> =
        withContext(Dispatchers.IO) {
            val parent = "projects/${BuildConfig.FIREBASE_PROJECT_ID}/databases/(default)/documents"
            val url =
                "https://firestore.googleapis.com/v1/projects/${BuildConfig.FIREBASE_PROJECT_ID}/databases/(default)/documents:runQuery" +
                        if (BuildConfig.FIREBASE_WEB_API_KEY.isNotBlank()) "?key=${BuildConfig.FIREBASE_WEB_API_KEY}" else ""

            // Structured query: filter by userId (no orderBy -> no composite index needed)
            val query = JsonObject().apply {
                addProperty("parent", parent)
                add("structuredQuery", JsonObject().apply {
                    add("from", JsonArray().apply {
                        add(JsonObject().apply { addProperty("collectionId", "bookings") })
                    })
                    add("where", JsonObject().apply {
                        add("fieldFilter", JsonObject().apply {
                            add("field", JsonObject().apply { addProperty("fieldPath", "userId") })
                            addProperty("op", "EQUAL")
                            add("value", JsonObject().apply { addProperty("stringValue", userId) })
                        })
                    })
                    // NOTE: no "orderBy" here to avoid composite index requirement
                })
            }

            val reqBuilder = Request.Builder()
                .url(url)
                .post(gson.toJson(query).toRequestBody("application/json; charset=utf-8".toMediaType()))
            if (idToken != null) reqBuilder.header("Authorization", "Bearer $idToken")
            val req = reqBuilder.build()

            client.newCall(req).execute().use { resp ->
                val body = resp.body?.string()
                if (!resp.isSuccessful) {
                    // Show full body to reveal Firestore’s helpful error/index link
                    throw Exception("Query failed: HTTP ${resp.code}: ${body ?: resp.message}")
                }

                val arr = gson.fromJson(body, JsonArray::class.java)
                val list = arr.mapNotNull { el ->
                    val doc = el.asJsonObject.getAsJsonObject("document") ?: return@mapNotNull null
                    val name = doc.get("name").asString
                    val id = name.substringAfterLast("/")
                    val f = doc.getAsJsonObject("fields")
                    fun s(k: String) = f.getAsJsonObject(k)?.get("stringValue")?.asString
                    @RequiresApi(Build.VERSION_CODES.O)
                    fun ts(k: String) = f.getAsJsonObject(k)?.get("timestampValue")?.asString?.let { Instant.parse(it) }
                    Booking(
                        bookingId = id,
                        userId = s("userId") ?: "",
                        tutorId = s("tutorId") ?: "",
                        tutorName = s("tutorName"),
                        subject = s("subject") ?: "",
                        bookingTime = ts("bookingTime") ?: Instant.EPOCH,
                        status = s("status") ?: "requested",
                        notes = s("notes")
                    )
                }

                // Client-side sort (newest first) to replace the removed orderBy
                list.sortedByDescending { it.bookingTime }
            }
        }

}
