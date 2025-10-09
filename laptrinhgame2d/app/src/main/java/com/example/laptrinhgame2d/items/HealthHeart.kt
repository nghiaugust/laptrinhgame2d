package com.example.laptrinhgame2d.items

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.sqrt

/**
 * Item trái tim - hồi máu cho hero
 * Vẽ bằng code (không cần ảnh)
 */
class HealthHeart(
    private val context: Context,
    private var x: Float,
    private var y: Float,
    private val healAmount: Int = 20  // Hồi 20 HP
) {
    private var isCollected = false
    private var lifetime = 0
    private val maxLifetime = 600  // 10 giây (60 FPS)
    
    // Animation
    private var floatOffset = 0f
    private var floatSpeed = 0.05f
    private var rotation = 0f
    
    // Size
    private val size = 40f
    
    // Paint
    private val heartPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    private val glowPaint = Paint().apply {
        color = Color.argb(100, 255, 100, 100)
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    
    private val shinePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    fun update() {
        if (isCollected) return
        
        lifetime++
        
        // Animation float lên xuống
        floatOffset = kotlin.math.sin(lifetime * floatSpeed) * 8f
        
        // Xoay nhẹ
        rotation += 1f
        if (rotation >= 360f) rotation = 0f
    }
    
    fun draw(canvas: Canvas) {
        if (isCollected) return
        
        canvas.save()
        
        // Di chuyển đến vị trí + float offset
        canvas.translate(x, y + floatOffset)
        
        // Xoay nhẹ
        canvas.rotate(rotation, 0f, 0f)
        
        // Vẽ glow effect (hào quang)
        drawGlow(canvas)
        
        // Vẽ trái tim
        drawHeart(canvas)
        
        // Vẽ shine effect (ánh sáng)
        drawShine(canvas)
        
        canvas.restore()
    }
    
    private fun drawHeart(canvas: Canvas) {
        val path = Path()
        
        // Tạo hình trái tim bằng Path
        // Điểm bắt đầu (đáy tim)
        path.moveTo(0f, size * 0.4f)
        
        // Nửa trái tim bên trái
        path.cubicTo(
            -size * 0.6f, -size * 0.2f,  // Control point 1
            -size * 0.8f, -size * 0.6f,  // Control point 2
            0f, -size * 0.3f              // End point (đỉnh trái)
        )
        
        // Nửa trái tim bên phải
        path.cubicTo(
            size * 0.8f, -size * 0.6f,   // Control point 1
            size * 0.6f, -size * 0.2f,   // Control point 2
            0f, size * 0.4f               // End point (đáy tim)
        )
        
        path.close()
        
        canvas.drawPath(path, heartPaint)
    }
    
    private fun drawGlow(canvas: Canvas) {
        val glowSize = size * 1.2f
        
        // Vẽ 2-3 vòng glow
        for (i in 1..2) {
            glowPaint.alpha = (80 - i * 30).coerceIn(0, 255)
            canvas.drawCircle(0f, 0f, glowSize + i * 8f, glowPaint)
        }
    }
    
    private fun drawShine(canvas: Canvas) {
        // Vẽ điểm sáng nhỏ ở góc trên bên trái
        shinePaint.alpha = (150 + kotlin.math.sin(lifetime * 0.1f) * 105).toInt().coerceIn(0, 255)
        canvas.drawCircle(-size * 0.3f, -size * 0.15f, size * 0.15f, shinePaint)
    }
    
    fun isCollidingWith(targetX: Float, targetY: Float, range: Float): Boolean {
        if (isCollected) return false
        
        val dx = targetX - x
        val dy = targetY - y
        val distance = sqrt(dx * dx + dy * dy)
        return distance < range + size
    }
    
    fun collect() {
        isCollected = true
    }
    
    fun isCollected(): Boolean = isCollected
    
    fun shouldBeRemoved(): Boolean {
        // Xóa nếu đã nhặt hoặc hết thời gian tồn tại
        return isCollected || lifetime >= maxLifetime
    }
    
    fun getHealAmount(): Int = healAmount
    
    fun getX(): Float = x
    fun getY(): Float = y
}
