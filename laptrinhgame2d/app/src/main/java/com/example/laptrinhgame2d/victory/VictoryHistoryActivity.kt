package com.example.laptrinhgame2d.victory

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.laptrinhgame2d.CharacterSelectionActivity
import com.example.laptrinhgame2d.R
import com.example.laptrinhgame2d.victory.VictoryHistoryAdapter
import com.example.laptrinhgame2d.victory.VictoryManager

class VictoryHistoryActivity : AppCompatActivity() {

    private lateinit var victoryManager: VictoryManager
    private lateinit var adapter: VictoryHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_victory_history)

        // Ẩn action bar
        supportActionBar?.hide()

        victoryManager = VictoryManager(this)

        val rvHistory = findViewById<RecyclerView>(R.id.rvHistory)
        val tvTotal = findViewById<TextView>(R.id.tvTotalVictories)
        val btnBack = findViewById<Button>(R.id.btnBack)

        // Setup RecyclerView
        adapter = VictoryHistoryAdapter()
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = adapter

        // Load data
        loadHistory()

        // Back button - Quay về giao diện chọn nhân vật
        btnBack.setOnClickListener {
            val intent = Intent(this, CharacterSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadHistory() {
        val records = victoryManager.getVictoryRecords()
        val tvTotal = findViewById<TextView>(R.id.tvTotalVictories)

        tvTotal.text = "Total Victories: ${records.size}"
        adapter.submitList(records)
    }
}