package com.example.momentreely.viewmodel

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.momentreely.MomentReelyApplication
import com.example.momentreely.data.DataRepository
import com.example.momentreely.model.Friend
import com.example.momentreely.model.Post
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File

sealed interface ChatUiState {
    data object Loading : ChatUiState
    data object Error : ChatUiState
    data object None : ChatUiState
}

class ChatViewModel(val repository: DataRepository,val  friend: Friend) : ViewModel() {
    var chatUiState: ChatUiState by mutableStateOf(ChatUiState.Loading)

    var posts by  mutableStateOf<List<Post>>(emptyList())

    init {
        getPosts()
    }

    fun getPosts()  {
        chatUiState = ChatUiState.Loading
        viewModelScope.launch {
            try {
                posts = repository.getUserPosts(friend.userId)
                chatUiState = ChatUiState.None
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error getting posts", e)
                chatUiState = ChatUiState.Error
            }
        }
    }


    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    fun sendPost(image : File) {
        chatUiState = ChatUiState.Loading
        viewModelScope.launch {
            try {

                val newPost = repository.sendPost(friend.userId, image)
                if(newPost != null) {
                    posts = posts + newPost
                }
                chatUiState = ChatUiState.None
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending post", e)
                chatUiState = ChatUiState.Error
            }
        }
    }

    companion object {
        fun provideFactory(friend: Friend): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as MomentReelyApplication
                val repository = application.container.dataRepository
                ChatViewModel(repository, friend)
            }
        }

    }

}