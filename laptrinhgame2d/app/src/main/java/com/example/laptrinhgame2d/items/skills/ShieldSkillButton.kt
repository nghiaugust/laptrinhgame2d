package com.example.laptrinhgame2d.items.skills

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * UI Button cho Shield Skill
 * Chỉ dùng 1 lần, sau đó ẩn nút
 */
class ShieldSkillButton(
    private val context: Context,
    var x: Float,
    var y: Float,
    private val radius: Float = 80f
) {
    private var isPressed = false
    private var pointerId: Int = -1
    
    private var isUsed = false       // Đã dùng chưa
    private var isVisible = true     // Hiển thị hay ẩn
    private var animationFrame = 0
    
    // Paint
    private val bgPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    private val iconPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val edgePaint = Paint().apply {
        color = Color.argb(200, 100, 200, 255)
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    
    fun update() {
        animationFrame++
    }
    
    fun draw(canvas: Canvas) {
        // Không vẽ nếu đã dùng hoặc bị ẩn
        if (isUsed || !isVisible) return
        
        canvas.save()
        canvas.translate(x, y)
        
        // Vẽ background
        val gradient = RadialGradient(
            0f, 0f, radius,
            intArrayOf(
                Color.argb(200, 80, 160, 240),
                Color.argb(150, 40, 100, 180)
            ),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        bgPaint.shader = gradient
        canvas.drawCircle(0f, 0f, radius, bgPaint)
        
        // Vẽ icon khiên mini
        drawMiniShield(canvas)
        
        // Vẽ viền
        edgePaint.alpha = if (isPressed) 255 else 200
        edgePaint.strokeWidth = if (isPressed) 5f else 4f
        canvas.drawCircle(0f, 0f, radius, edgePaint)
        
        canvas.restore()
    }
    
    private fun drawMiniShield(canvas: Canvas) {
        val size = radius * 0.5f
        val pulseScale = 1f + sin(animationFrame * 0.1f) * 0.08f
        
        canvas.save()
        canvas.scale(pulseScale, pulseScale)
        
        // Vẽ khiên mini
        iconPaint.style = Paint.Style.FILL
        
        // Gradient background
        val gradient = RadialGradient(
            0f, 0f, size,
            intArrayOf(
                Color.argb(255, 120, 200, 255),
                Color.argb(200, 80, 160, 230),
                Color.argb(100, 50, 120, 200)
            ),
            floatArrayOf(0f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        iconPaint.shader = gradient
        canvas.drawCircle(0f, 0f, size, iconPaint)
        
        // Vẽ hình khiên (hình chữ nhật bo góc)
        iconPaint.shader = null
        iconPaint.color = Color.argb(220, 100, 180, 255)
        val w = size * 0.6f
        val h = size * 0.8f
        canvas.drawRoundRect(-w/2, -h/2, w/2, h/2, 8f, 8f, iconPaint)
        
        // Viền khiên
        iconPaint.style = Paint.Style.STROKE
        iconPaint.strokeWidth = 2.5f
        iconPaint.color = Color.argb(255, 180, 230, 255)
        canvas.drawRoundRect(-w/2, -h/2, w/2, h/2, 8f, 8f, iconPaint)
        
        // Dấu + ở giữa
        iconPaint.strokeWidth = 3f
        iconPaint.color = Color.argb(255, 220, 245, 255)
        canvas.drawLine(-size*0.2f, 0f, size*0.2f, 0f, iconPaint)
        canvas.drawLine(0f, -size*0.25f, 0f, size*0.25f, iconPaint)
        
        canvas.restore()
    }
    
    fun isPressed(touchX: Float, touchY: Float): Boolean {
        // Không thể bấm nếu đã dùng hoặc bị ẩn
        if (isUsed || !isVisible) return false
        
        val dx = touchX - x
        val dy = touchY - y
        return sqrt(dx * dx + dy * dy) <= radius
    }
    
    fun onTouch(id: Int) {
        if (isUsed || !isVisible) return  // Không thể bấm nếu đã dùng
        
        isPressed = true
        pointerId = id
    }
    
    fun reset() {
        isPressed = false
        pointerId = -1
    }
    
    /**
     * Đánh dấu skill đã được sử dụng -> Ẩn nút
     */
    fun markAsUsed() {
        isUsed = true
        isVisible = false
    }
    
    fun isReady(): Boolean = !isUsed && isVisible
}
