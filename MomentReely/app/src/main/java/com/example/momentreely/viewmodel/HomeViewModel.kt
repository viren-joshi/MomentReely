package com.example.momentreely.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.momentreely.MomentReelyApplication
import com.example.momentreely.data.DataRepository
import com.example.momentreely.model.User
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Error : HomeUiState
    data object None : HomeUiState
}

sealed interface AddFriendUiState {
    data object Loading : AddFriendUiState
    data object Error : AddFriendUiState
    data object None : AddFriendUiState
    data class Success(val message: String) : AddFriendUiState
}

class HomeViewModel(val repository: DataRepository) : ViewModel() {
    var homeUiState: HomeUiState by mutableStateOf(HomeUiState.Loading)
    private set

    var addFriendUiState: AddFriendUiState by mutableStateOf(AddFriendUiState.None)
    private set

    var user by mutableStateOf<User?>(null)
    private set

    var bottomSheetVisible by mutableStateOf(false)
    private set

    var friendEmail by mutableStateOf("")



    init {
        // Call Get User's friends
        getUserData()
    }

    fun getUserData() {
        homeUiState = HomeUiState.Loading
        viewModelScope.launch {
            user = repository.getUserData()
            homeUiState = if (user == null) {
                HomeUiState.Error
            } else {
                HomeUiState.None
            }
        }
    }

    fun showBottomSheet() {
        bottomSheetVisible = true
    }

    fun hideBottomSheet() {
        bottomSheetVisible  = false
    }

    fun addFriend(friendEmail:String) {
        addFriendUiState = AddFriendUiState.Loading
        viewModelScope.launch {
            val response = repository.addFriends(friendEmail)
            addFriendUiState = if(response.success) {
                AddFriendUiState.Success(response.message ?: "Friend Added")
            } else {
                AddFriendUiState.Error
            }
        }

    }

    fun signOut() {
        repository.signOut()
    }



    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MomentReelyApplication)
                HomeViewModel(application.container.dataRepository)
            }
        }
    }
}