package com.medtrack.mobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.medtrack.mobile.data.local.daos.ConfirmacaoDao
import com.medtrack.mobile.data.local.daos.MedicamentoV2Dao
import com.medtrack.mobile.data.local.daos.NotificacaoDao
import com.medtrack.mobile.data.local.daos.ScanQueueDao
import com.medtrack.mobile.data.local.daos.UsuarioDao
import com.medtrack.mobile.data.local.entity.ConfirmacaoEntity
import com.medtrack.mobile.data.local.entity.MedicamentoEntity
import com.medtrack.mobile.data.local.entity.NotificacaoEntity
import com.medtrack.mobile.data.local.entity.ScanQueueItem
import com.medtrack.mobile.data.local.entity.UsuarioEntity
@Database(
    entities = [
        UsuarioEntity::class,
        MedicamentoEntity::class,
        NotificacaoEntity::class,
        ConfirmacaoEntity::class,
        ScanQueueItem::class,
    ],
    version = 10,
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
                .addMigrations(*SUPPORTED_MIGRATIONS)
                .fallbackToDestructiveMigration(false)
                .build()
            INSTANCE = instance
            instance
        }
    }
}
