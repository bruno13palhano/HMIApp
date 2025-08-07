package com.bruno13palhano.hmiapp.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.bruno13palhano.hmiapp.ui.navigation.Dashboard
import com.bruno13palhano.hmiapp.ui.navigation.Screen
import kotlinx.coroutines.launch

@Composable
fun DrawerMenu(
    modifier: Modifier = Modifier,
    currentKey: NavKey?,
    drawerState: DrawerState,
    navigateTo: (key: NavKey) -> Unit,
    gesturesEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val orientation = LocalConfiguration.current.orientation
    val items = listOf(Screen.DashboardScreen, Screen.SettingsScreen)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        drawerContent = {
            ModalDrawerSheet(
                drawerShape = RectangleShape,
                modifier = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    modifier
                        .fillMaxHeight()
                        .fillMaxWidth(.78F)
                        .consumeWindowInsets(WindowInsets.safeDrawing)
                } else {
                    modifier
                        .fillMaxHeight()
                        .consumeWindowInsets(WindowInsets.safeDrawing)

                },
            ) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Spacer(modifier = Modifier.height(12.dp))
                    items.forEach { screen ->
                        NavigationDrawerItem(
                            shape = RoundedCornerShape(0, 50, 50, 0),
                            icon = { Icon(imageVector = screen.icon, contentDescription = null) },
                            label = { Text(text = stringResource(id = screen.resourceId)) },
                            selected = currentKey == screen.key,
                            onClick = {
                                navigateTo(screen.key)
                                scope.launch {
                                    drawerState.close()
                                }
                            },
                            modifier = Modifier
                                .padding(top = 4.dp, bottom = 4.dp, end = 8.dp)
                                .height(52.dp),
                        )
                    }
                }
            }
        },
        content = content
    )
}

@Composable
fun <T> VertMenu(
    items: Map<T, String>,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onItemClick: (T) -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        items.forEach { item ->
            DropdownMenuItem(
                text = { Text(text = item.value) },
                onClick = {
                    onItemClick(item.key)
                    onDismissRequest()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DrawerPreview() {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(text = "Test") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(imageVector = Icons.Outlined.Menu, contentDescription = null)
                        }
                    }
                )
            }
        ) {
            DrawerMenu(
                modifier = Modifier.padding(it),
                currentKey = Dashboard,
                drawerState = DrawerState(DrawerValue.Open),
                navigateTo = {},
                gesturesEnabled = true
            ) {}
        }
}