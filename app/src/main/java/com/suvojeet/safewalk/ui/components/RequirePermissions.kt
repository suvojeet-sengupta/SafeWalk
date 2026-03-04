package com.suvojeet.safewalk.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * Step-based permission flow.
 * Android REQUIRES background location to be requested SEPARATELY after foreground location.
 * This composable handles that correctly with a multi-step UI.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequirePermissions(
    permissions: List<String>,
    onAllPermissionsGranted: @Composable () -> Unit,
) {
    val context = LocalContext.current

    // Separate background location from other permissions (Android requirement)
    val foregroundPermissions = permissions.filter {
        it != Manifest.permission.ACCESS_BACKGROUND_LOCATION
    }
    val needsBackgroundLocation = permissions.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    val foregroundState = rememberMultiplePermissionsState(permissions = foregroundPermissions)

    // Track whether we've already auto-launched the first request
    var hasRequestedOnce by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Check all permissions status live
    var allForegroundGranted by remember { mutableStateOf(false) }
    var backgroundGranted by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Re-check on every resume (user may have just come back from Settings)
                allForegroundGranted = foregroundPermissions.all {
                    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                }
                backgroundGranted = !needsBackgroundLocation ||
                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) ||
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Auto-request foreground permissions once
    LaunchedEffect(Unit) {
        if (!hasRequestedOnce && !allForegroundGranted) {
            hasRequestedOnce = true
            foregroundState.launchMultiplePermissionRequest()
        }
    }

    // Update state when accompanist reports changes
    LaunchedEffect(foregroundState.allPermissionsGranted) {
        allForegroundGranted = foregroundState.allPermissionsGranted
    }

    if (allForegroundGranted && backgroundGranted) {
        onAllPermissionsGranted()
    } else {
        PermissionSetupScreen(
            foregroundPermissions = foregroundPermissions,
            needsBackgroundLocation = needsBackgroundLocation,
            allForegroundGranted = allForegroundGranted,
            backgroundGranted = backgroundGranted,
            onRequestForeground = {
                foregroundState.launchMultiplePermissionRequest()
            },
            onRequestBackground = {
                // Background location must go to Settings on Android 11+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // On Android 10, we can still request it directly via runtime
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            },
            shouldShowRationale = foregroundState.shouldShowRationale,
        )
    }
}

@Composable
private fun PermissionSetupScreen(
    foregroundPermissions: List<String>,
    needsBackgroundLocation: Boolean,
    allForegroundGranted: Boolean,
    backgroundGranted: Boolean,
    onRequestForeground: () -> Unit,
    onRequestBackground: () -> Unit,
    shouldShowRationale: Boolean,
) {
    val context = LocalContext.current

    // Calculate progress
    val totalSteps = if (needsBackgroundLocation) 2 else 1
    val currentStep = when {
        allForegroundGranted && !backgroundGranted -> 2
        !allForegroundGranted -> 1
        else -> totalSteps
    }
    val progress = (currentStep - 1).toFloat() / totalSteps

    // Individual permission statuses
    data class PermissionItem(
        val name: String,
        val description: String,
        val icon: ImageVector,
        val permission: String,
        val isGranted: Boolean,
    )

    val permissionItems = buildList {
        if (foregroundPermissions.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
            add(
                PermissionItem(
                    name = "Location",
                    description = "Track & share your path in real-time",
                    icon = Icons.Filled.LocationOn,
                    permission = Manifest.permission.ACCESS_FINE_LOCATION,
                    isGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED,
                ),
            )
        }
        if (foregroundPermissions.contains(Manifest.permission.SEND_SMS)) {
            add(
                PermissionItem(
                    name = "SMS",
                    description = "Send SOS alerts to emergency contacts",
                    icon = Icons.Filled.Message,
                    permission = Manifest.permission.SEND_SMS,
                    isGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.SEND_SMS,
                    ) == PackageManager.PERMISSION_GRANTED,
                ),
            )
        }
        if (foregroundPermissions.contains(Manifest.permission.CALL_PHONE)) {
            add(
                PermissionItem(
                    name = "Phone Call",
                    description = "Auto-call emergency contacts in panic",
                    icon = Icons.Filled.Phone,
                    permission = Manifest.permission.CALL_PHONE,
                    isGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CALL_PHONE,
                    ) == PackageManager.PERMISSION_GRANTED,
                ),
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            foregroundPermissions.contains(Manifest.permission.POST_NOTIFICATIONS)
        ) {
            add(
                PermissionItem(
                    name = "Notifications",
                    description = "Show timer & tracking alerts",
                    icon = Icons.Filled.Notifications,
                    permission = Manifest.permission.POST_NOTIFICATIONS,
                    isGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED,
                ),
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Shield icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Setup Permissions",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "SafeWalk needs these permissions to keep you safe",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Progress indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Step $currentStep of $totalSteps",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "${(progress * 100).toInt()}% complete",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Step 1: Foreground permissions
        AnimatedVisibility(
            visible = !allForegroundGranted,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column {
                Text(
                    text = "Step 1: Core Permissions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                )

                permissionItems.forEach { item ->
                    PermissionItemCard(
                        name = item.name,
                        description = item.description,
                        icon = item.icon,
                        isGranted = item.isGranted,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (shouldShowRationale) {
                            onRequestForeground()
                        } else {
                            // Permissions permanently denied — must open Settings
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(
                        text = if (shouldShowRationale) "Grant Permissions" else "Open Settings",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                if (!shouldShowRationale) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Permissions were denied. Please enable them manually in Settings → Permissions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        // Step 2: Background location
        AnimatedVisibility(
            visible = allForegroundGranted && needsBackgroundLocation && !backgroundGranted,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column {
                Text(
                    text = "Step 2: Background Location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                )

                PermissionItemCard(
                    name = "Background Location",
                    description = "Keep tracking your location even when app is closed — essential for SOS and panic alerts",
                    icon = Icons.Filled.MyLocation,
                    isGranted = false,
                )

                Spacer(modifier = Modifier.height(8.dp))

                ElevatedCard(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    ),
                ) {
                    Text(
                        text = "In Settings, go to Permissions → Location → select \"Allow all the time\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Medium,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onRequestBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(
                        text = "Open Settings",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PermissionItemCard(
    name: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isGranted) {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "perm_bg",
    )

    ElevatedCard(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = bgColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isGranted) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isGranted) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isGranted) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Filled.Check else Icons.Outlined.Close,
                    contentDescription = if (isGranted) "Granted" else "Not granted",
                    tint = if (isGranted) MaterialTheme.colorScheme.onSecondary
                    else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
