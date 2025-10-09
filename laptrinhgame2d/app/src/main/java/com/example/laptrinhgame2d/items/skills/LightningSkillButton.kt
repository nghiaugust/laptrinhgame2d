package com.example.laptrinhgame2d.items.skills

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

/**
 * Lightning Skill Button - Nút kỹ năng tia sét
 */
class LightningSkillButton(
    private val context: Context,
    private val x: Float,
    private val y: Float
) {
    private val radius = 80f
    private val paint = Paint().apply {
        isAntiAlias = true
    }
    
    private var isUsed = false
    private var isVisible = true
    
    // Icon
    private var icon: Bitmap? = null
    
    init {
        loadIcon()
    }
    
    private fun loadIcon() {
        try {
            // Sử dụng frame 3 làm icon
            val inputStream = context.assets.open("items/skills/lightning/3.png")
            icon = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun draw(canvas: Canvas) {
        if (!isVisible) return
        
        canvas.save()
        canvas.translate(x, y)
        
        // Vẽ background circle
        paint.color = Color.argb(200, 100, 100, 150)  // Màu tím xanh
        paint.style = Paint.Style.FILL
        canvas.drawCircle(0f, 0f, radius, paint)
        
        // Vẽ border
        paint.color = Color.argb(255, 150, 150, 255)  // Màu tím sáng
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        canvas.drawCircle(0f, 0f, radius, paint)
        
        // Vẽ icon
        icon?.let { bitmap ->
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
        
        canvas.restore()
    }
    
    fun update() {
        // Không cần update gì đặc biệt
    }
    
    fun isPressed(touchX: Float, touchY: Float): Boolean {
        if (!isVisible || isUsed) return false
        
        val dx = touchX - x
        val dy = touchY - y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
        
        return distance <= radius
    }
    
    fun onTouch(pointerId: Int) {
        // Handle touch
    }
    
    fun isReady(): Boolean = !isUsed && isVisible
    
    fun markAsUsed() {
        isUsed = true
        isVisible = false
    }
    
    fun cleanup() {
        icon?.recycle()
        icon = null
    }
}
