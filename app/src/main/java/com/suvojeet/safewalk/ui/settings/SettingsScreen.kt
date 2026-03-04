package com.suvojeet.safewalk.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.suvojeet.safewalk.ui.theme.SafeGreen

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val shakeEnabled by viewModel.shakeEnabled.collectAsStateWithLifecycle()
    val shakeSensitivity by viewModel.shakeSensitivity.collectAsStateWithLifecycle()
    val panicVibrate by viewModel.panicVibrate.collectAsStateWithLifecycle()
    val autoCall by viewModel.autoCall.collectAsStateWithLifecycle()
    val darkTheme by viewModel.darkTheme.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Configure your safety preferences",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Profile section
        SettingsSection(title = "Profile") {
            Text(
                text = "Your Name",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "Used in emergency SMS alerts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = userName,
                onValueChange = { viewModel.setUserName(it) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
                placeholder = { Text("Enter your name") },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Shake Detection section
        SettingsSection(title = "Shake Detection") {
            SettingsToggle(
                title = "Enable Shake Trigger",
                description = "Shake phone to trigger emergency alert",
                checked = shakeEnabled,
                onCheckedChange = { viewModel.setShakeEnabled(it) },
            )

            if (shakeEnabled) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "Sensitivity",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = when {
                            shakeSensitivity < 8f -> "Very sensitive"
                            shakeSensitivity < 12f -> "Normal"
                            else -> "Less sensitive"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Slider(
                        value = shakeSensitivity,
                        onValueChange = { viewModel.setShakeSensitivity(it) },
                        valueRange = 4f..20f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Panic Settings section
        SettingsSection(title = "Panic Alert") {
            SettingsToggle(
                title = "Vibrate on Panic",
                description = "Vibrate device when panic is triggered",
                checked = panicVibrate,
                onCheckedChange = { viewModel.setPanicVibrate(it) },
            )
            SettingsToggle(
                title = "Auto-call First Contact",
                description = "Automatically call your primary emergency contact",
                checked = autoCall,
                onCheckedChange = { viewModel.setAutoCall(it) },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Appearance section
        SettingsSection(title = "Appearance") {
            SettingsToggle(
                title = "Dark Theme",
                description = "Use dark theme for better night visibility",
                checked = darkTheme,
                onCheckedChange = { viewModel.setDarkTheme(it) },
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "SafeWalk v1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SafeGreen,
                checkedTrackColor = SafeGreen.copy(alpha = 0.3f),
            ),
        )
    }
}
