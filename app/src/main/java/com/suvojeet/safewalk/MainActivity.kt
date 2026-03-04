package com.suvojeet.safewalk

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.suvojeet.safewalk.data.local.prefs.PreferencesManager
import com.suvojeet.safewalk.receiver.ShakeDetector
import com.suvojeet.safewalk.service.PanicService
import com.suvojeet.safewalk.ui.navigation.BottomNavBar
import com.suvojeet.safewalk.ui.navigation.NavGraph
import com.suvojeet.safewalk.ui.theme.SafeWalkTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private var sensorManager: SensorManager? = null
    private var shakeDetector: ShakeDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup shake detection
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        shakeDetector = ShakeDetector {
            // Shake detected → trigger panic
            PanicService.start(this)
        }

        setContent {
            val isDarkTheme by preferencesManager.isDarkTheme
                .collectAsStateWithLifecycle(initialValue = true)

            SafeWalkTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()

                val permissions = buildList {
                    add(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    add(android.Manifest.permission.SEND_SMS)
                    add(android.Manifest.permission.CALL_PHONE)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        add(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        add(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }
                }

                com.suvojeet.safewalk.ui.components.RequirePermissions(
                    permissions = permissions
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = { BottomNavBar(navController = navController) },
                    ) { innerPadding ->
                        NavGraph(
                            navController = navController,
                            modifier = Modifier.padding(innerPadding),
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Register shake detector if enabled in settings
        val shakeEnabled = runBlocking { preferencesManager.isShakeEnabled.first() }
        val sensitivity = runBlocking { preferencesManager.shakeSensitivity.first() }
        if (shakeEnabled) {
            shakeDetector?.setSensitivity(sensitivity)
            sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let { accelerometer ->
                sensorManager?.registerListener(
                    shakeDetector,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_UI,
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(shakeDetector)
    }
}