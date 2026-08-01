package com.medtrack.mobile.ui.navigation

import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object NavigationManager {
    private val eventsChannel = Channel<NavigationEvent>(Channel.BUFFERED)
    val events = eventsChannel.receiveAsFlow()

    fun openConfirmation(medicamento: MedicamentoCapturadoDomain) {
        eventsChannel.trySend(NavigationEvent.OpenConfirmation(medicamento))
    }
}

sealed interface NavigationEvent {
    data class OpenConfirmation(val medicamento: MedicamentoCapturadoDomain) : NavigationEvent
}
