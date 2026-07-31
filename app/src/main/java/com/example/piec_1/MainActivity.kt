package com.example.piec_1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.example.piec_1.domain.model.MedicamentoCapturadoDomain
import com.example.piec_1.ui.navigation.AppNavigation
import com.example.piec_1.ui.navigation.NavigationManager
import com.example.piec_1.ui.theme.PIEC1Theme
import com.example.piec_1.utils.notifications.NotificationHelper
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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
                    AppNavigation()
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

    override fun onDestroy() {
        super.onDestroy()
        NavigationManager.reset()
    }

    private fun processIntent(intent: Intent) {
        if (intent.action != "OPEN_CONFIRMATION") return

        val medicamentoJson = intent.getStringExtra("medicamento_json") ?: return

        try {
            val medicamento = Gson().fromJson(
                medicamentoJson,
                MedicamentoCapturadoDomain::class.java,
            )
            NavigationManager.setMedicamento(medicamento)
        } catch (_: Exception) {
            Log.w("MainActivity", "Payload de navegacao invalido")
        }
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
