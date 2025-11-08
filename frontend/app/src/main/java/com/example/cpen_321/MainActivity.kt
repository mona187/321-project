package com.example.cpen_321

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.cpen_321.ui.navigation.AppNavGraph
import com.example.cpen_321.ui.theme.ProvideFontSizes
import com.example.cpen_321.ui.theme.ProvideSpacing
import com.example.cpen_321.ui.theme.Cpen321Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Cpen321Theme {
                Cpen321App()  // This stays the same
            }
        }
    }

//    // ADD THIS METHOD - it prevents navigation state from being saved
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        // Don't save navigation state - always start fresh at splash
//        outState.clear()
//    }
}

@Composable
fun Cpen321App() {
    ProvideSpacing {
        ProvideFontSizes {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}