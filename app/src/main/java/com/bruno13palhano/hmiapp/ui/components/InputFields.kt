package com.bruno13palhano.hmiapp.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.bruno13palhano.hmiapp.ui.shared.clearFocusOnKeyboardDismiss

@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    icon: @Composable (() -> Unit)? = null,
    label: String,
    placeholder: String,
    enabled: Boolean = true,
    isError: Boolean = false,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
) {
    OutlinedTextField(
        modifier = modifier.clearFocusOnKeyboardDismiss(),
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        isError = isError,
        leadingIcon = icon,
        label = {
            Text(
                text = label,
                fontStyle = FontStyle.Italic,
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                fontStyle = FontStyle.Italic,
            )
        },
        singleLine = singleLine,
        readOnly = readOnly,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { defaultKeyboardAction(ImeAction.Done) }),
    )
}

@Composable
fun CustomPasswordTextField(
    modifier: Modifier = Modifier,
    visibility: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    togglePasswordVisibility: () -> Unit,
    label: String,
    enabled: Boolean = true,
    placeholder: String,
    isError: Boolean = false,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        isError = isError,
        leadingIcon = leadingIcon,
        trailingIcon = {
            if (visibility) {
                IconButton(onClick = togglePasswordVisibility) {
                    Icon(
                        imageVector = Icons.Filled.Visibility,
                        contentDescription = null,
                    )
                }
            } else {
                IconButton(onClick = togglePasswordVisibility) {
                    Icon(
                        imageVector = Icons.Filled.VisibilityOff,
                        contentDescription = null,
                    )
                }
            }
        },
        label = {
            Text(
                text = label,
                fontStyle = FontStyle.Italic,
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                fontStyle = FontStyle.Italic,
            )
        },
        singleLine = singleLine,
        readOnly = readOnly,
        visualTransformation = if (visibility) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions =
        KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { defaultKeyboardAction(ImeAction.Done) }),
    )
}

@Composable
fun CustomIntegerField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    label: String,
    placeholder: String,
    isError: Boolean = false,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
) {
    val patternInt = remember { Regex("^\\d*") }

    OutlinedTextField(
        modifier = modifier.clearFocusOnKeyboardDismiss(),
        value = value,
        onValueChange = { newValue ->
            if (newValue.isEmpty() || newValue.matches(patternInt)) {
                onValueChange(newValue)
            }
        },
        enabled = enabled,
        isError = isError,
        leadingIcon = leadingIcon,
        label = {
            Text(
                text = label,
                fontStyle = FontStyle.Italic,
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                fontStyle = FontStyle.Italic,
            )
        },
        singleLine = singleLine,
        readOnly = readOnly,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Number,
        ),
        keyboardActions = KeyboardActions(onDone = { defaultKeyboardAction(ImeAction.Done) }),
    )
}
