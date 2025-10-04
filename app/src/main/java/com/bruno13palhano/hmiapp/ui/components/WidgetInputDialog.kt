package com.bruno13palhano.hmiapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bruno13palhano.hmiapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetInputDialog(
    label: String,
    endpoint: String,
    hasExtras: Boolean,
    extras: List<String> = emptyList(),
    hasLimit: Boolean,
    limit: String?,
    onLabelChange: (label: String) -> Unit,
    onEndpointChange: (endpoint: String) -> Unit,
    onExtraChange: (index: Int, value: String) -> Unit,
    onLimitChange: (limit: String) -> Unit,
    onAddExtra: () -> Unit,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.wrapContentWidth().wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(id = R.string.widget_configuration),
                )
                Spacer(modifier = Modifier.height(24.dp))
                CustomTextField(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth(),
                    value = label,
                    onValueChange = onLabelChange,
                    label = stringResource(id = R.string.label),
                    placeholder = stringResource(id = R.string.enter_label)
                )
                CustomTextField(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth(),
                    value = endpoint,
                    onValueChange = onEndpointChange,
                    label = stringResource(id = R.string.endpoint),
                    placeholder = stringResource(id = R.string.enter_endpoint)
                )
                if (hasLimit) {
                    CustomTextField(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth(),
                        value = limit ?: "",
                        onValueChange = onLimitChange,
                        label = stringResource(id = R.string.limit),
                        placeholder = stringResource(id = R.string.limit_placeholder)
                    )
                }
                if (hasExtras) {
                    Text(
                        text = stringResource(id = R.string.extras),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, start = 8.dp)
                    )

                    extras.forEachIndexed { index, value ->
                        CustomTextField(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .fillMaxWidth(),
                            value = value,
                            onValueChange = { newValue ->
                                onExtraChange(index, newValue)
                            },
                            label = stringResource(id = R.string.extra_tag, index + 1),
                            placeholder = stringResource(id = R.string.extra_placeholder)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .align(Alignment.End),
                        onClick = onAddExtra
                    ) {
                        Text(text = stringResource(id = R.string.add_extra))
                    }
                }
                Row(
                    modifier = Modifier
                        .padding(top = 16.dp, end = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnvironmentInputDialog(
    name: String,
    onNameChange: (name: String) -> Unit,
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
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(id = R.string.environment),
                )
                Spacer(modifier = Modifier.height(24.dp))
                CustomTextField(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth(),
                    value = name,
                    onValueChange = onNameChange,
                    label = stringResource(id = R.string.name),
                    placeholder = stringResource(id = R.string.name_placeholder)
                )

                Row(
                    modifier = Modifier
                        .padding(top = 16.dp, end = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
}