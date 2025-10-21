package com.example.cpen_321.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun MainBottomBar(navController: NavController) {
            BottomAppBar(
                actions = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween){
                        IconButton(onClick = {
                            navController.navigate("home")
                        }) {
                            Icon(Icons.Filled.Home, contentDescription = "Localized description")
                        }
                        IconButton(onClick = {
                            navController.navigate("profile")
                        }) {
                            Icon(
                                Icons.Filled.AccountBox,
                                contentDescription = "Localized description",
                            )
                        }
                        IconButton(onClick = { /* do something */ }) {
                            Icon(
                                Icons.Filled.Groups,
                                contentDescription = "Localized description",
                            )
                        }
                        IconButton(onClick = { navController.navigate("settings")}) {
                            Icon(
                                Icons.Filled.SettingsSuggest,
                                contentDescription = "Localized description",
                            )
                        }
                    }

                }
            )
}