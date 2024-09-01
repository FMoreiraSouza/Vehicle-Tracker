package network

import constants.ApiConstants.BASE
import constants.ApiConstants.KEY
import constants.ApiConstants.TOKEN
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okio.IOException

class ApiClient {

    val client = OkHttpClient()

    fun makeRequest(
        url: String,
        method: String,
        requestBody: RequestBody? = null
    ): Response {
        val requestBuilder = Request.Builder()
            .url("$BASE/$url")
            .addHeader("apikey", KEY)
            .addHeader("Authorization", "Bearer $TOKEN")

        when (method) {
            "GET" -> requestBuilder.get()
            "POST" -> requestBody?.let { requestBuilder.post(it) }
            "PATCH" -> requestBody?.let { requestBuilder.patch(it) }
            "DELETE" -> requestBuilder.delete(requestBody)
        }

        val request = requestBuilder.build()
        return client.newCall(request).execute()
    }
}
