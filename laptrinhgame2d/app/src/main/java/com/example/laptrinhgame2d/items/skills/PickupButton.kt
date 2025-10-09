package com.example.laptrinhgame2d.items.skills

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Nút nhặt vật phẩm (hiện khi player đến gần skill item)
 */
class PickupButton(
    var x: Float,
    var y: Float,
    private val radius: Float = 70f,
    private val text: String = "NHẶT"
) {
    private var isPressed = false
    private var pointerId: Int = -1
    private var animationFrame = 0
    private var isVisible = false
    
    // Paint
    private val bgPaint = Paint().apply {
        color = Color.argb(200, 50, 200, 50)
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    private val edgePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }
    
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 35f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }
    
    private val iconPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }
    
    fun update() {
        animationFrame++
    }
    
    fun draw(canvas: Canvas) {
        if (!isVisible) return
        
        canvas.save()
        canvas.translate(x, y)
        
        // Pulse animation
        val pulseScale = 1f + sin(animationFrame * 0.15f) * 0.1f
        canvas.scale(pulseScale, pulseScale)
        
        // Vẽ background
        if (isPressed) {
            bgPaint.color = Color.argb(255, 30, 150, 30)
        } else {
            bgPaint.color = Color.argb(200, 50, 200, 50)
        }
        canvas.drawCircle(0f, 0f, radius, bgPaint)
        
        // Vẽ viền
        canvas.drawCircle(0f, 0f, radius, edgePaint)
        
        // Vẽ icon hand (bàn tay nhặt)
        drawHandIcon(canvas)
        
        // Vẽ text
        canvas.drawText(text, 0f, radius + 40f, textPaint)
        
        canvas.restore()
    }
    
    private fun drawHandIcon(canvas: Canvas) {
        // Vẽ icon bàn tay đơn giản
        iconPaint.style = Paint.Style.STROKE
        iconPaint.strokeWidth = 4f
        
        // Palm (lòng bàn tay)
        canvas.drawCircle(0f, 5f, 20f, iconPaint)
        
        // Fingers (ngón tay)
        for (i in 0 until 4) {
            val startX = -15f + i * 10f
            val startY = 5f
            val endX = startX
            val endY = startY - 20f
            canvas.drawLine(startX, startY, endX, endY, iconPaint)
        }
        
        // Thumb (ngón cái)
        canvas.drawLine(-20f, 10f, -30f, 5f, iconPaint)
    }
    
    fun isPressed(touchX: Float, touchY: Float): Boolean {
        if (!isVisible) return false
        
        val dx = touchX - x
        val dy = touchY - y
        return sqrt(dx * dx + dy * dy) <= radius
    }
    
    fun onTouch(id: Int) {
        isPressed = true
        pointerId = id
    }
    
    fun reset() {
        isPressed = false
        pointerId = -1
    }
    
    fun show(posX: Float, posY: Float) {
        x = posX
        y = posY
        isVisible = true
    }
    
    fun hide() {
        isVisible = false
    }
    
    fun isVisible(): Boolean = isVisible
}
