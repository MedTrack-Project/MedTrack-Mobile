package com.medtrack.mobile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.medtrack.mobile.data.navigation.PendingNavigationStore
import com.medtrack.mobile.ui.camera.CameraController
import com.medtrack.mobile.ui.navigation.AppIntentContract
import com.medtrack.mobile.ui.navigation.AppNavigation
import com.medtrack.mobile.ui.navigation.NavigationManager
import com.medtrack.mobile.ui.theme.PIEC1Theme
import com.medtrack.mobile.utils.notifications.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var cameraController: CameraController

    @Inject lateinit var pendingNavigationStore: PendingNavigationStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        processIntent(intent)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)
        showNotificationPermissionWarningIfNeeded()

        setContent {
            PIEC1Theme {
                val isPermissionGranted = remember { mutableStateOf(false) }

                if (isPermissionGranted.value) {
                    AppNavigation(cameraController)
                } else {
                    RequestPermission { isGranted ->
                        isPermissionGranted.value = isGranted
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        processIntent(intent)
    }

    private fun processIntent(intent: Intent) {
        val reference = AppIntentContract.confirmationReference(
            intent.action,
            intent.getStringExtra(AppIntentContract.EXTRA_RESULT_REFERENCE),
        ) ?: return
        pendingNavigationStore.consume(reference)?.let(NavigationManager::openConfirmation)
    }

    private fun showNotificationPermissionWarningIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || areNotificationsEnabled()) {
            return
        }

        Toast.makeText(
            this,
            "Por favor, habilite as notificacoes nas configuracoes do aplicativo",
            Toast.LENGTH_LONG,
        ).show()
    }

    private fun areNotificationsEnabled(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

@Composable
private fun RequestPermission(onPermissionResult: (Boolean) -> Unit) {
    val context = LocalContext.current
    val permissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        } else {
            arrayOf(Manifest.permission.CAMERA)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissionsMap ->
        val allGranted = permissionsMap.values.all { it }
        onPermissionResult(allGranted)
        if (!allGranted) {
            Toast.makeText(
                context,
                "Algumas permissoes necessarias foram negadas!",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    LaunchedEffect(Unit) {
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            onPermissionResult(true)
        } else {
            permissionLauncher.launch(permissions)
        }
    }
}
