package com.example.momentreely

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.momentreely.ui.theme.MomentReelyTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private var initScreen = "login"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val splashScreen = installSplashScreen()

        splashScreen.setKeepOnScreenCondition {true}

        auth = Firebase.auth

        // Logic
        CoroutineScope(Dispatchers.Main).launch {
            val currentUser = auth.currentUser
            if(currentUser != null) {
                initScreen = "home"
            }
            splashScreen.setKeepOnScreenCondition {false}
        }


        setContent {
            MomentReelyTheme {
                MomentReelyApp(initScreen)
            }
        }
    }


}

@Composable
fun MomentReelyApp(initScreen: String) {
    MomentReelyTheme {
        val navController = rememberNavController()
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = initScreen,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(route = "login") {
                    LoginScreen()
                }
                composable(route = "home") {
                    HomeScreen()
                }
            }
        }


    }
}

@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("This is the Login Screen")
    }
}

@Composable
fun SignUpScreen(modifier: Modifier = Modifier) {
    
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("This is the Home Screen")
    }
}


//@Composable
//fun SplashScreen(renderNextScreen: (String) -> Unit) {
//
//    Box(
//        contentAlignment = Alignment.Center,
//        modifier = Modifier.fillMaxSize()
//    ) {
//        Image(
//            painter = painterResource(R.drawable.momentreely_logo),
//            contentDescription = "MomentReely Logo",
//            modifier = Modifier.clip(shape = CircleShape)
//        )
//    }
//    LaunchedEffect(key1 = true) {
//        // Logic to check for user login
//        delay(3000L)
//        renderNextScreen("login")
//    }
//}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MomentReelyTheme {
        MomentReelyApp("login")
    }
}