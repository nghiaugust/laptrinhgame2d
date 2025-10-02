package com.example.laptrinhgame2d

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VictoryHistoryAdapter : RecyclerView.Adapter<VictoryHistoryAdapter.ViewHolder>() {
    
    private var records = listOf<VictoryRecord>()
    
    fun submitList(newRecords: List<VictoryRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_victory_history, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(records[position], position + 1)
    }
    
    override fun getItemCount(): Int = records.size
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRank: TextView = itemView.findViewById(R.id.tvRank)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvCharacter: TextView = itemView.findViewById(R.id.tvCharacter)
        private val tvEnemies: TextView = itemView.findViewById(R.id.tvEnemies)
        
        fun bind(record: VictoryRecord, rank: Int) {
            tvRank.text = "#$rank"
            tvTime.text = "⏱ ${record.getFormattedTime()}"
            tvDate.text = record.getFormattedDate()
            tvCharacter.text = "⚔️ ${record.characterType}"
            tvEnemies.text = "💀 ${record.enemiesKilled} kills"
        }
    }
}
