package com.example.laptrinhgame2d

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class DamageText(
    private var x: Float,
    private var y: Float,
    private val damage: Int
) {
    private var alpha = 255
    private var lifeTime = 60 // 1 second at 60 FPS
    private var currentLife = 0
    
    private val paint = Paint().apply {
        color = Color.RED
        textSize = 36f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
        setShadowLayer(3f, 2f, 2f, Color.BLACK)
    }
    
    fun update() {
        currentLife++
        y -= 2f // Bay lên
        
        // Fade out
        alpha = ((1f - currentLife.toFloat() / lifeTime) * 255).toInt().coerceIn(0, 255)
    }
    
    fun draw(canvas: Canvas) {
        paint.alpha = alpha
        canvas.drawText("-$damage", x, y, paint)
    }
    
    fun isFinished(): Boolean {
        return currentLife >= lifeTime
    }
}
