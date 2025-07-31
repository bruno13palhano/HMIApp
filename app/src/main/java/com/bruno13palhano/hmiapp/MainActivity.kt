package com.bruno13palhano.hmiapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bruno13palhano.hmiapp.ui.navigation.AppNavigation
import com.bruno13palhano.hmiapp.ui.theme.HMIAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HMIAppTheme {
                HmiApp()
            }
        }
    }
}

@Composable
fun HmiApp() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        AppNavigation(modifier = Modifier.padding(innerPadding))
    }
}
