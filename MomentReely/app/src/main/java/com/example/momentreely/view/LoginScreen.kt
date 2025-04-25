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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.momentreely.ui.theme.MomentReelyTheme
import com.example.momentreely.viewmodel.LoginScreenState
import com.example.momentreely.viewmodel.LoginViewModel

@Composable
fun LoginScreen(navToSignUp: () -> Unit, onLoginSuccess: () -> Unit, modifier: Modifier = Modifier) {
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
    Scaffold (modifier = Modifier.fillMaxSize()){
        Column(
            modifier = modifier.padding(it).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (loginViewModel.loginState) {
                is LoginScreenState.None -> {
                    Text("Login to continue!")
                    OutlinedTextField(
                        onValueChange = {
                            loginViewModel.email = it
                        },
                        value = loginViewModel.email,
                        //            value = "",
                        label = {
                            Text("Email")
                        },
                        modifier = Modifier.padding(bottom = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    OutlinedTextField(
                        onValueChange = {
                            loginViewModel.password = it
                        },
                        value = loginViewModel.password,
                        //            value = "",
                        label = {
                            Text("Password")
                        },
                        modifier = Modifier.padding(top = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    Button(
                        onClick = {
                            loginViewModel.login()
                        },
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Login", modifier = Modifier.padding(horizontal = 8.dp))
                    }
                    Spacer(
                        modifier = Modifier
                    )
                    Text(
                        modifier = Modifier.padding(12.dp).clickable(
                            onClick = {
                                navToSignUp()
                            }
                        ),
                        text = "Sign Up")
                }

                is LoginScreenState.Loading -> {
                    CircularProgressIndicator()
                }

                is LoginScreenState.Error -> {
                    val context = LocalContext.current
                    Toast.makeText(context, "Login Failed", Toast.LENGTH_SHORT).show()
                    loginViewModel.resetScreen()
                }

                is LoginScreenState.Success -> {
                    val context = LocalContext.current
                    Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()

                    onLoginSuccess()
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MomentReelyTheme {
        Scaffold (modifier = Modifier.fillMaxSize()){
            LoginScreen(
                onLoginSuccess = {},
                navToSignUp = {},
                modifier = Modifier.padding(it))
        }
    }
}