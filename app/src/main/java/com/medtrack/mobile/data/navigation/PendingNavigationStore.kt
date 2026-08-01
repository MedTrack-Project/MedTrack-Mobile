package com.medtrack.mobile.data.navigation

import android.content.Context
import com.google.gson.Gson
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingNavigationStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val gson: Gson,
) {
    fun save(medicamento: MedicamentoCapturadoDomain): String {
        val reference = UUID.randomUUID().toString()
        directory().resolve(reference).writeText(gson.toJson(medicamento))
        return reference
    }

    fun consume(reference: String): MedicamentoCapturadoDomain? {
        val file = directory().resolve(reference)
        if (!file.isFile) return null
        return runCatching { gson.fromJson(file.readText(), MedicamentoCapturadoDomain::class.java) }
            .also { file.delete() }
            .getOrNull()
    }

    private fun directory(): File = File(context.filesDir, "pending_navigation").apply { mkdirs() }
}
