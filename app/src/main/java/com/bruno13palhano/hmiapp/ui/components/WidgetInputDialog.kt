package com.bruno13palhano.hmiapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bruno13palhano.hmiapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetInputDialog(
    label: String,
    endpoint: String,
    onLabelChange: (label: String) -> Unit,
    onEndpointChange: (endpoint: String) -> Unit,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.wrapContentWidth().wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(id = R.string.widget_configuration),
                )
                Spacer(modifier = Modifier.height(24.dp))
                CustomTextField(
                    value = label,
                    onValueChange = onLabelChange,
                    label = stringResource(id = R.string.label),
                    placeholder = stringResource(id = R.string.enter_label)
                )
                CustomTextField(
                    value = endpoint,
                    onValueChange = onEndpointChange,
                    label = stringResource(id = R.string.endpoint),
                    placeholder = stringResource(id = R.string.enter_endpoint)
                )

                Row {
                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                    ) {
                        Text(text = stringResource(id = R.string.cancel))
                    }

                    TextButton(
                        onClick = onConfirm,
                        modifier = Modifier
                    ) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
}
