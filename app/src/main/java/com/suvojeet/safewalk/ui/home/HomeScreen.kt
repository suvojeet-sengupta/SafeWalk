package com.suvojeet.safewalk.ui.home

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.suvojeet.safewalk.service.PanicService
import com.suvojeet.safewalk.ui.home.components.PanicButton
import com.suvojeet.safewalk.ui.home.components.QuickShareCard
import com.suvojeet.safewalk.ui.home.components.TimerStatusCard

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val isPanicActive by viewModel.isPanicActive.collectAsStateWithLifecycle()
    val isLocationSharing by viewModel.isLocationSharing.collectAsStateWithLifecycle()
    val timerActive by viewModel.timerActive.collectAsStateWithLifecycle()
    val timerRemaining by viewModel.timerRemainingSeconds.collectAsStateWithLifecycle()
    val timerTotal by viewModel.timerTotalSeconds.collectAsStateWithLifecycle()
    val defaultDuration by viewModel.defaultTimerDuration.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()

    var showTimerPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Header ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "SafeWalk",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Your safety companion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Panic Button ──
        PanicButton(
            isPanicActive = isPanicActive,
            onPanicTrigger = {
                if (contacts.isEmpty()) {
                    Toast.makeText(
                        context,
                        "Add emergency contacts first!",
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {
                    viewModel.onPanicTriggered()
                    PanicService.start(context)
                }
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (isPanicActive) "Emergency alert sent!" else "Long press for emergency alert",
            style = MaterialTheme.typography.bodySmall,
            color = if (isPanicActive) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Check-in Timer Section ──
        if (timerActive) {
            TimerStatusCard(
                isActive = true,
                remainingSeconds = timerRemaining,
                totalSeconds = timerTotal,
                onCheckIn = {
                    viewModel.setTimerActive(false)
                    Toast.makeText(context, "Checked in safely!", Toast.LENGTH_SHORT).show()
                },
                onCancel = { viewModel.setTimerActive(false) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            FilledTonalButton(
                onClick = { showTimerPicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Check-in Timer ($defaultDuration min)")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Live Location Sharing ──
        QuickShareCard(
            isLocationSharing = isLocationSharing,
            onToggleSharing = { enabled ->
                viewModel.toggleLocationSharing(context, enabled)
                Toast.makeText(
                    context,
                    if (enabled) "Location sharing started" else "Location sharing stopped",
                    Toast.LENGTH_SHORT,
                ).show()
            },
            onShareLink = {
                try {
                    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                    fusedClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            val mapLink =
                                "https://maps.google.com/maps?q=${location.latitude},${location.longitude}"
                            val shareText =
                                "I'm sharing my live location via SafeWalk:\n$mapLink"
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(
                                Intent.createChooser(shareIntent, "Share location via"),
                            )
                        } else {
                            Toast.makeText(
                                context,
                                "Location not available yet",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                } catch (e: SecurityException) {
                    Toast.makeText(
                        context,
                        "Location permission required",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Emergency Contacts Status ──
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (contacts.isEmpty()) Icons.Outlined.Warning else Icons.Filled.Group,
                    contentDescription = null,
                    tint = if (contacts.isEmpty()) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.secondary
                    },
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (contacts.isEmpty()) "No Emergency Contacts" else "Emergency Contacts",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = if (contacts.isEmpty()) {
                            "Go to Contacts tab to add"
                        } else {
                            "${contacts.size} contact${if (contacts.size > 1) "s" else ""} active"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    // ── Timer Picker Dialog ──
    if (showTimerPicker) {
        TimerPickerDialog(
            defaultMinutes = defaultDuration,
            onDismiss = { showTimerPicker = false },
            onConfirm = { minutes ->
                showTimerPicker = false
                viewModel.startTimer(minutes)
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimerPickerDialog(
    defaultMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var selectedMinutes by remember { mutableIntStateOf(defaultMinutes) }
    var customText by remember { mutableStateOf("") }
    var isCustom by remember { mutableStateOf(false) }

    val presetOptions = listOf(5, 10, 15, 20, 30, 45, 60)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Set Timer Duration",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Choose how long before a check-in is needed:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    presetOptions.forEach { minutes ->
                        FilterChip(
                            selected = !isCustom && selectedMinutes == minutes,
                            onClick = {
                                selectedMinutes = minutes
                                isCustom = false
                            },
                            label = { Text("$minutes min") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                    FilterChip(
                        selected = isCustom,
                        onClick = { isCustom = true },
                        label = { Text("Custom") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        ),
                    )
                }

                if (isCustom) {
                    OutlinedTextField(
                        value = customText,
                        onValueChange = { customText = it.filter { c -> c.isDigit() } },
                        label = { Text("Minutes (1–120)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                }

                // Show selected duration summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    val displayMinutes = if (isCustom) {
                        customText.toIntOrNull() ?: 0
                    } else {
                        selectedMinutes
                    }
                    Text(
                        text = "Timer will run for $displayMinutes minutes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val duration = if (isCustom) {
                        customText.toIntOrNull()?.coerceIn(1, 120) ?: defaultMinutes
                    } else {
                        selectedMinutes
                    }
                    onConfirm(duration)
                },
            ) {
                Text("Start Timer", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
