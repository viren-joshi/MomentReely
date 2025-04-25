package com.example.momentreely.view

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.momentreely.viewmodel.SignUpScreenState
import com.example.momentreely.viewmodel.SignUpViewModel

@Composable
fun SignUpScreen(navToLogin: ()-> Unit, onSignUpSuccess: () -> Unit, modifier: Modifier = Modifier) {
    val signUpViewModel: SignUpViewModel = viewModel(factory = SignUpViewModel.Factory)

    Scaffold () {
        Column(
            modifier = modifier.padding(it).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (signUpViewModel.signUpState) {
                is SignUpScreenState.None -> {
                    Text("Sign Up to continue!")
                    OutlinedTextField(
                        onValueChange = {
                            signUpViewModel.name = it
                        },
                        value = signUpViewModel.name,
                        label = {
                            Text("Name")
                        },
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    OutlinedTextField(
                        onValueChange = {
                            signUpViewModel.email = it
                        },
                        value = signUpViewModel.email,
                        //            value = "",
                        label = {
                            Text("Email")
                        },
                        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    OutlinedTextField(
                        onValueChange = {
                            signUpViewModel.password = it
                        },
                        value = signUpViewModel.password,
                        //            value = "",
                        label = {
                            Text("Password")
                        },
                        modifier = Modifier.padding(top = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    Button(
                        onClick = {
                            signUpViewModel.signUp()
                        },
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Sign Up", modifier = Modifier.padding(horizontal = 8.dp))
                    }
                    Spacer(
                        modifier = Modifier
                    )
                    Text(
                        modifier = Modifier.padding(12.dp).clickable(
                            onClick = {
                                navToLogin()
                            }
                        ),
                        text = "Login")
                }

                is SignUpScreenState.Loading -> {
                    CircularProgressIndicator()
                }

                is SignUpScreenState.Error -> {
                    val context = LocalContext.current
                    Toast.makeText(context, "SignUp Failed", Toast.LENGTH_SHORT).show()
                    signUpViewModel.resetScreen()
                }

                is SignUpScreenState.Success -> {
                    val context = LocalContext.current
                    Toast.makeText(context, "SignUp Successful", Toast.LENGTH_SHORT).show()
                    onSignUpSuccess()
                }
            }
    }
    }
}