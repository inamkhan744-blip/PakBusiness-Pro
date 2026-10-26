package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppLanguage
import com.example.ui.Localization
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary

@Composable
fun AppLockScreen(
    savedUsername: String,
    onUnlock: (password: String) -> Boolean,
    onUnlockWithCredentials: ((usernameOrStaffCode: String, pinOrPass: String) -> Pair<Boolean, String>)? = null,
    onBypassSecurity: () -> Unit = {},
    appLanguage: AppLanguage = AppLanguage.ENGLISH,
    businessName: String = "PakBusiness Pro"
) {
    var inputUsername by remember { mutableStateOf(savedUsername.ifBlank { "admin" }) }
    var inputPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successGreeting by remember { mutableStateOf("") }
    var showForgotHint by remember { mutableStateOf(false) }
    var selectedLoginMode by remember { mutableStateOf(0) } // 0: Owner, 1: Staff ID

    val focusManager = LocalFocusManager.current

    val handleLoginAction = {
        focusManager.clearFocus()
        if (onUnlockWithCredentials != null) {
            val (ok, msg) = onUnlockWithCredentials(inputUsername.trim(), inputPassword.trim())
            if (!ok) {
                errorMessage = msg
            } else {
                errorMessage = ""
                successGreeting = msg
            }
        } else {
            val success = onUnlock(inputPassword.trim())
            if (!success) {
                errorMessage = "Ghalat Password! Please try again."
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PakEmeraldDark,
                        Color(0xFF0F2E23),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Shield Lock Icon with Golden Glow
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(Color.White.copy(alpha = 0.12f), CircleShape)
                    .border(2.dp, PakGoldSecondary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Security Shield",
                    tint = PakGoldSecondary,
                    modifier = Modifier.size(46.dp)
                )
            }

            Text(
                text = businessName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = when (appLanguage) {
                    AppLanguage.URDU -> "کاروباری سیکیورٹی اور پاس ورڈ لاگ ان"
                    AppLanguage.HINDI -> "व्यापार सुरक्षा और पासवर्ड लॉगिन"
                    AppLanguage.ENGLISH -> "Business Security & Protected Login"
                },
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = when (appLanguage) {
                            AppLanguage.URDU -> "براہ کرم پاس ورڈ درج کریں"
                            AppLanguage.HINDI -> "कृपया पासवर्ड दर्ज करें"
                            AppLanguage.ENGLISH -> "Enter Credentials to Unlock"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Role Login Selector: Owner vs Staff ID
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedLoginMode == 0) PakEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedLoginMode == 0) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedLoginMode = 0
                                    inputUsername = savedUsername.ifBlank { "admin" }
                                    inputPassword = ""
                                    errorMessage = ""
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Owner Login (مالک)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedLoginMode == 1) PakGoldSecondary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedLoginMode == 1) Color.Black else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedLoginMode = 1
                                    inputUsername = ""
                                    inputPassword = ""
                                    errorMessage = ""
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Staff ID (عملہ)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Username / Staff ID Field
                    OutlinedTextField(
                        value = inputUsername,
                        onValueChange = { inputUsername = it },
                        label = {
                            Text(
                                if (selectedLoginMode == 0) "Owner Username / صارف نام"
                                else "Staff ID / Phone / Name (عملہ آئی ڈی یا فون)"
                            )
                        },
                        placeholder = {
                            if (selectedLoginMode == 1) Text("e.g. STF-101 ya 03001234567")
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = if (selectedLoginMode == 0) PakEmeraldPrimary else PakGoldSecondary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lock_screen_username")
                    )

                    // Password / Staff PIN Field
                    OutlinedTextField(
                        value = inputPassword,
                        onValueChange = {
                            inputPassword = it
                            errorMessage = ""
                        },
                        label = {
                            Text(
                                if (selectedLoginMode == 0) "Master Password / PIN (پاس ورڈ)"
                                else "Staff 4-Digit PIN (عملہ پن کوڈ)"
                            )
                        },
                        placeholder = {
                            if (selectedLoginMode == 1) Text("Default: 1234")
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Key,
                                contentDescription = null,
                                tint = if (selectedLoginMode == 0) PakEmeraldPrimary else PakGoldSecondary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (selectedLoginMode == 1) KeyboardType.NumberPassword else KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { handleLoginAction() }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lock_screen_password")
                    )

                    AnimatedVisibility(
                        visible = errorMessage.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Unlock Button
                    Button(
                        onClick = { handleLoginAction() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedLoginMode == 0) PakEmeraldPrimary else PakGoldSecondary,
                            contentColor = if (selectedLoginMode == 0) Color.White else Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_unlock_app")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedLoginMode == 0) {
                                when (appLanguage) {
                                    AppLanguage.URDU -> "مالک لاگ ان کریں"
                                    AppLanguage.HINDI -> "मालिक लॉगिन करें"
                                    AppLanguage.ENGLISH -> "Login as Owner"
                                }
                            } else {
                                when (appLanguage) {
                                    AppLanguage.URDU -> "عملہ لاگ ان کریں (Staff Login)"
                                    AppLanguage.HINDI -> "स्टाफ लॉगिन करें"
                                    AppLanguage.ENGLISH -> "Staff Member Login"
                                }
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showForgotHint = !showForgotHint }
                        ) {
                            Text(
                                text = "Forgot Password?",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        TextButton(
                            onClick = onBypassSecurity
                        ) {
                            Text(
                                text = "Skip / Guest Access",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    AnimatedVisibility(visible = showForgotHint) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PakEmeraldContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Admin Hint: Agar aap password bhool gaye hain to 'admin' ya blank password try karein, ya 'Skip / Guest Access' dabayein.",
                                fontSize = 11.sp,
                                color = PakEmeraldDark,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
