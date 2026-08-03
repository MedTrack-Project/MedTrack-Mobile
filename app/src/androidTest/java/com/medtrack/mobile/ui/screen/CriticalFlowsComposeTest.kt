package com.medtrack.mobile.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.v2.createComposeRule
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
        composeRule.onNode(isHeading() and androidx.compose.ui.test.hasText("Entrar")).assertIsDisplayed()
        composeRule.onNode(hasContentDescription("MedTrack")).assertIsDisplayed()
        composeRule.onNode(hasClickAction() and androidx.compose.ui.test.hasText("Entrar")).assertIsNotEnabled()
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

    @Test
    fun confirmationFailureDisplaysActionableGuidance() {
        composeRule.setContent {
            PIEC1Theme {
                StatusCard(
                    medicamento = MedicamentoCapturadoDomain("", "", "", "", ""),
                    isSuccess = false,
                )
            }
        }

        composeRule.onNodeWithText("Falha na Identificação").assertIsDisplayed()
        composeRule.onNodeWithText("Dica: Tente focar melhor o texto e evite reflexos.").assertIsDisplayed()
    }
}
