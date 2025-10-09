package com.example.laptrinhgame2d.items.skills

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import kotlin.math.sqrt

/**
 * UI Button cho Bomb Skill
 * Chỉ dùng 1 lần, sau đó ẩn nút
 */
class BombSkillButton(
    private val context: Context,
    var x: Float,
    var y: Float,
    private val radius: Float = 80f
) {
    private var isPressed = false
    private var pointerId: Int = -1
    
    private var isUsed = false       // Đã dùng chưa
    private var isVisible = true     // Hiển thị hay ẩn
    
    // Icon bitmap (frame 4)
    private var iconBitmap: Bitmap? = null
    
    // Paint
    private val bgPaint = Paint().apply {
        color = android.graphics.Color.argb(200, 80, 40, 20)
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    private val edgePaint = Paint().apply {
        color = android.graphics.Color.argb(200, 255, 100, 50)
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    
    init {
        loadIcon()
    }
    
    private fun loadIcon() {
        try {
            // Load frame 4 làm icon
            val inputStream = context.assets.open("items/skills/boom1/4.png")
            iconBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun update() {
        // No animation needed
    }
    
    fun draw(canvas: Canvas) {
        // Không vẽ nếu đã dùng hoặc bị ẩn
        if (isUsed || !isVisible) return
        
        canvas.save()
        canvas.translate(x, y)
        
        // Vẽ background circle
        canvas.drawCircle(0f, 0f, radius, bgPaint)
        
        // Vẽ icon bomb
        iconBitmap?.let { bitmap ->
            val iconSize = radius * 1.2f
            canvas.drawBitmap(
                bitmap,
                null,
                android.graphics.RectF(
                    -iconSize / 2,
                    -iconSize / 2,
                    iconSize / 2,
                    iconSize / 2
                ),
                null
            )
        }
        
        // Vẽ viền
        edgePaint.alpha = if (isPressed) 255 else 200
        edgePaint.strokeWidth = if (isPressed) 5f else 4f
        canvas.drawCircle(0f, 0f, radius, edgePaint)
        
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
    
    fun cleanup() {
        iconBitmap?.recycle()
        iconBitmap = null
    }
}
