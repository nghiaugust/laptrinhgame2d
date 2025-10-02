package com.example.laptrinhgame2d

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView

class VictoryDialog(
    context: Context,
    private val victoryRecord: VictoryRecord,
    private val onViewHistory: () -> Unit,
    private val onPlayAgain: () -> Unit,
    private val onMainMenu: () -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_victory)
        setCancelable(false)
        
        // Display stats
        findViewById<TextView>(R.id.tvCompletionTime).text = "⏱ Time: ${victoryRecord.getFormattedTime()}"
        findViewById<TextView>(R.id.tvCharacterUsed).text = "⚔️ Hero: ${victoryRecord.characterType}"
        findViewById<TextView>(R.id.tvEnemiesKilled).text = "💀 Enemies Defeated: ${victoryRecord.enemiesKilled}"
        
        // View History button
        findViewById<Button>(R.id.btnViewHistory).setOnClickListener {
            dismiss()
            onViewHistory()
        }
        
        // Play Again button
        findViewById<Button>(R.id.btnPlayAgain).setOnClickListener {
            dismiss()
            onPlayAgain()
        }
        
        // Main Menu button
        findViewById<Button>(R.id.btnMainMenu).setOnClickListener {
            dismiss()
            onMainMenu()
        }
    }
}
