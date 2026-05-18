package com.example.treasurehuntapp.features.reset_password

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.treasurehuntapp.ui.appPageHeaderPadding
import com.example.treasurehuntapp.ui.theme.AppColors
import com.example.treasurehuntapp.ui.theme.AppGradients
import kotlinx.coroutines.delay

@Composable
fun ResetPasswordScreen(
    token: String?,
    onBack: () -> Unit,
    onResetSuccess: () -> Unit,
    vm: ResetPasswordViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    LaunchedEffect(token) {
        vm.setToken(token)
    }

    LaunchedEffect(state.successMessage) {
        if (!state.successMessage.isNullOrBlank()) {
            delay(1000)
            onResetSuccess()
        }
    }

    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppGradients.ScreenBackground)
                .padding(padding)
                .appPageHeaderPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = AppColors.PurplePrimary
                )
            }

            Spacer(Modifier.height(40.dp))

            Text(
                text = "Reset Password",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Create a new password for your account.",
                color = AppColors.TextMuted,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(28.dp))

            if (state.token.isBlank()) {
                Text(
                    text = "Invalid reset link.",
                    color = AppColors.Error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            ResetPasswordFieldLabel("NEW PASSWORD")
            Spacer(Modifier.height(8.dp))
            ResetPasswordField(
                value = state.newPassword,
                onValueChange = vm::onNewPasswordChange,
                placeholder = "Enter new password",
                visible = showNewPassword,
                onToggleVisibility = { showNewPassword = !showNewPassword }
            )

            Spacer(Modifier.height(14.dp))

            ResetPasswordFieldLabel("CONFIRM PASSWORD")
            Spacer(Modifier.height(8.dp))
            ResetPasswordField(
                value = state.confirmPassword,
                onValueChange = vm::onConfirmPasswordChange,
                placeholder = "Confirm new password",
                visible = showConfirmPassword,
                onToggleVisibility = { showConfirmPassword = !showConfirmPassword }
            )

            if (!state.error.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = state.error.orEmpty(),
                    color = AppColors.Error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (!state.successMessage.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = state.successMessage.orEmpty(),
                    color = AppColors.Success,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = { vm.submit(onSuccess = {}) },
                enabled = !state.isLoading && state.token.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.PurplePrimary,
                    contentColor = Color.White
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Reset Password", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Back to Login",
                    color = AppColors.PurplePrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onBack)
                )
            }
        }
    }
}

@Composable
private fun ResetPasswordFieldLabel(text: String) {
    Text(
        text = text,
        color = AppColors.PurplePrimary,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ResetPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    visible: Boolean,
    onToggleVisibility: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(placeholder, color = AppColors.TextMuted) },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                    contentDescription = "Toggle password visibility",
                    tint = AppColors.TextMuted
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AppColors.Surface,
            unfocusedContainerColor = AppColors.Surface,
            focusedBorderColor = AppColors.PurplePrimary,
            unfocusedBorderColor = AppColors.PurpleDeep,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = AppColors.PurplePrimary
        )
    )
}
