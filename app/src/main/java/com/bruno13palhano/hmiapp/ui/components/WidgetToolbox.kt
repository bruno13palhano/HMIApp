package com.bruno13palhano.hmiapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bruno13palhano.core.model.WidgetType

@Composable
fun WidgetToolbox(onAdd: (WidgetType) -> Unit) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color.Gray)
            .padding(8.dp)
    ) {
        WidgetType.entries.forEach { type ->
            Button(
                onClick = { onAdd(type) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text(text = "+ ${type.name}")
            }
        }
    }
}
