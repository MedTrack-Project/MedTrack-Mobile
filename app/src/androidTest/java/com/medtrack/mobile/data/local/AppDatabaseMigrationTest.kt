package com.medtrack.mobile.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun migrate8To10PreservesMedicationAndAddsQueueContract() {
        helper.createDatabase(DATABASE_NAME_8, 8).apply {
            execSQL(
                "INSERT INTO usuario(id,nome,email,nomeUsuario) VALUES(7,'Yann','yann@example.test','yann')",
            )
            execSQL(
                "INSERT INTO medicamentos_v2(" +
                    "id,nome,compostoAtivo,dosagem,freq_frequenciaUsoTipo,freq_usoContinuo," +
                    "freq_horariosEspecificos) VALUES(10,'Losartana','Losartana Potassica','50mg'," +
                    "'HORARIOS_ESPECIFICOS',1,'[\"08:00\"]')",
            )
            close()
        }

        helper.runMigrationsAndValidate(DATABASE_NAME_8, 10, true, MIGRATION_8_9, MIGRATION_9_10).use { db ->
            db.query("SELECT nome, imagemUrl FROM medicamentos_v2 WHERE id=10").use { cursor ->
                cursor.moveToFirst()
                assertEquals("Losartana", cursor.getString(0))
                assertEquals(null, cursor.getString(1))
            }
        }
    }

    @Test
    fun migrate9To10PreservesPendingScanAndCreatesIdempotencyIndex() {
        helper.createDatabase(DATABASE_NAME_9, 9).apply {
            execSQL("INSERT INTO scan_queue(id,imagePath,status,timestamp) VALUES(3,'file:///scan.jpg','PENDENTE',123)")
            close()
        }

        helper.runMigrationsAndValidate(DATABASE_NAME_9, 10, true, MIGRATION_9_10).use { db ->
            db.query(
                "SELECT imagePath,status,idempotencyKey,attemptCount,updatedAt FROM scan_queue WHERE id=3",
            ).use { cursor ->
                cursor.moveToFirst()
                assertEquals("file:///scan.jpg", cursor.getString(0))
                assertEquals("PENDING", cursor.getString(1))
                assertEquals("legacy-3", cursor.getString(2))
                assertEquals(0, cursor.getInt(3))
                assertEquals(123, cursor.getLong(4))
            }
        }
    }

    private companion object {
        const val DATABASE_NAME_8 = "migration-8-10"
        const val DATABASE_NAME_9 = "migration-9-10"
    }
}
