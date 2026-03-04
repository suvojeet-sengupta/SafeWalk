package com.suvojeet.safewalk.ui.settings

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhoneInTalk
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.suvojeet.safewalk.receiver.SafeWalkDeviceAdminReceiver

@OptIn(ExperimentalLayoutApi::class)
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
    val userPhone by viewModel.userPhone.collectAsStateWithLifecycle()
    val timerDuration by viewModel.timerDuration.collectAsStateWithLifecycle()
    val sirenEnabled by viewModel.sirenEnabled.collectAsStateWithLifecycle()
    val strobeEnabled by viewModel.strobeEnabled.collectAsStateWithLifecycle()
    val lowBatteryAlert by viewModel.lowBatteryAlert.collectAsStateWithLifecycle()
    val networkLossAlert by viewModel.networkLossAlert.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val devicePolicyManager = remember {
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }
    val adminComponent = remember { SafeWalkDeviceAdminReceiver.getComponentName(context) }
    var isDeviceAdminActive by remember {
        mutableStateOf(devicePolicyManager.isAdminActive(adminComponent))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        // ── Header ──
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Configure your safety preferences",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Profile Section ──
        SettingsSection(title = "Profile", icon = Icons.Outlined.Person) {
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

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your Phone Number",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "Included in SOS messages so contacts can call you back",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = userPhone,
                onValueChange = { viewModel.setUserPhone(it) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
                placeholder = { Text("+91 XXXXX XXXXX") },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Check-in Timer Section ──
        SettingsSection(title = "Check-in Timer", icon = Icons.Outlined.Timer) {
            Text(
                text = "Default Timer Duration",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "Set how long before a check-in is needed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(10.dp))

            val presetOptions = listOf(5, 10, 15, 20, 30, 45, 60)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                presetOptions.forEach { minutes ->
                    FilterChip(
                        selected = timerDuration == minutes,
                        onClick = { viewModel.setTimerDuration(minutes) },
                        label = { Text("$minutes min") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Currently: $timerDuration minutes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Shake Detection Section ──
        SettingsSection(title = "Shake Detection", icon = Icons.Outlined.Vibration) {
            SettingsToggle(
                title = "Enable Shake Trigger",
                description = "Shake phone to trigger emergency alert",
                checked = shakeEnabled,
                onCheckedChange = { viewModel.setShakeEnabled(it) },
            )

            if (shakeEnabled) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                )
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
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
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Slider(
                        value = shakeSensitivity,
                        onValueChange = { viewModel.setShakeSensitivity(it) },
                        valueRange = 4f..20f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        ),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Panic Alert Section ──
        SettingsSection(title = "Panic Alert", icon = Icons.Outlined.Shield) {
            SettingsToggle(
                title = "Vibrate on Panic",
                description = "Vibrate device when panic is triggered",
                checked = panicVibrate,
                onCheckedChange = { viewModel.setPanicVibrate(it) },
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            )
            SettingsToggle(
                title = "Auto-call First Contact",
                description = "Automatically call your primary emergency contact",
                checked = autoCall,
                onCheckedChange = { viewModel.setAutoCall(it) },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Deterrent Mode Section ──
        SettingsSection(title = "Deterrent Mode", icon = Icons.Outlined.VolumeUp) {
            SettingsToggle(
                title = "Loud Siren",
                description = "Play high-volume alarm sound during panic",
                checked = sirenEnabled,
                onCheckedChange = { viewModel.setSirenEnabled(it) },
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            )
            SettingsToggle(
                title = "Strobe Light",
                description = "Blink flashlight rapidly to attract attention",
                checked = strobeEnabled,
                onCheckedChange = { viewModel.setStrobeEnabled(it) },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── System Alerts Section ──
        SettingsSection(title = "System Alerts", icon = Icons.Outlined.BatteryAlert) {
            SettingsToggle(
                title = "Low Battery SOS",
                description = "Notify contacts when battery is critically low",
                checked = lowBatteryAlert,
                onCheckedChange = { viewModel.setLowBatteryAlert(it) },
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            )
            SettingsToggle(
                title = "Network Loss SOS",
                description = "Notify contacts if phone goes offline/airplane mode",
                checked = networkLossAlert,
                onCheckedChange = { viewModel.setNetworkLossAlert(it) },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Appearance Section ──
        SettingsSection(title = "Appearance", icon = Icons.Outlined.DarkMode) {
            SettingsToggle(
                title = "Dark Theme",
                description = "Use dark theme for better night visibility",
                checked = darkTheme,
                onCheckedChange = { viewModel.setDarkTheme(it) },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Anti-Tamper Protection Section ──
        SettingsSection(title = "Protection", icon = Icons.Outlined.Lock) {
            Text(
                text = "Uninstall Prevention",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "When enabled, the app cannot be uninstalled without deactivating admin first. This prevents an attacker from removing SafeWalk from your phone.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (isDeviceAdminActive) {
                ElevatedCard(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "✅ Protection Active",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            } else {
                Button(
                    onClick = {
                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                            putExtra(
                                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                "SafeWalk needs Device Admin to prevent attackers from uninstalling the app during an emergency.",
                            )
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text("Enable Protection", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "SafeWalk v1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector? = null,
    content: @Composable () -> Unit,
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
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
            .padding(vertical = 6.dp),
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
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.secondary,
                checkedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        )
    }
}
