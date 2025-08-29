package infrastructure.api

import application.config.AppConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

class APIClient {
    private val client = OkHttpClient()

    fun makeRequest(url: String, method: String, requestBody: RequestBody? = null): Response {
        val requestBuilder = Request.Builder()
            .url("${AppConfig.supabaseUrl}/$url")
            .addHeader("apikey", AppConfig.supabaseKey)
            .addHeader("Authorization", "Bearer ${AppConfig.supabaseKey}")

        when (method) {
            "GET" -> requestBuilder.get()
            "POST" -> requestBody?.let { requestBuilder.post(it) }
            "PATCH" -> requestBody?.let { requestBuilder.patch(it) }
            "DELETE" -> requestBuilder.delete(requestBody)
        }

        return client.newCall(requestBuilder.build()).execute()
    }
}