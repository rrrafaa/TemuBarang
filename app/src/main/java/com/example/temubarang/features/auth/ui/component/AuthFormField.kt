package com.example.temubarang.features.auth.ui.component

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.temubarang.ui.component.AppValidatedTextField

@Composable
fun NameField(
    value: String, onValueChange: (String) -> Unit, enabled: Boolean = true,
    errorText: String? = null, isFirstError: Boolean = false, submitVersion: Int = 0
) {
    AppValidatedTextField(value, onValueChange, label = "Nama lengkap", enabled = enabled,
        errorText = errorText, isFirstError = isFirstError, submitVersion = submitVersion)
}

@Composable
fun EmailField(
    value: String, onValueChange: (String) -> Unit, enabled: Boolean = true,
    errorText: String? = null, isFirstError: Boolean = false, submitVersion: Int = 0
) {
    AppValidatedTextField(value, onValueChange, label = "Email", enabled = enabled,
        keyboardType = KeyboardType.Email, capitalization = KeyboardCapitalization.None,
        errorText = errorText, isFirstError = isFirstError, submitVersion = submitVersion)
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Password",
    enabled: Boolean = true,
    errorText: String? = null,
    isFirstError: Boolean = false,
    submitVersion: Int = 0
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    AppValidatedTextField(
        value, onValueChange, label = label, enabled = enabled,
        keyboardType = KeyboardType.Password, capitalization = KeyboardCapitalization.None,
        errorText = errorText, isFirstError = isFirstError, submitVersion = submitVersion,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            TextButton(onClick = { visible = !visible }, enabled = enabled) {
                Text(if (visible) "Tutup" else "Lihat")
            }
        }
    )
}
