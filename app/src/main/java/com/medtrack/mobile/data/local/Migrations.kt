package com.medtrack.mobile.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE medicamentos_v2 ADD COLUMN imagemUrl TEXT")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE scan_queue ADD COLUMN idempotencyKey TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE scan_queue ADD COLUMN attemptCount INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE scan_queue ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE scan_queue ADD COLUMN lastError TEXT")
        db.execSQL(
            "UPDATE scan_queue SET idempotencyKey = 'legacy-' || id, updatedAt = timestamp " +
                "WHERE idempotencyKey = ''",
        )
        db.execSQL(
            "UPDATE scan_queue SET status = CASE status " +
                "WHEN 'PENDENTE' THEN 'PENDING' WHEN 'CONCLUIDO' THEN 'COMPLETED' ELSE 'FAILED' END",
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_scan_queue_idempotencyKey " +
                "ON scan_queue(idempotencyKey)",
        )
    }
}

val SUPPORTED_MIGRATIONS = arrayOf(MIGRATION_8_9, MIGRATION_9_10)
