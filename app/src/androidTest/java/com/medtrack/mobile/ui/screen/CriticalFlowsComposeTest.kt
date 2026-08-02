package com.medtrack.mobile.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.model.Usuario
import com.medtrack.mobile.ui.components.StatusCard
import com.medtrack.mobile.ui.screen.viewmodel.LoginUiState
import com.medtrack.mobile.ui.theme.PIEC1Theme
import org.junit.Rule
import org.junit.Test

class CriticalFlowsComposeTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun loginDisplaysAuthenticationError() {
        composeRule.setContent {
            PIEC1Theme {
                LoginContent(
                    state = LoginUiState(errorMessage = "Usuario ou senha invalidos"),
                    onIntent = {},
                    onForgotPasswordClick = {},
                )
            }
        }
        composeRule.onNodeWithText("Usuario ou senha invalidos").assertIsDisplayed()
    }

    @Test
    fun medicationListDisplaysEmptyState() {
        composeRule.setContent {
            PIEC1Theme {
                PrincipalContent(
                    state = LoginUiState(usuario = Usuario(1, "Yann", "yann@example.test", "yann")),
                    onHorarioClick = {},
                )
            }
        }
        composeRule.onNodeWithText("Nenhum medicamento para exibir").assertIsDisplayed()
    }

    @Test
    fun confirmationDisplaysDetectedMedication() {
        composeRule.setContent {
            PIEC1Theme {
                StatusCard(
                    medicamento = MedicamentoCapturadoDomain(
                        nome = "Losartana",
                        compostoAtivo = "Losartana Potassica",
                        dosagem = "50mg",
                        quantidade = "30",
                        validade = "2027-01",
                    ),
                    isSuccess = true,
                )
            }
        }
        composeRule.onNodeWithText("Nome: Losartana").assertIsDisplayed()
    }
}
