package com.example.laptrinhgame2d.victory

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class VictoryRecord(
    val id: Long = System.currentTimeMillis(),
    val completionTimeMs: Long, // Thời gian hoàn thành (milliseconds)
    val characterType: String, // Fighter, Samurai_Archer, etc.
    val enemiesKilled: Int, // Số quái đã giết
    val timestamp: Long = System.currentTimeMillis() // Thời điểm chiến thắng
) {
    // Format thời gian hoàn thành thành MM:SS
    fun getFormattedTime(): String {
        val minutes = (completionTimeMs / 1000) / 60
        val seconds = (completionTimeMs / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    // Format ngày tháng
    fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}