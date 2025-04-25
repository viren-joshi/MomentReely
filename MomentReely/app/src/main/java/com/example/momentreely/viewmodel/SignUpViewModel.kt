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
import kotlinx.coroutines.launch

sealed interface SignUpScreenState {
    data object None: SignUpScreenState
    data object Error: SignUpScreenState
    data object Loading: SignUpScreenState
    data object Success: SignUpScreenState
}

class SignUpViewModel(private val repository: DataRepository) : ViewModel() {
    var signUpState: SignUpScreenState by mutableStateOf(SignUpScreenState.None)

    var email: String by mutableStateOf("")
    var password: String by mutableStateOf("")
    var name: String by mutableStateOf("")

    fun signUp() {
        signUpState = SignUpScreenState.Loading
        viewModelScope.launch {
            signUpState = try {
                if(repository.signUp(email = email, password = password, name = name).success) {
                    SignUpScreenState.Success
                } else {
                    SignUpScreenState.Error
                }
            } catch (e: Exception){
                SignUpScreenState.Error
            }
        }
    }

    fun resetScreen() {
        signUpState = SignUpScreenState.None
    }
    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MomentReelyApplication)
                SignUpViewModel(application.container.dataRepository)
            }
        }
    }
}