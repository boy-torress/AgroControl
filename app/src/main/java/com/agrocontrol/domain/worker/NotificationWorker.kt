package com.agrocontrol.domain.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.agrocontrol.data.repository.AlertaRepository
import com.agrocontrol.data.repository.SessionManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val alertaRepo: AlertaRepository,
    private val sessionManager: SessionManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val userId = sessionManager.userId.firstOrNull() ?: return Result.success()
            val unreadCount = alertaRepo.countAlertasNoLeidas(userId).firstOrNull() ?: 0

            if (unreadCount > 0) {
                val helper = NotificationHelper(context)
                helper.showNotification(
                    id = 1001,
                    title = "🚜 AgroControl",
                    content = "Tienes $unreadCount alertas nuevas sobre tu cultivo. ¡Abre la app para revisarlas!"
                )
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}
