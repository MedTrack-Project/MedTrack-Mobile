package com.medtrack.mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.medtrack.mobile.ui.camera.CameraController
import com.medtrack.mobile.ui.screen.TelaCamera
import com.medtrack.mobile.ui.screen.TelaConfirmacao
import com.medtrack.mobile.ui.screen.TelaDoseHorario
import com.medtrack.mobile.ui.screen.TelaEsqueciSenha
import com.medtrack.mobile.ui.screen.TelaInicial
import com.medtrack.mobile.ui.screen.TelaLogin
import com.medtrack.mobile.ui.screen.TelaPrincipal
import com.medtrack.mobile.ui.screen.TelaRedefinirSenha
import com.medtrack.mobile.ui.screen.viewmodel.CameraEvent
import com.medtrack.mobile.ui.screen.viewmodel.CameraIntent
import com.medtrack.mobile.ui.screen.viewmodel.CameraViewModel
import com.medtrack.mobile.ui.screen.viewmodel.LoginIntent
import com.medtrack.mobile.ui.screen.viewmodel.LoginViewModel
import com.medtrack.mobile.ui.screen.viewmodel.MedicamentoViewModel
import com.medtrack.mobile.ui.screen.viewmodel.SelectedDose
import com.medtrack.mobile.utils.connection.ConnectivityObserver

@Composable
fun AppNavigation(cameraController: CameraController) {
    val navController = rememberNavController()
    val cameraViewModel: CameraViewModel = hiltViewModel()
    val loginViewModel: LoginViewModel = hiltViewModel()
    val medicamentoViewModel: MedicamentoViewModel = hiltViewModel()
    val context = LocalContext.current
    val connectivityObserver = remember { ConnectivityObserver(context) }
    LaunchedEffect(Unit) {
        NavigationManager.events.collect { event ->
            if (event is NavigationEvent.OpenConfirmation) {
                cameraViewModel.openMedicationFromNotification(event.medicamento)
            }
        }
    }
    LaunchedEffect(cameraViewModel) {
        cameraViewModel.events.collect { event ->
            if (event == CameraEvent.NavigateToConfirmation) {
                navController.navigate(AppRoutes.CONFIRMACAO) { launchSingleTop = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.INICIAL,
    ) {
        composable(AppRoutes.INICIAL) {
            TelaInicial(
                onStartClick = { navController.navigate(AppRoutes.LOGIN) },
            )
        }
        composable(AppRoutes.LOGIN) {
            TelaLogin(
                loginViewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(AppRoutes.PRINCIPAL) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
                onForgotPasswordClick = { navController.navigate(AppRoutes.ESQUECI_SENHA) },
            )
        }
        composable(AppRoutes.PRINCIPAL) {
            TelaPrincipal(
                loginViewModel = loginViewModel,
                onHorarioClick = { item ->
                    navController.navigate(
                        AppRoutes.doseHorario(
                            medicamentoId = item.medicamentoId,
                            data = item.date.toString(),
                            horario = item.horario,
                        ),
                    )
                },
            )
        }
        composable(
            AppRoutes.DOSE_HORARIO,
            arguments = listOf(
                navArgument(AppRoutes.Arguments.MEDICATION_ID) { type = NavType.LongType },
                navArgument(AppRoutes.Arguments.DATE) { type = NavType.StringType },
                navArgument(AppRoutes.Arguments.TIME) { type = NavType.StringType },
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "app://telaDose/{medicamentoId}/{data}/{horario}"
                },
            ),
        ) { backStackEntry ->
            val dose = AppRoutes.Dose.parse(
                backStackEntry.arguments?.getLong(AppRoutes.Arguments.MEDICATION_ID),
                backStackEntry.arguments?.getString(AppRoutes.Arguments.DATE),
                backStackEntry.arguments?.getString(AppRoutes.Arguments.TIME),
            ) ?: return@composable

            TelaDoseHorario(
                medicamentoId = dose.medicationId,
                data = dose.date,
                horario = dose.time,
                onBackClick = { navController.popBackStack() },
                onScanClick = {
                    cameraViewModel.onIntent(
                        CameraIntent.SelectDose(SelectedDose(dose.medicationId, dose.date, dose.time)),
                    )
                    navController.navigate(AppRoutes.CAMERA)
                },
            )
        }
        composable(AppRoutes.ESQUECI_SENHA) {
            TelaEsqueciSenha(
                onEmailSent = { navController.navigate(AppRoutes.REDEFINIR_SENHA) },
                onBackToLogin = { navController.popBackStack() },
            )
        }
        composable(AppRoutes.REDEFINIR_SENHA) {
            TelaRedefinirSenha(
                onPasswordReset = {
                    navController.navigate(AppRoutes.PRINCIPAL) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(AppRoutes.CONFIRMACAO) {
            TelaConfirmacao(
                cameraViewModel = cameraViewModel,
                medicamentoViewModel = medicamentoViewModel,
                onConfirmSuccess = {
                    loginViewModel.onIntent(LoginIntent.RefreshConfirmedDoses)
                    navController.navigate(AppRoutes.PRINCIPAL) {
                        popUpTo(AppRoutes.PRINCIPAL) { inclusive = true }
                    }
                },
                onRetakePhoto = { navController.popBackStack() },
            )
        }
        composable(AppRoutes.CAMERA) {
            TelaCamera(
                onBackClick = { navController.popBackStack() },
                viewModel = cameraViewModel,
                cameraController = cameraController,
                connectivityObserver = connectivityObserver,
            )
        }
    }
}
