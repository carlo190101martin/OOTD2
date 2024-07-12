package com.example.ootd2

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MyFirebaseInstanceIDService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        // Send token to your app server.
        Log.d("FCM", "Sending token to server: $token")

        val url = "https://your-server-url.com/register-token"
        val jsonBody = JSONObject().apply {
            put("token", token)
        }
        val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FCM", "Failed to send token to server", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("FCM", "Token sent to server successfully")
                } else {
                    Log.e("FCM", "Failed to send token to server. Response code: ${response.code}")
                }
            }
        })
    }
}
