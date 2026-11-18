package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import com.example.data.local.AppThemeMode
import com.example.ui.components.ThemeSwitcherDialog
import com.example.ui.components.pressScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.HeartRose
import com.example.ui.theme.LocalPulseRunColors
import com.example.ui.theme.PenjasTealVibrant
import com.example.ui.theme.StravaOrange
import com.example.ui.theme.StravaOrangeDark

@Composable
fun LoginScreen(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    isCurrentlyDark: Boolean = true,
    useDynamicColor: Boolean = false,
    onSelectThemeMode: (AppThemeMode) -> Unit = {},
    onToggleDynamicColor: (Boolean) -> Unit = {},
    onLogin: (accountName: String, accountEmail: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    val focusManager = LocalFocusManager.current
    var accountName by rememberSaveable { mutableStateOf("") }
    var accountEmail by rememberSaveable { mutableStateOf("") }
    var showNameError by rememberSaveable { mutableStateOf(false) }
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }

    fun attemptLogin() {
        val trimmed = accountName.trim()
        if (trimmed.isEmpty()) {
            showNameError = true
            return
        }
        focusManager.clearFocus()
        onLogin(trimmed, accountEmail.trim())
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        // Lightweight radial-gradient ambient glows (zero RenderEffect blur overhead)
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopEnd)
                .offset(x = 72.dp, y = (-56).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(StravaOrange.copy(alpha = 0.20f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-72).dp, y = 56.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PenjasTealVibrant.copy(alpha = 0.18f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        val themeIcon = when (themeMode) {
            AppThemeMode.SYSTEM -> Icons.Rounded.BrightnessAuto
            AppThemeMode.LIGHT -> Icons.Rounded.LightMode
            AppThemeMode.DARK -> Icons.Rounded.DarkMode
        }
        FilledTonalButton(
            onClick = { showThemeDialog = true },
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(48.dp)
                .testTag("login_theme_button")
        ) {
            Icon(
                imageVector = themeIcon,
                contentDescription = "Ganti tema tampilan",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = themeMode.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Brand Logo & Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(listOf(StravaOrange, StravaOrangeDark))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.DirectionsRun,
                        contentDescription = "Ikon PulseRun",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Text(
                    text = "PulseRun",
                    fontFamily = FredokaFontFamily,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )

                Text(
                    text = "Masuk dengan akun Anda untuk mulai melacak lari, pemanasan Penjas, dan kuis kebugaran mingguan.",
                    fontSize = 13.sp,
                    color = palette.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )
            }

            // Feature Highlights Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LoginHighlightChip(
                    icon = Icons.Rounded.Favorite,
                    label = "Detak & GPS",
                    tint = HeartRose,
                    modifier = Modifier.weight(1f)
                )
                LoginHighlightChip(
                    icon = Icons.Rounded.EmojiEvents,
                    label = "Kuis Mingguan",
                    tint = PenjasTealVibrant,
                    modifier = Modifier.weight(1f)
                )
            }

            // Login Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(palette.cardBackground)
                    .border(1.dp, palette.cardBorder, RoundedCornerShape(26.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Masuk Akun Atlet",
                    fontFamily = FredokaFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )

                OutlinedTextField(
                    value = accountName,
                    onValueChange = {
                        accountName = it
                        if (showNameError && it.isNotBlank()) showNameError = false
                    },
                    label = { Text("Nama Akun") },
                    placeholder = { Text("Masukkan nama lengkap atau nama akun") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Badge,
                            contentDescription = null,
                            tint = StravaOrange
                        )
                    },
                    isError = showNameError,
                    supportingText = if (showNameError) {
                        { Text("Nama akun wajib diisi untuk melanjutkan.") }
                    } else {
                        { Text("Nama ini akan digunakan sebagai nama pengguna di aplikasi.") }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StravaOrange,
                        focusedLabelColor = StravaOrange
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_account_name_input")
                )

                OutlinedTextField(
                    value = accountEmail,
                    onValueChange = { accountEmail = it },
                    label = { Text("Email atau ID Akun (Opsional)") },
                    placeholder = { Text("contoh@email.com") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.AlternateEmail,
                            contentDescription = null,
                            tint = PenjasTealVibrant
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { attemptLogin() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PenjasTealVibrant,
                        focusedLabelColor = PenjasTealVibrant
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_email_input")
                )

                AnimatedVisibility(
                    visible = accountName.trim().isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldAccent.copy(alpha = 0.12f))
                            .border(1.dp, EmeraldAccent.copy(alpha = 0.30f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmeraldAccent)
                        )
                        Text(
                            text = "Nama tampilan aktif: ${accountName.trim()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldAccent
                        )
                    }
                }

                val loginBtnInteraction = remember { MutableInteractionSource() }
                Button(
                    onClick = { attemptLogin() },
                    interactionSource = loginBtnInteraction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StravaOrange,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .pressScale(loginBtnInteraction)
                        .testTag("login_submit_button")
                ) {
                    Text(
                        text = "Masuk Sekarang",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showThemeDialog) {
        ThemeSwitcherDialog(
            themeMode = themeMode,
            isCurrentlyDark = isCurrentlyDark,
            useDynamicColor = useDynamicColor,
            onSelectThemeMode = onSelectThemeMode,
            onToggleDynamicColor = onToggleDynamicColor,
            onDismiss = { showThemeDialog = false }
        )
    }
}

@Composable
private fun LoginHighlightChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(palette.cardBackground)
            .border(1.dp, palette.cardBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = palette.textPrimary
        )
    }
}
