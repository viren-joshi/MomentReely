package com.example.momentreely.network

import com.example.momentreely.model.ApiResponse
import com.example.momentreely.model.Post
import com.example.momentreely.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class NoAuth  // Marks endpoints that don't need auth

interface ApiService {

    @POST("userData")
    suspend fun getUserData(@Body request: UserDataRequest): Response<User?>
    data class UserDataRequest(
        val userId: String
    )

    @POST("getPosts")
    suspend fun getUserPosts(@Body request: GetPostsRequest): Response<List<Post>>

    data class GetPostsRequest(
        val userId: String,
        val friendId: String,
    )

    @POST("registerUserDevice")
    suspend fun registerUserDevice(@Body request: UserDeviceData): Response<ApiResponse>

    data class UserDeviceData(
        val deviceId: String,
        val userId: String
    )

    @NoAuth
    @POST("signUp")
    suspend fun signUp(@Body request: UserSignUpData) : Response<ApiResponse>

    data class UserSignUpData(
        val name: String,
        val email: String,
        val userId: String,
        val deviceId: String
    )

    @POST("addFriend")
    suspend fun addFriends(@Body request: AddUserFriendsData) : Response<ApiResponse>

    data class AddUserFriendsData(
        val friendEmail: String,
        val userId: String
    )

    @Multipart
    @POST("sendPost")
    suspend fun sendPost(@Part("userId") userId: RequestBody, @Part("friendId") friendId: RequestBody, @Part image : MultipartBody.Part ) : Response<Post>

//    data class SendPostData(
//        val userId: String,
//        val friendId: String,
//        val image: String
//    )
}