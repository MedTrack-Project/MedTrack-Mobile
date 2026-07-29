package com.example.piec_1.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.piec_1.data.local.daos.ConfirmacaoDao
import com.example.piec_1.data.local.daos.MedicamentoV2Dao
import com.example.piec_1.data.local.daos.NotificacaoDao
import com.example.piec_1.data.local.daos.ScanQueueDao
import com.example.piec_1.data.local.daos.UsuarioDao
import com.example.piec_1.data.local.entity.ConfirmacaoEntity
import com.example.piec_1.data.local.entity.MedicamentoEntity
import com.example.piec_1.data.local.entity.NotificacaoEntity
import com.example.piec_1.data.local.entity.ScanQueueItem
import com.example.piec_1.data.local.entity.UsuarioEntity
@Database(
    entities = [
        UsuarioEntity::class,
        MedicamentoEntity::class,
        NotificacaoEntity::class,
        ConfirmacaoEntity::class,
        ScanQueueItem::class,
    ],
    version = 9,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun medicamentoV2Dao(): MedicamentoV2Dao
    abstract fun notificacaoDao(): NotificacaoDao
    abstract fun confirmacaoDao(): ConfirmacaoDao
    abstract fun scanQueueDao(): ScanQueueDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database_db",
            )
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9,
                )
                .fallbackToDestructiveMigration(false)
                .build()
            INSTANCE = instance
            instance
        }
    }
}
