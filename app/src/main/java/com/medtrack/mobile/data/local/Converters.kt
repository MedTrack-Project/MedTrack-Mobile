package com.medtrack.mobile.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtrack.mobile.data.local.entity.ScanQueueStatus

class Converters {
    @TypeConverter
    fun fromScanQueueStatus(value: ScanQueueStatus): String = value.name

    @TypeConverter
    fun toScanQueueStatus(value: String): ScanQueueStatus = runCatching { ScanQueueStatus.valueOf(value) }
        .getOrDefault(ScanQueueStatus.FAILED)

    @TypeConverter
    fun fromStringList(value: List<String>): String = Gson().toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
