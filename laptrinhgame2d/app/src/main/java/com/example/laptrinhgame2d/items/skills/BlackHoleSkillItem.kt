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
 * Item skill Black Hole - Vật phẩm rơi ra khi đánh quái
 * Nhặt được sẽ unlock skill triệu hồi hố đen
 */
class BlackHoleSkillItem(
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
    private val blackHolePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val glowPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }
    
    private val particlePaint = Paint().apply {
        color = Color.argb(200, 200, 100, 255)
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    // Particles
    private data class Particle(
        var angle: Float,
        var distance: Float,
        var speed: Float
    )
    private val particles = mutableListOf<Particle>()
    
    init {
        // Tạo particles xoay quanh
        for (i in 0 until 12) {
            particles.add(
                Particle(
                    angle = i * 30f,
                    distance = 60f + (i % 3) * 15f,
                    speed = 2f + (i % 2) * 1f
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
        rotationAngle += 2f
        if (rotationAngle >= 360f) rotationAngle = 0f
        
        // Pulse effect
        pulseScale = 1f + sin(lifetime * 0.1f) * 0.15f
        
        // Update particles
        particles.forEach { particle ->
            particle.angle += particle.speed
            if (particle.angle >= 360f) particle.angle -= 360f
            
            // Spiral effect
            particle.distance = 60f + sin(lifetime * 0.05f + particle.angle) * 20f
        }
    }
    
    fun draw(canvas: Canvas) {
        if (isPickedUp) return
        
        canvas.save()
        canvas.translate(x, y + floatOffset)
        
        // Vẽ particles xoay quanh
        drawParticles(canvas)
        
        // Vẽ hố đen
        canvas.save()
        canvas.scale(pulseScale, pulseScale)
        drawBlackHole(canvas)
        canvas.restore()
        
        // Vẽ glow rings
        drawGlowRings(canvas)
        
        canvas.restore()
    }
    
    private fun drawBlackHole(canvas: Canvas) {
        val size = baseSize
        
        // Vẽ nhiều vòng tròn gradient tạo hiệu ứng hố đen
        for (i in 5 downTo 1) {
            val radius = size * i / 5f
            val gradient = RadialGradient(
                0f, 0f, radius,
                intArrayOf(
                    Color.argb(255, 50, 0, 80),      // Tím đậm ở giữa
                    Color.argb(200, 100, 50, 150),   // Tím nhạt
                    Color.argb(100, 150, 100, 200),  // Tím sáng
                    Color.argb(0, 200, 150, 255)     // Trong suốt ở ngoài
                ),
                floatArrayOf(0f, 0.3f, 0.7f, 1f),
                Shader.TileMode.CLAMP
            )
            blackHolePaint.shader = gradient
            canvas.drawCircle(0f, 0f, radius, blackHolePaint)
        }
        
        // Vẽ tâm hố đen (đen tuyền)
        blackHolePaint.shader = null
        blackHolePaint.color = Color.BLACK
        canvas.drawCircle(0f, 0f, size * 0.3f, blackHolePaint)
    }
    
    private fun drawGlowRings(canvas: Canvas) {
        // Vẽ 3 vòng sáng xoay quanh
        for (i in 0 until 3) {
            val angle = rotationAngle + i * 120f
            val radius = baseSize * 1.2f + i * 5f
            
            canvas.save()
            canvas.rotate(angle)
            
            glowPaint.color = Color.argb(150, 150, 100, 255)
            glowPaint.strokeWidth = 3f - i * 0.5f
            
            // Vẽ cung tròn
            canvas.drawArc(
                -radius, -radius, radius, radius,
                0f, 120f, false, glowPaint
            )
            
            canvas.restore()
        }
    }
    
    private fun drawParticles(canvas: Canvas) {
        particles.forEach { particle ->
            val rad = Math.toRadians(particle.angle.toDouble())
            val px = (kotlin.math.cos(rad) * particle.distance).toFloat()
            val py = (kotlin.math.sin(rad) * particle.distance).toFloat()
            
            // Độ sáng giảm dần theo khoảng cách
            val alpha = (200 * (1f - particle.distance / 100f)).toInt().coerceIn(50, 200)
            particlePaint.color = Color.argb(alpha, 200, 100, 255)
            
            canvas.drawCircle(px, py, 3f, particlePaint)
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
