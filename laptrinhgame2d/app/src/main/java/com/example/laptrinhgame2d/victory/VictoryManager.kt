package com.example.laptrinhgame2d.victory

import android.content.Context
import android.content.SharedPreferences
import com.example.laptrinhgame2d.victory.VictoryRecord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class VictoryManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("victory_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Lưu victory record
    fun saveVictory(record: VictoryRecord) {
        val records = getVictoryRecords().toMutableList()
        records.add(record)

        // Sắp xếp theo thời gian hoàn thành (nhanh nhất lên trước)
        records.sortBy { it.completionTimeMs }

        // Chỉ giữ lại Top 8 kỷ lục nhanh nhất
        val topRecords = if (records.size > 8) {
            records.take(8)
        } else {
            records
        }

        val json = gson.toJson(topRecords)
        prefs.edit().putString("victories", json).apply()
    }

    // Lấy danh sách victory records
    fun getVictoryRecords(): List<VictoryRecord> {
        val json = prefs.getString("victories", null) ?: return emptyList()
        val type = object : TypeToken<List<VictoryRecord>>() {}.type
        return gson.fromJson(json, type)
    }

    // Xóa tất cả lịch sử
    fun clearHistory() {
        prefs.edit().remove("victories").apply()
    }

    // Lấy best time
    fun getBestTime(): VictoryRecord? {
        val records = getVictoryRecords()
        return records.minByOrNull { it.completionTimeMs }
    }

    // Lấy tổng số victory
    fun getTotalVictories(): Int {
        return getVictoryRecords().size
    }
}