package com.example.laptrinhgame2d.items.skills

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * UI Button cho Black Hole Skill
 * Chỉ dùng 1 lần, sau đó ẩn nút
 */
class BlackHoleSkillButton(
    var x: Float,
    var y: Float,
    private val radius: Float = 80f
) {
    private var isPressed = false
    private var pointerId: Int = -1
    
    // COOLDOWN SYSTEM - COMMENTED OUT (dùng 1 lần thay vì cooldown)
    // private var cooldownTimer = 0
    // private val cooldownDuration = 600  // 10 giây (60 FPS)
    
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
        color = Color.argb(200, 200, 100, 255)
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    
    private val cooldownPaint = Paint().apply {
        color = Color.argb(180, 50, 50, 50)
        style = Paint.Style.FILL
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
        
        // COOLDOWN UPDATE - COMMENTED OUT
        // if (cooldownTimer > 0) {
        //     cooldownTimer--
        // }
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
                Color.argb(200, 80, 40, 120),
                Color.argb(150, 40, 20, 80)
            ),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        bgPaint.shader = gradient
        canvas.drawCircle(0f, 0f, radius, bgPaint)
        
        // Vẽ icon hố đen mini
        drawMiniBlackHole(canvas)
        
        // Vẽ viền
        edgePaint.alpha = if (isPressed) 255 else 200
        canvas.drawCircle(0f, 0f, radius, edgePaint)
        
        // COOLDOWN OVERLAY - COMMENTED OUT
        // if (cooldownTimer > 0) {
        //     canvas.drawCircle(0f, 0f, radius, cooldownPaint)
        //     
        //     // Hiển thị thời gian cooldown
        //     val seconds = (cooldownTimer / 60) + 1
        //     textPaint.textSize = 50f
        //     canvas.drawText("$seconds", 0f, 15f, textPaint)
        // }
        
        canvas.restore()
    }
    
    private fun drawMiniBlackHole(canvas: Canvas) {
        val size = radius * 0.5f
        val pulseScale = 1f + sin(animationFrame * 0.1f) * 0.1f
        
        canvas.save()
        canvas.scale(pulseScale, pulseScale)
        
        // Gradient hố đen
        for (i in 3 downTo 1) {
            val r = size * i / 3f
            val gradient = RadialGradient(
                0f, 0f, r,
                intArrayOf(
                    Color.argb(255, 20, 0, 40),
                    Color.argb(200, 100, 50, 150),
                    Color.argb(0, 200, 100, 255)
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            iconPaint.shader = gradient
            canvas.drawCircle(0f, 0f, r, iconPaint)
        }
        
        // Tâm đen
        iconPaint.shader = null
        iconPaint.color = Color.BLACK
        canvas.drawCircle(0f, 0f, size * 0.3f, iconPaint)
        
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
    
    // COOLDOWN METHODS - COMMENTED OUT
    // fun startCooldown() {
    //     cooldownTimer = cooldownDuration
    // }
    // 
    // fun isOnCooldown(): Boolean = cooldownTimer > 0
    // 
    // fun getCooldownProgress(): Float {
    //     return if (cooldownTimer > 0) {
    //         cooldownTimer.toFloat() / cooldownDuration
    //     } else {
    //         0f
    //     }
    // }
    
    /**
     * Đánh dấu skill đã được sử dụng -> Ẩn nút
     */
    fun markAsUsed() {
        isUsed = true
        isVisible = false
    }
    
    fun isReady(): Boolean = !isUsed && isVisible
}
