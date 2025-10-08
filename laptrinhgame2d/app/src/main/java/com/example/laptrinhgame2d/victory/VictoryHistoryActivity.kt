package com.example.laptrinhgame2d.victory

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.laptrinhgame2d.R

class VictoryHistoryActivity : AppCompatActivity() {

    private lateinit var victoryManager: VictoryManager
    private lateinit var adapter: VictoryHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_victory_history)

        supportActionBar?.hide()

        victoryManager = VictoryManager(this)

        val rvHistory = findViewById<RecyclerView>(R.id.rvHistory)
        val tvTotal = findViewById<TextView>(R.id.tvTotalVictories)
        val btnBack = findViewById<Button>(R.id.btnBack)

        adapter = VictoryHistoryAdapter()
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = adapter

        loadHistory()

        btnBack.setOnClickListener {
            setResult(RESULT_OK)
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