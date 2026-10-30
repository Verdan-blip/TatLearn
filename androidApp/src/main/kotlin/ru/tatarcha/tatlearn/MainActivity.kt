package ru.tatarcha.tatlearn

import android.Manifest
import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import ru.tatarcha.tatlearn.core.notifications.NotificationPermissionRequester

class MainActivity : ComponentActivity() {
    private val isDebuggable get() = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        NotificationPermissionRequester.onResult(granted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        NotificationPermissionRequester.attach { notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS) }

        setContent {
            App(showDebugTools = isDebuggable)
        }
    }

    override fun onDestroy() {
        NotificationPermissionRequester.detach()
        super.onDestroy()
    }
}
