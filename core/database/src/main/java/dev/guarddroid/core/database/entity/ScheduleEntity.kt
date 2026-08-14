package dev.guarddroid.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val enabledDays: Int, // Bitmask: 1=Mo, 2=Di, 4=Mi, 8=Do, 16=Fr, 32=Sa, 64=So
    val timeWindowsJson: String, // JSON: [{"startHour":6,"startMinute":0,"endHour":19,"endMinute":0}]
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class TimeWindow(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int
)
