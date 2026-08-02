# Notificações e reagendamento

Lembretes usam IDs estáveis e conteúdo `VISIBILITY_PRIVATE`. O receiver de alarme apenas valida o
payload e publica a notificação de forma síncrona; não cria `CoroutineScope` sem lifecycle.

Full-screen intent é anexado somente quando `NotificationManagerCompat.canUseFullScreenIntent()`
autoriza. Sem autorização, Android apresenta o fallback heads-up normal com o mesmo deep link.
Full-screen permanece reservado aos lembretes configurados pelo usuário como alarme.

`ScheduleChangeReceiver` agenda `RescheduleNotificationsWorker` após boot, atualização do pacote,
mudança manual de horário ou timezone. O Worker relê medicamentos da source of truth Room e recria os
agendamentos usando Hilt.

Referências: [restrições de full-screen no Android 14](https://developer.android.com/about/versions/14/behavior-changes-14) e [NotificationManagerCompat.canUseFullScreenIntent](https://developer.android.com/reference/androidx/core/app/NotificationManagerCompat#canUseFullScreenIntent()).
