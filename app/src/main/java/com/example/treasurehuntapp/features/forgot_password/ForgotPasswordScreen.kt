package com.example.treasurehuntapp.features.forgot_password

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.treasurehuntapp.ui.appPageHeaderPadding
import com.example.treasurehuntapp.ui.theme.AppColors
import com.example.treasurehuntapp.ui.theme.AppGradients

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    vm: ForgotPasswordViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val bg = AppGradients.ScreenBackground

    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(padding)
                .appPageHeaderPadding()
                .padding(horizontal = (24 - 16).dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = AppColors.PurplePrimary
                )
            }

            Spacer(Modifier.height(54.dp))

            Text(
                text = "Forgot Password?",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Enter your email address and we'll\nsend you instructions to reset your\npassword.",
                color = AppColors.TextMuted,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(44.dp))

            Text(
                text = "EMAIL ADDRESS",
                color = AppColors.PurplePrimary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = state.email,
                onValueChange = vm::onEmailChange,
                placeholder = { Text("name@example.com", color = AppColors.TextMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
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

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = vm::submit,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.PurplePrimary,
                    contentColor = Color.Black
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.Black
                    )
                } else {
                    Text(
                        text = "Reset Password",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

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

            Spacer(Modifier.height(12.dp))
        }
    }
}
