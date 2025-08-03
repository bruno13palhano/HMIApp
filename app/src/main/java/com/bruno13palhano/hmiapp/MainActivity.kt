package com.bruno13palhano.hmiapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.rememberNavBackStack
import com.bruno13palhano.hmiapp.ui.navigation.AppNavigation
import com.bruno13palhano.hmiapp.ui.navigation.Dashboard
import com.bruno13palhano.hmiapp.ui.navigation.DrawerMenu
import com.bruno13palhano.hmiapp.ui.theme.HMIAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
    val navBackStack = rememberNavBackStack(Dashboard)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var gesturesEnabled by rememberSaveable { mutableStateOf(true) }

    DrawerMenu(
        drawerState = drawerState,
        navBackStack = navBackStack,
        gesturesEnabled = gesturesEnabled
    ) {
        val scope = rememberCoroutineScope()

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            AppNavigation(
                modifier = Modifier.padding(innerPadding),
                navBackStack = navBackStack,
                onGesturesEnable = { enable -> gesturesEnabled = enable },
                onMenuIconClick = {
                    scope.launch {
                        drawerState.open()
                    }
                }
            )
        }
    }
}
