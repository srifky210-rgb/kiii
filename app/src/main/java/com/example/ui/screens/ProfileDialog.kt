package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppThemeMode
import com.example.ui.components.Material3ThemeModeSelector
import com.example.ui.theme.HeartRose
import com.example.ui.theme.LocalPulseRunColors
import com.example.ui.theme.StravaOrange

@Composable
fun ProfileEditDialog(
    initialName: String,
    accountEmail: String,
    initialStepGoal: Int,
    initialCalorieGoal: Int,
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    onSelectThemeMode: (AppThemeMode) -> Unit = {},
    onDismiss: () -> Unit,
    onSave: (String, Int, Int) -> Unit,
    onLogout: () -> Unit
) {
    val palette = LocalPulseRunColors.current
    var name by remember { mutableStateOf(initialName) }
    var stepGoalText by remember { mutableStateOf(initialStepGoal.toString()) }
    var calorieGoalText by remember { mutableStateOf(initialCalorieGoal.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Akun & Target Harian", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (accountEmail.isNotBlank()) {
                    Text(
                        text = "Akun masuk: $accountEmail",
                        fontSize = 12.sp,
                        color = palette.textSecondary
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Akun") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input")
                )
                OutlinedTextField(
                    value = stepGoalText,
                    onValueChange = { stepGoalText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Target Langkah Harian") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_step_goal_input")
                )
                OutlinedTextField(
                    value = calorieGoalText,
                    onValueChange = { calorieGoalText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Target Kalori (kkal)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_calorie_goal_input")
                )

                HorizontalDivider(color = palette.cardBorder)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tema Tampilan (Material 3)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Material3ThemeModeSelector(
                        selectedMode = themeMode,
                        onSelectMode = onSelectThemeMode
                    )
                }

                HorizontalDivider(color = palette.cardBorder)

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onLogout()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HeartRose),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("logout_button")
                ) {
                    Text("Keluar / Ganti Akun", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val steps = stepGoalText.toIntOrNull() ?: initialStepGoal
                    val cals = calorieGoalText.toIntOrNull() ?: initialCalorieGoal
                    onSave(name.trim().ifEmpty { initialName }, steps, cals)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = StravaOrange,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("profile_save_button")
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
