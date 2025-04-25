package com.example.momentreely.network

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation
import java.io.IOException
import java.util.concurrent.TimeUnit

class AuthInterceptor : Interceptor{
    private val auth: FirebaseAuth = Firebase.auth

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val newRequestBuilder = request.newBuilder()

        // Log the request URL, body and headers
        Log.d("AuthInterceptor", "Request URL: ${request.url}")
        Log.d("AuthInterceptor", "Request Body: ${request.body}")
        Log.d("AuthInterceptor", "Request Headers: ${request.headers}")

        // This is to skip the step of adding authentication header for APIs that cannot have
        // auth, like SignUp
        val invocation = request.tag(Invocation::class.java)

        if(invocation?.method()?.isAnnotationPresent(NoAuth::class.java) == true) {
            return chain.proceed(request)
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            try {
                val tokenResult = Tasks.await(currentUser.getIdToken(false), 5, TimeUnit.SECONDS)
                val token = tokenResult.token
                if (!token.isNullOrEmpty()) {
                    Log.d("AuthInterceptor", "Token: $token")
                    newRequestBuilder.addHeader("Authorization", token)
                }
            } catch (e: Exception) {
                e.printStackTrace() // Log if needed
            }
        }
        return chain.proceed(newRequestBuilder.build())
    }
}