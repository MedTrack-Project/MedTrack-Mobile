package com.medtrack.mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.medtrack.mobile.ui.screen.TelaCamera
import com.medtrack.mobile.ui.screen.TelaConfirmacao
import com.medtrack.mobile.ui.screen.TelaDoseHorario
import com.medtrack.mobile.ui.screen.TelaEsqueciSenha
import com.medtrack.mobile.ui.screen.TelaInicial
import com.medtrack.mobile.ui.screen.TelaLogin
import com.medtrack.mobile.ui.screen.TelaPrincipal
import com.medtrack.mobile.ui.screen.TelaRedefinirSenha
import com.medtrack.mobile.ui.screen.viewmodel.CameraViewModel
import com.medtrack.mobile.ui.screen.viewmodel.LoginViewModel
import com.medtrack.mobile.ui.screen.viewmodel.MedicamentoViewModel
import com.medtrack.mobile.utils.connection.ConnectivityObserver
import kotlinx.coroutines.delay

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val cameraViewModel: CameraViewModel = hiltViewModel()
    val loginViewModel: LoginViewModel = hiltViewModel()
    val medicamentoViewModel: MedicamentoViewModel = hiltViewModel()
    val context = LocalContext.current
    val connectivityObserver = remember { ConnectivityObserver(context) }
    val shouldNavigate = NavigationManager.shouldNavigate.collectAsState()
    val shouldNavigateFromCamera by cameraViewModel.navigateToConfirmation.observeAsState(false)

    LaunchedEffect(shouldNavigate.value) {
        shouldNavigate.value?.let { medicamento ->
            delay(300)
            cameraViewModel.atualizarMedicamento(medicamento)
            navController.navigate(AppRoutes.CONFIRMACAO) {
                popUpTo(AppRoutes.INICIAL) { inclusive = true }
                launchSingleTop = true
            }
            NavigationManager.reset()
        }
    }

    LaunchedEffect(shouldNavigateFromCamera) {
        if (shouldNavigateFromCamera) {
            navController.navigate(AppRoutes.CONFIRMACAO)
            cameraViewModel.onNavigationToConfirmationHandled()
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
                navArgument("medicamentoId") { type = NavType.LongType },
                navArgument("data") { type = NavType.StringType },
                navArgument("horario") { type = NavType.StringType },
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "app://telaDose/{medicamentoId}/{data}/{horario}"
                },
            ),
        ) { backStackEntry ->
            val medicamentoId =
                backStackEntry.arguments?.getLong("medicamentoId") ?: return@composable
            val data = backStackEntry.arguments?.getString("data").orEmpty()
            val horario = backStackEntry.arguments?.getString("horario").orEmpty()

            TelaDoseHorario(
                medicamentoId = medicamentoId,
                data = data,
                horario = horario,
                onBackClick = { navController.popBackStack() },
                onScanClick = {
                    cameraViewModel.selecionarDose(medicamentoId, data, horario)
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
                    loginViewModel.carregarDosesConfirmadas()
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
                connectivityObserver = connectivityObserver,
            )
        }
        composable(
            AppRoutes.CAMERA_FROM_NOTIFICATION,
            arguments = listOf(
                navArgument("medicamentoId") { type = NavType.LongType },
                navArgument("horario") { type = NavType.StringType },
            ),
        ) {
            TelaCamera(
                onBackClick = { navController.popBackStack() },
                viewModel = cameraViewModel,
                connectivityObserver = connectivityObserver,
            )
        }
    }
}
