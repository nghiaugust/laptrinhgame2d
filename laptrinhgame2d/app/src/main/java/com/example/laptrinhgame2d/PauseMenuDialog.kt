package com.example.laptrinhgame2d

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.Button

class PauseMenuDialog(
    context: Context,
    private val onContinue: () -> Unit,
    private val onRestart: () -> Unit,
    private val onCharacterSelect: () -> Unit,
    private val onMainMenu: () -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_pause_menu)
        setCancelable(false)
        
        // Continue button
        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            onContinue()
            dismiss()
        }
        
        // Restart button
        findViewById<Button>(R.id.btnRestart).setOnClickListener {
            dismiss()
            onRestart()
        }
        
        // Character Selection button
        findViewById<Button>(R.id.btnCharacterSelect).setOnClickListener {
            dismiss()
            onCharacterSelect()
        }
        
        // Main Menu button
        findViewById<Button>(R.id.btnMainMenu).setOnClickListener {
            dismiss()
            onMainMenu()
        }
    }
}
