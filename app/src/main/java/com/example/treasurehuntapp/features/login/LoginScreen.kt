package com.example.treasurehuntapp.features.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Lock
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.example.treasurehuntapp.R
import com.example.treasurehuntapp.ui.appPageHeaderPadding
import com.example.treasurehuntapp.ui.theme.AppColors
import com.example.treasurehuntapp.ui.theme.AppGradients

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    vm: LoginViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    val pageBg = AppGradients.ScreenBackground
    val topGlow = Brush.verticalGradient(
        listOf(AppColors.PurplePrimary.copy(alpha = 0.35f), Color.Transparent)
    )

    Scaffold(containerColor = Color.Transparent) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(pageBg)
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(topGlow)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .appPageHeaderPadding()
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(Modifier.height(8.dp))

                Column {
                    Spacer(Modifier.height(180.dp))
                    Text(
                        text = stringResource(R.string.login_title),
                        color = AppColors.PurplePrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.login_welcome),
                        color = AppColors.TextMuted,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(Modifier.height(26.dp))

                    StyledLoginField(
                        value = state.email,
                        onValueChange = vm::onEmailChange,
                        placeholder = stringResource(R.string.login_email_placeholder),
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.AlternateEmail,
                                contentDescription = null,
                                tint = AppColors.TextMuted
                            )
                        },
                        singleLine = true
                    )

                    Spacer(Modifier.height(14.dp))

                    StyledLoginField(
                        value = state.password,
                        onValueChange = vm::onPasswordChange,
                        placeholder = stringResource(R.string.login_password_placeholder),
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = AppColors.TextMuted
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) {
                                        Icons.Rounded.VisibilityOff
                                    } else {
                                        Icons.Rounded.Visibility
                                    },
                                    contentDescription = stringResource(R.string.cd_toggle_password),
                                    tint = AppColors.TextMuted
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onForgotPasswordClick) {
                            Text(
                                stringResource(R.string.login_forgot_password),
                                color = AppColors.PurplePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (!state.error.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = state.error!!,
                            color = AppColors.Error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = { vm.login(onSuccess = onLoginSuccess) },
                        enabled = !state.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.PurplePrimary,
                            contentColor = Color.White,
                            disabledContainerColor = AppColors.PurpleDeep,
                            disabledContentColor = Color.White.copy(alpha = 0.9f)
                        )
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(stringResource(R.string.login_button_loading), fontWeight = FontWeight.Bold)
                        } else {
                            Text(stringResource(R.string.login_button), fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                Icons.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.login_new_user),
                            color = AppColors.TextMuted
                        )
                        TextButton(onClick = onRegisterClick) {
                            Text(
                                stringResource(R.string.login_create_account),
                                color = AppColors.PurplePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = AppColors.Border,
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier
                            .width(116.dp)
                            .height(4.dp)
                    ) {}
                }
            }
        }
    }
}

@Composable
private fun StyledLoginField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                color = Color(0xFF8D86A8)
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1A1431),
            unfocusedContainerColor = Color(0xFF1A1431),
            disabledContainerColor = Color(0xFF1A1431),
            focusedBorderColor = Color(0xFF8E63FF),
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFF8E63FF)
        )
    )
}
