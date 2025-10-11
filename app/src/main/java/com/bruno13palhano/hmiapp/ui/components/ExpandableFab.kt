package com.bruno13palhano.hmiapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bruno13palhano.hmiapp.R
import com.bruno13palhano.hmiapp.ui.theme.HMIAppTheme

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <T> ExpandableFab(
    expanded: Boolean,
    items: Map<T, ExpandableFabButtons>,
    onClick: () -> Unit,
    onOptionSelected: (T) -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        label = "fabRotation",
    )
    Box(
        modifier = Modifier.padding(16.dp),
        contentAlignment = Alignment.BottomEnd,
    ) {
        Column(horizontalAlignment = Alignment.End) {
            AnimatedVisibility(
                modifier = Modifier.padding(bottom = 12.dp),
                visible = expanded,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    items.forEach { item ->
                        SmallFabButton(
                            icon = item.value.icon,
                            label = item.value.label,
                            contentDescription = item.value.contentDescription,
                            onClick = {
                                onOptionSelected(item.key)
                            },
                        )
                    }
                }
            }

            FloatingActionButton(onClick = onClick) {
                var icon = Icons.Outlined.Add
                var description = stringResource(id = R.string.add_button)

                if (expanded) {
                    icon = Icons.Outlined.Close
                    description = stringResource(id = R.string.close_button)
                }

                Icon(
                    modifier = Modifier.rotate(rotation),
                    imageVector = icon,
                    contentDescription = description,
                )
            }
        }
    }
}

@Composable
private fun SmallFabButton(
    icon: ImageVector,
    label: String,
    contentDescription: String? = null,
    onClick: () -> Unit,
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(icon, contentDescription = contentDescription) },
        text = { Text(text = label) },
        modifier = Modifier.wrapContentWidth()
            .height(40.dp),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    )
}

data class ExpandableFabButtons(
    val icon: ImageVector,
    val label: String,
    val contentDescription: String? = null,
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ExpandableFabPreview() {
    HMIAppTheme {
        Scaffold(
            floatingActionButton = {
                ExpandableFab(
                    expanded = false,
                    items = mapOf(
                        "Widgets" to ExpandableFabButtons(
                            icon = Icons.Outlined.Widgets,
                            label = "Widgets",
                        ),
                        "Environment" to ExpandableFabButtons(
                            icon = Icons.Outlined.Add,
                            label = "Environment",
                        ),
                    ),
                    onClick = {},
                    onOptionSelected = {},
                )
            },
        ) {
            Column(modifier = Modifier.padding(it)) {
            }
        }
    }
}
