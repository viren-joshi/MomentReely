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


sealed interface LoginScreenState {
    data object None: LoginScreenState
    data object Error: LoginScreenState
    data object Loading: LoginScreenState
    data object Success: LoginScreenState
}

class LoginViewModel(private val repository: DataRepository) : ViewModel() {

    var loginState: LoginScreenState by mutableStateOf(LoginScreenState.None)
        private set
    var email: String by mutableStateOf("")
    var password: String by mutableStateOf("")

    fun login() {
        loginState = LoginScreenState.Loading
        viewModelScope.launch {
            loginState = try {
                if(repository.login(email, password).success) {
                    LoginScreenState.Success
                } else {
                    LoginScreenState.Error
                }
            } catch (e: Exception) {
                LoginScreenState.Error
            }
        }
    }

    fun resetScreen() {
        loginState = LoginScreenState.None
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MomentReelyApplication)
                LoginViewModel(application.container.dataRepository)
            }
        }
    }
}