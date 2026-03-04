package com.suvojeet.safewalk.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "SafeWalk",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(
            text = "Your safety companion",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Panic Button
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

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isPanicActive) {
                "🚨 Emergency alert sent!"
            } else {
                "Long press for emergency alert"
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (isPanicActive) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Timer Status
        TimerStatusCard(
            isActive = timerActive,
            remainingSeconds = timerRemaining,
            onCheckIn = {
                viewModel.setTimerActive(false)
                Toast.makeText(context, "✓ Checked in safely!", Toast.LENGTH_SHORT).show()
            },
            onCancel = {
                viewModel.setTimerActive(false)
            },
            modifier = Modifier.fillMaxWidth(),
        )

        if (timerActive) {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quick Location Share
        QuickShareCard(
            isLocationSharing = isLocationSharing,
            onToggleSharing = { enabled ->
                // TODO: Start/stop LocationTrackingService
                Toast.makeText(
                    context,
                    if (enabled) "Location sharing started" else "Location sharing stopped",
                    Toast.LENGTH_SHORT,
                ).show()
            },
            onShareLink = {
                // TODO: Generate and share link
                Toast.makeText(context, "Share link copied!", Toast.LENGTH_SHORT).show()
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Emergency contacts count
        if (contacts.isEmpty()) {
            Text(
                text = "⚠️ No emergency contacts added.\nGo to Contacts tab to add.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        } else {
            Text(
                text = "${contacts.size} emergency contact${if (contacts.size > 1) "s" else ""} active",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
