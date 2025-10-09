package com.example.laptrinhgame2d.items.skills

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Item skill Shield - Vật phẩm rơi ra khi đánh quái
 * Nhặt được sẽ unlock skill tạo khiên bảo vệ
 */
class ShieldSkillItem(
    private val context: Context,
    private var x: Float,
    private var y: Float
) {
    private var isPickedUp = false
    private var lifetime = 0
    private val maxLifetime = 900  // 15 giây (60 FPS)
    
    // Animation
    private var rotationAngle = 0f
    private var floatOffset = 0f
    private var pulseScale = 1f
    
    // Size
    private val baseSize = 50f
    
    // Paint
    private val shieldPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val edgePaint = Paint().apply {
        color = Color.argb(200, 100, 200, 255)
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    
    private val glowPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }
    
    private val energyPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    // Energy particles
    private data class EnergyParticle(
        var angle: Float,
        var distance: Float,
        var speed: Float,
        var size: Float
    )
    private val energyParticles = mutableListOf<EnergyParticle>()
    
    init {
        // Tạo energy particles xoay quanh
        for (i in 0 until 16) {
            energyParticles.add(
                EnergyParticle(
                    angle = i * 22.5f,
                    distance = 60f + (i % 4) * 10f,
                    speed = 2.5f + (i % 3) * 0.8f,
                    size = 3f + (i % 2) * 2f
                )
            )
        }
    }
    
    fun update() {
        if (isPickedUp) return
        
        lifetime++
        
        // Animation float lên xuống
        floatOffset = sin(lifetime * 0.05f) * 10f
        
        // Xoay
        rotationAngle += 2.5f
        if (rotationAngle >= 360f) rotationAngle = 0f
        
        // Pulse effect
        pulseScale = 1f + sin(lifetime * 0.1f) * 0.15f
        
        // Update energy particles
        energyParticles.forEach { particle ->
            particle.angle += particle.speed
            if (particle.angle >= 360f) particle.angle -= 360f
            
            // Orbital effect
            particle.distance = 60f + sin(lifetime * 0.06f + particle.angle) * 15f
        }
    }
    
    fun draw(canvas: Canvas) {
        if (isPickedUp) return
        
        canvas.save()
        canvas.translate(x, y + floatOffset)
        
        // Vẽ energy particles
        drawEnergyParticles(canvas)
        
        // Vẽ khiên
        canvas.save()
        canvas.scale(pulseScale, pulseScale)
        drawShield(canvas)
        canvas.restore()
        
        // Vẽ rotating glow
        drawGlowRings(canvas)
        
        canvas.restore()
    }
    
    private fun drawShield(canvas: Canvas) {
        val size = baseSize
        
        // Vẽ gradient background - màu xanh dương
        val gradient = RadialGradient(
            0f, 0f, size,
            intArrayOf(
                Color.argb(255, 100, 180, 255),  // Xanh sáng ở giữa
                Color.argb(200, 50, 120, 200),   // Xanh dương
                Color.argb(100, 30, 80, 150),    // Xanh đậm
                Color.argb(0, 20, 60, 120)       // Trong suốt
            ),
            floatArrayOf(0f, 0.3f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
        shieldPaint.shader = gradient
        canvas.drawCircle(0f, 0f, size, shieldPaint)
        
        // Vẽ hình khiên (hình chữ nhật bo góc)
        shieldPaint.shader = null
        shieldPaint.style = Paint.Style.FILL
        
        // Vẽ nhiều lớp khiên
        for (i in 3 downTo 1) {
            val scale = i / 3f
            val w = size * 0.7f * scale
            val h = size * 0.9f * scale
            val alpha = 150 + i * 30
            
            shieldPaint.color = Color.argb(alpha, 80, 160, 255)
            canvas.drawRoundRect(-w/2, -h/2, w/2, h/2, 10f, 10f, shieldPaint)
        }
        
        // Vẽ viền khiên
        shieldPaint.style = Paint.Style.STROKE
        shieldPaint.strokeWidth = 3f
        shieldPaint.color = Color.argb(255, 150, 220, 255)
        val w = size * 0.7f
        val h = size * 0.9f
        canvas.drawRoundRect(-w/2, -h/2, w/2, h/2, 10f, 10f, shieldPaint)
        
        // Vẽ dấu + ở giữa
        shieldPaint.strokeWidth = 5f
        shieldPaint.color = Color.argb(255, 200, 240, 255)
        canvas.drawLine(-size*0.25f, 0f, size*0.25f, 0f, shieldPaint)
        canvas.drawLine(0f, -size*0.3f, 0f, size*0.3f, shieldPaint)
        
        shieldPaint.style = Paint.Style.FILL
    }
    
    private fun drawGlowRings(canvas: Canvas) {
        // Vẽ 3 vòng sáng xoay quanh
        for (i in 0 until 3) {
            val angle = rotationAngle + i * 120f
            val radius = baseSize * 1.3f + i * 8f
            
            canvas.save()
            canvas.rotate(angle)
            
            glowPaint.color = Color.argb(130, 100, 200, 255)
            glowPaint.strokeWidth = 3f - i * 0.5f
            
            // Vẽ cung tròn
            canvas.drawArc(
                -radius, -radius, radius, radius,
                0f, 100f, false, glowPaint
            )
            
            canvas.restore()
        }
    }
    
    private fun drawEnergyParticles(canvas: Canvas) {
        energyParticles.forEach { particle ->
            val rad = Math.toRadians(particle.angle.toDouble())
            val px = (cos(rad) * particle.distance).toFloat()
            val py = (sin(rad) * particle.distance).toFloat()
            
            // Màu xanh dương sáng
            val alpha = (200 * (1f - particle.distance / 100f)).toInt().coerceIn(80, 220)
            energyPaint.color = Color.argb(alpha, 100, 200, 255)
            
            canvas.drawCircle(px, py, particle.size, energyPaint)
            
            // Thêm glow nhỏ
            energyPaint.color = Color.argb(alpha / 2, 150, 220, 255)
            canvas.drawCircle(px, py, particle.size * 1.5f, energyPaint)
        }
    }
    
    fun isCollidingWith(targetX: Float, targetY: Float, range: Float): Boolean {
        if (isPickedUp) return false
        
        val dx = targetX - x
        val dy = targetY - y
        val distance = sqrt(dx * dx + dy * dy)
        return distance < range + baseSize
    }
    
    fun pickup() {
        isPickedUp = true
    }
    
    fun isPickedUp(): Boolean = isPickedUp
    
    fun shouldBeRemoved(): Boolean {
        return isPickedUp || lifetime >= maxLifetime
    }
    
    fun getX(): Float = x
    fun getY(): Float = y
}
