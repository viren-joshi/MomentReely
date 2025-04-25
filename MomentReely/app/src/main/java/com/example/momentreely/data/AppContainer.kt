package com.example.momentreely.data

import com.example.momentreely.network.ApiService
import com.example.momentreely.network.AuthInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class AppContainer {

//    private val baseUrl = "http://10.0.2.2:6000";
    private val baseUrl = "http://m-stack-ALB-3SBf3LWrSqaT-1325104438.us-east-1.elb.amazonaws.com:80"


    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .connectTimeout(240,TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val dataRepository: DataRepository by lazy {
        DataRepository(api)
    }
}