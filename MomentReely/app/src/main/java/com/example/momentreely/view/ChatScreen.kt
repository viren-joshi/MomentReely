package com.example.momentreely.view

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.momentreely.model.Friend
import com.example.momentreely.viewmodel.ChatUiState
import com.example.momentreely.viewmodel.ChatViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(friend: Friend) {

    val chatViewModel: ChatViewModel = viewModel(factory = ChatViewModel.provideFactory(friend))

    val context = LocalContext.current

    val photoFile = remember {
        File.createTempFile("temp_photo", ".jpg", context.cacheDir).apply {
//            deleteOnExit() // ensures file is not kept
        }
    }

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        photoFile
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            chatViewModel.sendPost(photoFile)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launcher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text("MomentReely")
                },
                actions = {
                    IconButton(
                        onClick = {
                            chatViewModel.getPosts()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        launcher.launch(uri)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Send,
                    contentDescription = "Send A Photo"
                )
            }
        }
    ) {
        LazyColumn (
            modifier = Modifier.padding(it)
        ) {
            when(chatViewModel.chatUiState) {
                ChatUiState.Loading -> {
                    item {
                        CircularProgressIndicator()
                    }
                }
                ChatUiState.Error -> {
                    item {
                        Text("An Error Occurred :(")
                    }
                }
                ChatUiState.None -> {
                    items(chatViewModel.posts.size) {
                        AsyncImage(
                            model = chatViewModel.posts[it].image,
                            contentDescription = "Post Image",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

}
