package com.example.laptrinhgame2d

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class GameButton(
    var x: Float,
    var y: Float,
    private val radius: Float,
    private val label: String,
    private val color: Int
) {
    var pointerId = -1
    private var isPressed = false
    
    private val paint = Paint().apply {
        style = Paint.Style.FILL
    }
    
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }
    
    private val strokePaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    
    fun onTouch(pId: Int) {
        pointerId = pId
        isPressed = true
    }
    
    fun reset() {
        pointerId = -1
        isPressed = false
    }
    
    fun isPressed(touchX: Float, touchY: Float): Boolean {
        val dx = touchX - x
        val dy = touchY - y
        return Math.sqrt((dx * dx + dy * dy).toDouble()) < radius
    }
    
    fun draw(canvas: Canvas) {
        // Vẽ nút với màu đậm hơn khi được nhấn
        paint.color = if (isPressed) {
            darkenColor(color, 0.7f)
        } else {
            color
        }
        
        canvas.drawCircle(x, y, radius, paint)
        canvas.drawCircle(x, y, radius, strokePaint)
        
        // Vẽ text
        canvas.drawText(label, x, y + 15f, textPaint)
    }
    
    private fun darkenColor(color: Int, factor: Float): Int {
        val r = (Color.red(color) * factor).toInt()
        val g = (Color.green(color) * factor).toInt()
        val b = (Color.blue(color) * factor).toInt()
        return Color.rgb(r, g, b)
    }
}
