package com.example.laptrinhgame2d.items.skills

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlin.math.sin

/**
 * Nút skill Laser Beam
 * Chỉ dùng 1 lần, sau đó ẩn nút
 */
class LaserBeamSkillButton(
    private val context: Context,
    private var x: Float,
    private var y: Float
) {
    // Cooldown system - COMMENTED OUT (dùng 1 lần thay vì cooldown)
    // private var isOnCooldown = false
    // private var cooldownTimer = 0
    // private val cooldownDuration = 900  // 15 giây (60 FPS)
    
    private var isUsed = false  // Đã dùng chưa
    private var isVisible = true  // Hiển thị hay ẩn
    
    private val size = 80f
    private val iconSize = 50f
    
    // Animation
    private var pulseScale = 1f
    private var rotationAngle = 0f
    
    // Paint
    private val bgPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val iconPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        textSize = 24f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    
    private val cooldownPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.argb(180, 0, 0, 0)
    }
    
    fun update() {
        // COOLDOWN LOGIC - COMMENTED OUT
        // if (isOnCooldown) {
        //     cooldownTimer++
        //     if (cooldownTimer >= cooldownDuration) {
        //         isOnCooldown = false
        //         cooldownTimer = 0
        //     }
        // }
        
        // Pulse animation khi ready (chưa dùng)
        if (!isUsed && isVisible) {
            pulseScale = 1f + sin(System.currentTimeMillis() * 0.005f) * 0.1f
        } else {
            pulseScale = 1f
        }
        
        // Rotation
        rotationAngle += 2f
        if (rotationAngle >= 360f) rotationAngle = 0f
    }
    
    fun draw(canvas: Canvas) {
        // Không vẽ nếu đã dùng hoặc bị ẩn
        if (isUsed || !isVisible) return
        
        canvas.save()
        canvas.translate(x, y)
        
        // Draw background
        drawBackground(canvas)
        
        // Draw laser icon
        canvas.save()
        canvas.scale(pulseScale, pulseScale)
        drawLaserIcon(canvas)
        canvas.restore()
        
        // COOLDOWN OVERLAY - COMMENTED OUT (không dùng cooldown nữa)
        // if (isOnCooldown) {
        //     drawCooldownOverlay(canvas)
        // }
        
        canvas.restore()
    }
    
    private fun drawBackground(canvas: Canvas) {
        // Luôn vẽ màu ready (vì chỉ show khi chưa dùng)
        val gradient = RadialGradient(
            0f, 0f, size,
            intArrayOf(
                Color.argb(200, 255, 100, 100),  // Đỏ
                Color.argb(150, 200, 50, 50)
            ),
            floatArrayOf(0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        
        bgPaint.shader = gradient
        canvas.drawCircle(0f, 0f, size, bgPaint)
        
        // Border
        bgPaint.shader = null
        bgPaint.style = Paint.Style.STROKE
        bgPaint.strokeWidth = 4f
        bgPaint.color = Color.argb(255, 255, 255, 255)
        canvas.drawCircle(0f, 0f, size - 2f, bgPaint)
        bgPaint.style = Paint.Style.FILL
    }
    
    private fun drawLaserIcon(canvas: Canvas) {
        canvas.save()
        canvas.rotate(rotationAngle)
        
        // Vẽ core (tâm trắng)
        iconPaint.color = Color.WHITE
        canvas.drawCircle(0f, 0f, iconSize * 0.2f, iconPaint)
        
        // Vẽ 4 laser beams quay
        for (i in 0 until 4) {
            canvas.save()
            canvas.rotate(i * 90f)
            
            // Gradient trắng -> đỏ
            val beamGradient = RadialGradient(
                iconSize * 0.4f, 0f, iconSize * 0.3f,
                intArrayOf(
                    Color.WHITE,
                    Color.argb(255, 255, 150, 150),
                    Color.argb(200, 255, 100, 100)
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            iconPaint.shader = beamGradient
            
            canvas.drawRect(
                iconSize * 0.15f, -4f,
                iconSize * 0.6f, 4f,
                iconPaint
            )
            
            canvas.restore()
        }
        
        iconPaint.shader = null
        canvas.restore()
    }
    
    // COOLDOWN OVERLAY FUNCTION - COMMENTED OUT (không dùng nữa)
    // private fun drawCooldownOverlay(canvas: Canvas) {
    //     // Semi-transparent overlay
    //     canvas.drawCircle(0f, 0f, size, cooldownPaint)
    //     
    //     // Cooldown text
    //     val remainingSeconds = ((cooldownDuration - cooldownTimer) / 60f).toInt() + 1
    //     textPaint.textSize = 36f
    //     canvas.drawText("$remainingSeconds", 0f, 12f, textPaint)
    //     
    //     textPaint.textSize = 18f
    //     canvas.drawText("s", 0f, 32f, textPaint)
    // }
    
    fun isPressed(touchX: Float, touchY: Float): Boolean {
        // Không thể bấm nếu đã dùng hoặc bị ẩn
        if (isUsed || !isVisible) return false
        
        val dx = touchX - x
        val dy = touchY - y
        return (dx * dx + dy * dy) <= (size * size)
    }
    
    fun onTouch(id: Int) {
        if (isUsed || !isVisible) return  // Không thể bấm nếu đã dùng
    }
    
    // COOLDOWN METHODS - COMMENTED OUT
    // fun startCooldown() {
    //     isOnCooldown = true
    //     cooldownTimer = 0
    // }
    
    /**
     * Đánh dấu skill đã được sử dụng -> Ẩn nút
     */
    fun markAsUsed() {
        isUsed = true
        isVisible = false
    }
    
    fun isReady(): Boolean = !isUsed && isVisible
    
    fun setPosition(newX: Float, newY: Float) {
        x = newX
        y = newY
    }
}
