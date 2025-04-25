package com.example.momentreely.view

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.momentreely.R
import com.example.momentreely.model.Friend
import com.example.momentreely.viewmodel.AddFriendUiState
import com.example.momentreely.viewmodel.HomeUiState
import com.example.momentreely.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier, onFriendChatSelected: (Friend) -> Unit, onSignOut: () -> Unit) {
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true) // ✅ Key line
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("MomentReely")
                },
                actions = {
                    IconButton(
                        onClick = {
                            homeViewModel.getUserData()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload"
                        )
                    }
                    IconButton(
                        onClick = {
                            homeViewModel.signOut()
                            onSignOut()
                        }
                    ) {
                        Image(
                            painter = painterResource(R.drawable.logout),
                            contentDescription = "Sign Out"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Render Bottom Sheet
                    homeViewModel.showBottomSheet()
                },
                containerColor = Color.White,
                contentColor = Color.Black
            ) {
                Icon(Icons.Filled.Add, "Add Friend")
            }
        },
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
        ) {
            when (homeViewModel.homeUiState) {
                is HomeUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is HomeUiState.Error -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("An Error Occurred :(")
                        }
                    }
                }

                is HomeUiState.None -> {
                    if (homeViewModel.user?.friends?.isEmpty() == true) {
                        item {
                            Text("Add Friends to share posts!")
                        }
                    }
                    homeViewModel.user?.friends?.forEach {
                        item {
                            FriendCard(
                                it,
                                onFriendChatSelected = {
                                    onFriendChatSelected(it)
                                }
                            )
                        }
                    }
//                    item {
//                        FriendCard(
//                            Friend("Name", "Test", "friend@mail.com"),
//                            onFriendChatSelected = {
//                                onFriendChatSelected(Friend("Name", "Test", "friend@mail.com"))
//                            }
//                        )
//                    }
                }
            }


        }
        if (homeViewModel.bottomSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = {
                    if (homeViewModel.addFriendUiState !is AddFriendUiState.Loading) {
                        coroutineScope.launch {
                            sheetState.hide()
                            homeViewModel.hideBottomSheet()
                        }
                    }
                },
                sheetState = sheetState
            ) {
                Column  (
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text("Enter Friend's Email")
                    OutlinedTextField(
                        onValueChange = {
                            homeViewModel.friendEmail = it
                        },
                        value = homeViewModel.friendEmail,
                        label = {
                            Text("Email")
                        },
                        modifier = Modifier.padding(bottom = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    Button(
                        onClick = {
                            homeViewModel.addFriend(homeViewModel.friendEmail)
                        }
                    ) {
                        when(homeViewModel.addFriendUiState) {
                            is AddFriendUiState.Loading -> {
                                CircularProgressIndicator()
                            }
                            else -> {
                                Text("Add Friend")
                            }
                        }
                    }
                }
                LaunchedEffect(homeViewModel.addFriendUiState) {
                    when (val state = homeViewModel.addFriendUiState) {
                        is AddFriendUiState.Success -> {
                            Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                            sheetState.hide()
                            homeViewModel.hideBottomSheet()
                            homeViewModel.getUserData()
                        }
                        is AddFriendUiState.Error -> {
                            Toast.makeText(context, "An Error Occurred", Toast.LENGTH_SHORT).show()
                            sheetState.hide()
                            homeViewModel.hideBottomSheet()
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun FriendCard(friend: Friend, onFriendChatSelected: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .fillMaxSize(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.Black),
        onClick = onFriendChatSelected

    ) {
        Text(friend.name, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
        Text(friend.email, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onFriendChatSelected = {

        },
        onSignOut = {}
    )
}