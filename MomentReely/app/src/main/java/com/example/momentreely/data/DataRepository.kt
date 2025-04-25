package com.example.momentreely.data

import android.util.Log
import com.example.momentreely.model.ApiResponse
import com.example.momentreely.model.Post
import com.example.momentreely.model.User
import com.example.momentreely.network.ApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class DataRepository(private val apiService: ApiService) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun getUserData(): User? {
        try {
            val userId: String =
                auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
            return withContext(Dispatchers.IO) {
                val apiRes = apiService.getUserData(ApiService.UserDataRequest(userId))
                if (apiRes.isSuccessful) {
                    return@withContext apiRes.body()
                } else {
                    Log.e(
                        "UserRepository",
                        "Error getting user data: ${apiRes.errorBody()?.string()}"
                    )
                    return@withContext null
                }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting user data", e)
        }
        return null
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun getUserPosts(friendId: String): List<Post> {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not authenticated")
        return withContext(Dispatchers.IO) {
            val apiRes = apiService.getUserPosts(ApiService.GetPostsRequest(userId, friendId))
            if (apiRes.isSuccessful) {
                return@withContext apiRes.body() ?: emptyList()
            } else {
                Log.e("UserRepository", "Error getting user posts: ${apiRes.errorBody()?.string()}")
                return@withContext emptyList()
            }
        }
    }

    suspend fun login(email: String, password: String): ApiResponse {
        try {
            val authResult = auth.signInWithEmailAndPassword(email, password).addOnFailureListener {
                Log.e("UserRepository", "User not authenticated")
            }.await()
            val userId = authResult.user?.uid ?: throw Exception("User not authenticated.")
//
            val deviceId = FirebaseMessaging.getInstance().token.await()
            return withContext(Dispatchers.IO) {
                val apiRes =
                    apiService.registerUserDevice(ApiService.UserDeviceData(deviceId, userId))
                if (apiRes.isSuccessful) {
                    return@withContext apiRes.body() ?: ApiResponse(false)
                } else {
                    Log.e("UserRepository", "Error logging in: ${apiRes.errorBody()?.string()}")
                    return@withContext ApiResponse(
                        false,
                        message = apiRes.errorBody()?.string() ?: "Unknown error"
                    )
                }
//                return@withContext ApiResponse(true)
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "User not authenticated", e)
        }
        return ApiResponse(false)
    }

    suspend fun signUp(email: String, password: String, name: String): ApiResponse {
        try {
            val cleanEmail = email.trim()
            // Firebase signup
            Log.d("UserRepository", "Signing up user with email: $email")
            val authResult =
                auth.createUserWithEmailAndPassword(email, password).addOnFailureListener {
                    Log.e("UserRepository", "User not created")
                }.await()
            val userId = authResult.user?.uid ?: throw Exception("User not created")
            val deviceId: String = FirebaseMessaging.getInstance().token.await()

            // Call next API
            return withContext(Dispatchers.IO) {
                val apiRes =
                    apiService.signUp(ApiService.UserSignUpData(name, email, userId, deviceId))
                if (apiRes.isSuccessful) {
                    return@withContext apiRes.body() ?: ApiResponse(false)
                } else {
                    Log.e("UserRepository", "Error signing up: ${apiRes.errorBody()?.string()}")
                    return@withContext ApiResponse(
                        false,
                        message = apiRes.errorBody()?.string() ?: "Unknown error"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error signing up", e)
        }
        return ApiResponse(false)
    }

    suspend fun addFriends(friendEmail: String): ApiResponse {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not created")
            return withContext(Dispatchers.IO) {
                val apiRes =
                    apiService.addFriends(ApiService.AddUserFriendsData(friendEmail, userId))
                if (apiRes.isSuccessful) {
                    return@withContext apiRes.body() ?: ApiResponse(false)
                } else {
                    val errorBody = apiRes.errorBody()?.string()
                    Log.e("UserRepository", "Error adding friends: $errorBody")
                    return@withContext ApiResponse(false, errorBody ?: "Unknown error")
                }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error adding friends", e)
        }
        return ApiResponse(false)
    }

    suspend fun sendPost(friendId: String, image: File): Post? {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
                ?: throw IllegalStateException("User not authenticated")


            val imagePart = image.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("image", image.name, imagePart)


            withContext(Dispatchers.IO) {
                val apiRes = apiService.sendPost(
                    userId = userId.toRequestBody("text/plain".toMediaTypeOrNull()),
                    friendId = friendId.toRequestBody("text/plain".toMediaTypeOrNull()),
                    image = filePart
                )
                if (apiRes.isSuccessful) {
                    return@withContext apiRes.body()
                } else {
                    Log.e("UserRepository", "Error sending post: ${apiRes.errorBody()?.string()}")
                    return@withContext null
                }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error sending post", e)
        }
        return null
    }
}