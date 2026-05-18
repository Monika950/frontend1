package com.example.treasurehuntapp.features.register

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.treasurehuntapp.ui.appPageHeaderPadding
import com.example.treasurehuntapp.ui.theme.AppColors
import com.example.treasurehuntapp.ui.theme.AppGradients

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit,
    vm: RegisterViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(false) }

    val bg = AppGradients.ScreenBackground
    val topGlow = AppGradients.GlowPurpleTop

    Scaffold(containerColor = Color.Transparent) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(topGlow)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .appPageHeaderPadding()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Create Account",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Join the hunt and discover hidden treasures\naround you.",
                        color = AppColors.TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(26.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        RegisterFieldLabel("First name")
                        Spacer(Modifier.height(6.dp))
                        StyledRegisterField(
                            value = state.firstName,
                            onValueChange = vm::onFirstNameChange,
                            placeholder = "John",
                            singleLine = true
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        RegisterFieldLabel("Last name")
                        Spacer(Modifier.height(6.dp))
                        StyledRegisterField(
                            value = state.lastName,
                            onValueChange = vm::onLastNameChange,
                            placeholder = "Doe",
                            singleLine = true
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                RegisterFieldLabel("Username")
                Spacer(Modifier.height(6.dp))
                StyledRegisterField(
                    value = state.username,
                    onValueChange = vm::onUsernameChange,
                    placeholder = "treasure_hunter_99",
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

                RegisterFieldLabel("Email address")
                Spacer(Modifier.height(6.dp))
                StyledRegisterField(
                    value = state.email,
                    onValueChange = vm::onEmailChange,
                    placeholder = "hunter@example.com",
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.MailOutline,
                            contentDescription = null,
                            tint = AppColors.TextMuted
                        )
                    },
                    singleLine = true
                )

                Spacer(Modifier.height(14.dp))

                RegisterFieldLabel("Password")
                Spacer(Modifier.height(6.dp))
                StyledRegisterField(
                    value = state.password,
                    onValueChange = vm::onPasswordChange,
                    placeholder = "••••••••",
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
                                contentDescription = "Toggle password visibility",
                                tint = Color(0xFF8C84A8)
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

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = acceptedTerms,
                        onCheckedChange = { acceptedTerms = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = AppColors.PurplePrimary,
                            uncheckedColor = AppColors.TextMuted,
                            checkmarkColor = Color.White
                        )
                    )
                    Spacer(Modifier.width(4.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "By signing up, I agree to the ",
                                color = AppColors.TextMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Terms of Service",
                                color = AppColors.PurplePrimary,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.clickable { }
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "and ",
                                color = AppColors.TextMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Privacy Policy.",
                                color = AppColors.PurplePrimary,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.clickable { }
                            )
                        }
                    }
                }

                if (!state.error.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = state.error!!,
                        color = AppColors.Error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { if (acceptedTerms) vm.register(onRegisterSuccess) },
                    enabled = !state.isLoading && acceptedTerms,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.PurplePrimary,
                        contentColor = Color.White,
                        disabledContainerColor = AppColors.PurpleDeep,
                        disabledContentColor = Color.White.copy(alpha = 0.75f)
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Creating...", fontWeight = FontWeight.Bold)
                    } else {
                        Text("Sign Up", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Already a hunter? ",
                        color = AppColors.TextMuted
                    )
                    Text(
                        text = "Sign In",
                        color = AppColors.PurplePrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onBack)
                    )
                }

                Spacer(Modifier.height(6.dp))
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

@Composable
private fun RegisterFieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = AppColors.TextMuted,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun StyledRegisterField(
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
            Text(placeholder, color = AppColors.TextMuted)
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AppColors.SurfaceAlt,
            unfocusedContainerColor = AppColors.SurfaceAlt,
            disabledContainerColor = AppColors.SurfaceAlt,
            focusedBorderColor = AppColors.PurplePrimary,
            unfocusedBorderColor = AppColors.Border,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = AppColors.PurplePrimary
        )
    )
}
