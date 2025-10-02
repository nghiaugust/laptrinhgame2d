package com.example.laptrinhgame2d

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.Button

class GameOverDialog(
    private val context: Context,
    private val onContinue: () -> Unit,
    private val onRestart: () -> Unit
) {
    
    private var dialog: Dialog? = null
    
    fun show() {
        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.game_over_overlay)
            setCancelable(false)
            
            // Làm cho background trong suốt
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            
            val continueButton = findViewById<Button>(R.id.continueButton)
            val restartButton = findViewById<Button>(R.id.restartButton)
            val exitButton = findViewById<Button>(R.id.exitButton)
            
            // Continue - Hồi đầy máu và tiếp tục
            continueButton.setOnClickListener {
                dismiss()
                onContinue()
            }
            
            // Restart - Về màn hình chọn nhân vật
            restartButton.setOnClickListener {
                dismiss()
                onRestart()
                
                // Chuyển về CharacterSelectionActivity
                val intent = Intent(context, CharacterSelectionActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                
                // Đóng MainActivity hiện tại
                (context as? Activity)?.finish()
            }
            
            // Exit - Thoát game
            exitButton.setOnClickListener {
                dismiss()
                
                // Đóng activity hiện tại
                val activity = context as? Activity
                activity?.finishAffinity() // Đóng tất cả activities trong stack
                
                // Thoát ứng dụng hoàn toàn
                android.os.Process.killProcess(android.os.Process.myPid())
            }
            
            show()
        }
    }
    
    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }
    
    fun isShowing(): Boolean {
        return dialog?.isShowing == true
    }
}
