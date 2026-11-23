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
import com.example.temubarang.ui.component.AppFormField

@Composable
fun NameField(value: String, onValueChange: (String) -> Unit, enabled: Boolean = true) {
    AppFormField(value, onValueChange, label = "Nama lengkap", enabled = enabled)
}

@Composable
fun EmailField(value: String, onValueChange: (String) -> Unit, enabled: Boolean = true) {
    AppFormField(value, onValueChange, label = "Email", enabled = enabled,
        keyboardType = KeyboardType.Email, capitalization = KeyboardCapitalization.None)
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Password",
    enabled: Boolean = true
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    AppFormField(value, onValueChange, label = label, enabled = enabled,
        keyboardType = KeyboardType.Password, capitalization = KeyboardCapitalization.None,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            TextButton(onClick = { visible = !visible }, enabled = enabled) {
                Text(if (visible) "Tutup" else "Lihat")
            }
        })
}
