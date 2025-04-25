package com.example.momentreely

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.momentreely.model.Friend
import com.example.momentreely.ui.theme.MomentReelyTheme
import com.example.momentreely.view.ChatScreen
import com.example.momentreely.view.HomeScreen
import com.example.momentreely.view.LoginScreen
import com.example.momentreely.view.SignUpScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.gson.Gson
import java.net.URLDecoder
import java.net.URLEncoder


class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private var initScreen = "login"
    override fun onStart() {
        super.onStart()
        auth = Firebase.auth
        val currentUser = auth.currentUser
        if(currentUser != null) {
            initScreen = "home"
        }
    }

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()



        setContent {
            MomentReelyTheme {

                askNotificationPermission()

                MomentReelyApp(initScreen)
            }
        }
    }


}

@Composable
fun MomentReelyApp(initScreen: String) {
    MomentReelyTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = initScreen,
        ) {
            composable(route = "login") {
                LoginScreen(
                    navToSignUp = {
                        navController.navigate("signup")
                    },
                    onLoginSuccess = {
                        navController.navigate("home")
                    }
                )
            }
            composable(route = "home") {
                HomeScreen(
                    onSignOut = {
                        navController.navigate("login")
                    },
                    onFriendChatSelected = {
                        val gson = Gson()
                        navController.navigate("chat/${URLEncoder.encode(gson.toJson(it), "UTF-8")}")
                    }
                )
            }
            composable(route = "signup") {
                SignUpScreen(
                    onSignUpSuccess = {
                        navController.navigate("home")
                    },
                    navToLogin = {
                        navController.navigate("login")
                    }
                )
            }
            composable(
                route = "chat/{friend}",
                arguments = listOf(navArgument("friend") {
                        type = NavType.StringType
                    }
                )
            ) {
                val encodedJson = it.arguments?.getString("friend")
                val decodedJson = URLDecoder.decode(encodedJson, "UTF-8")
                val gson = Gson()
                ChatScreen(gson.fromJson(decodedJson, Friend::class.java))
            }
        }


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