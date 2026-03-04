package com.suvojeet.safewalk.ui.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequirePermissions(
    permissions: List<String>,
    onAllPermissionsGranted: @Composable () -> Unit
) {
    val permissionState = rememberMultiplePermissionsState(permissions = permissions)
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                permissionState.launchMultiplePermissionRequest()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    })

    if (permissionState.allPermissionsGranted) {
        onAllPermissionsGranted()
    } else {
        PermissionDeniedContent(
            permissionState = permissionState,
            permissions = permissions
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionDeniedContent(
    permissionState: MultiplePermissionsState,
    permissions: List<String>
) {
    val context = LocalContext.current
    
    // Determine which permissions are missing to show appropriate message
    val isLocationMissing = permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION) &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED

    val isSmsMissing = permissions.contains(Manifest.permission.SEND_SMS) &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED

    val isCallPhoneMissing = permissions.contains(Manifest.permission.CALL_PHONE) &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED

    val isNotificationMissing = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
        permissions.contains(Manifest.permission.POST_NOTIFICATIONS) &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val message = buildString {
            append("SafeWalk needs the following permissions to work correctly:\n\n")
            if (isLocationMissing) append("📍 Location (For tracking and sharing your path)\n")
            if (isSmsMissing) append("💬 SMS (To send SOS messages to your emergency contacts)\n")
            if (isCallPhoneMissing) append("📞 Phone Call (To directly call emergency numbers)\n")
            if (isNotificationMissing) append("🔔 Notifications (To show timer and tracking status)\n")
            append("\nPlease grant these permissions to continue.")
        }
        
        Text(
            text = "Permissions Required",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = {
                if (permissionState.shouldShowRationale) {
                    permissionState.launchMultiplePermissionRequest()
                } else {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (permissionState.shouldShowRationale) "Grant Permissions" else "Open Settings")
        }
    }
}
