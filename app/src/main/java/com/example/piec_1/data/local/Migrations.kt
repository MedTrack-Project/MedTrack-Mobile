package com.example.piec_1.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE medicamentos ADD COLUMN usoContinuo INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE notificacoes(
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                medicamentoId INTEGER NOT NULL,
                horario TEXT NOT NULL,
                dataAgendamento TEXT NOT NULL,
                exibida INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(medicamentoId) REFERENCES medicamentos(id)
            )
            """.trimIndent(),
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `confirmacoes` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `medicamentoId` INTEGER NOT NULL,
                `horario` TEXT NOT NULL,
                `data` TEXT NOT NULL,
                `foiTomado` INTEGER NOT NULL,
                `observacao` TEXT,
                `sincronizado` INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(`medicamentoId`) REFERENCES `medicamentos`(`id`) ON DELETE NO ACTION
            )
            """.trimIndent(),
        )
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS medicamentos_v2 (
                id INTEGER NOT NULL PRIMARY KEY,
                nome TEXT NOT NULL,
                compostoAtivo TEXT NOT NULL,
                dosagem TEXT NOT NULL,
                freq_frequenciaUsoTipo TEXT NOT NULL,
                freq_usoContinuo INTEGER NOT NULL,
                freq_horariosEspecificos TEXT NOT NULL,
                freq_intervaloHoras INTEGER,
                freq_primeiroHorario TEXT,
                freq_dataInicio TEXT,
                freq_dataTermino TEXT
            )
            """.trimIndent(),
        )
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys=OFF")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS confirmacoes_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                medicamentoId INTEGER NOT NULL,
                horario TEXT NOT NULL,
                data TEXT NOT NULL,
                foiTomado INTEGER NOT NULL,
                observacao TEXT,
                sincronizado INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(medicamentoId) REFERENCES medicamentos_v2(id) ON DELETE NO ACTION
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            INSERT INTO confirmacoes_new (id, medicamentoId, horario, data, foiTomado, observacao, sincronizado)
            SELECT id, medicamentoId, horario, data, foiTomado, observacao, sincronizado
            FROM confirmacoes
            WHERE EXISTS (
                SELECT 1 FROM medicamentos_v2 WHERE medicamentos_v2.id = confirmacoes.medicamentoId
            )
            """.trimIndent(),
        )

        db.execSQL("DROP TABLE confirmacoes")
        db.execSQL("ALTER TABLE confirmacoes_new RENAME TO confirmacoes")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_confirmacoes_medicamentoId ON confirmacoes(medicamentoId)",
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS notificacoes_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                medicamentoId INTEGER NOT NULL,
                horario TEXT NOT NULL,
                dataAgendamento TEXT NOT NULL,
                exibida INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(medicamentoId) REFERENCES medicamentos_v2(id) ON DELETE NO ACTION
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            INSERT INTO notificacoes_new (id, medicamentoId, horario, dataAgendamento, exibida)
            SELECT id, medicamentoId, horario, dataAgendamento, exibida
            FROM notificacoes
            WHERE EXISTS (
                SELECT 1 FROM medicamentos_v2 WHERE medicamentos_v2.id = notificacoes.medicamentoId
            )
            """.trimIndent(),
        )

        db.execSQL("DROP TABLE notificacoes")
        db.execSQL("ALTER TABLE notificacoes_new RENAME TO notificacoes")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_notificacoes_medicamentoId ON notificacoes(medicamentoId)",
        )

        db.execSQL("DROP TABLE IF EXISTS medicamentos")

        db.execSQL("PRAGMA foreign_keys=ON")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE medicamentos_v2 ADD COLUMN imagemUrl TEXT")
    }
}
