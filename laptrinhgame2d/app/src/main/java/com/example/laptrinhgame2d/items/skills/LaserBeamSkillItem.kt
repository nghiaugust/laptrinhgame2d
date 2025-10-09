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
 * Item skill Laser Beam - Vật phẩm rơi ra khi đánh quái
 * Nhặt được sẽ unlock skill bắn laser vào quái gần nhất
 */
class LaserBeamSkillItem(
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
    private var glowIntensity = 0f
    
    // Size
    private val baseSize = 50f
    
    // Paint
    private val corePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val glowPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }
    
    private val beamPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    // Energy particles
    private data class EnergyParticle(
        var angle: Float,
        var distance: Float,
        var speed: Float,
        var alpha: Int
    )
    private val energyParticles = mutableListOf<EnergyParticle>()
    
    init {
        // Tạo energy particles
        for (i in 0 until 16) {
            energyParticles.add(
                EnergyParticle(
                    angle = i * 22.5f,
                    distance = 50f + (i % 4) * 10f,
                    speed = 2f + (i % 3) * 0.5f,
                    alpha = 150 + (i % 3) * 30
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
        rotationAngle += 3f
        if (rotationAngle >= 360f) rotationAngle = 0f
        
        // Pulse effect
        pulseScale = 1f + sin(lifetime * 0.1f) * 0.15f
        
        // Glow intensity
        glowIntensity = (sin(lifetime * 0.15f) * 0.5f + 0.5f)
        
        // Update energy particles
        energyParticles.forEach { particle ->
            particle.angle += particle.speed
            if (particle.angle >= 360f) particle.angle -= 360f
            
            // Pulse distance
            particle.distance = 50f + sin(lifetime * 0.08f + particle.angle) * 15f
        }
    }
    
    fun draw(canvas: Canvas) {
        if (isPickedUp) return
        
        canvas.save()
        canvas.translate(x, y + floatOffset)
        
        // Vẽ outer glow
        drawOuterGlow(canvas)
        
        // Vẽ energy particles
        drawEnergyParticles(canvas)
        
        // Vẽ core
        canvas.save()
        canvas.scale(pulseScale, pulseScale)
        drawLaserCore(canvas)
        canvas.restore()
        
        // Vẽ rotating beams
        drawRotatingBeams(canvas)
        
        canvas.restore()
    }
    
    private fun drawLaserCore(canvas: Canvas) {
        val size = baseSize
        
        // Gradient từ trắng -> đỏ
        val gradient = RadialGradient(
            0f, 0f, size,
            intArrayOf(
                Color.WHITE,                        // Trắng ở giữa
                Color.argb(255, 255, 200, 200),    // Trắng hồng
                Color.argb(255, 255, 100, 100),    // Hồng đỏ
                Color.argb(200, 255, 50, 50),      // Đỏ
                Color.argb(0, 255, 0, 0)           // Trong suốt
            ),
            floatArrayOf(0f, 0.3f, 0.6f, 0.8f, 1f),
            Shader.TileMode.CLAMP
        )
        corePaint.shader = gradient
        canvas.drawCircle(0f, 0f, size, corePaint)
        
        // Vẽ tâm sáng
        corePaint.shader = null
        corePaint.color = Color.WHITE
        canvas.drawCircle(0f, 0f, size * 0.4f, corePaint)
    }
    
    private fun drawOuterGlow(canvas: Canvas) {
        val glowSize = baseSize * 1.5f
        val alpha = (150 * glowIntensity).toInt()
        
        for (i in 1..3) {
            glowPaint.color = Color.argb(
                (alpha / i).coerceIn(0, 255),
                255, 100, 100
            )
            glowPaint.strokeWidth = (6f - i * 1.5f).coerceAtLeast(1f)
            canvas.drawCircle(0f, 0f, glowSize + i * 10f, glowPaint)
        }
    }
    
    private fun drawRotatingBeams(canvas: Canvas) {
        for (i in 0 until 4) {
            val angle = rotationAngle + i * 90f
            
            canvas.save()
            canvas.rotate(angle)
            
            // Vẽ beam nhỏ
            beamPaint.shader = RadialGradient(
                baseSize * 0.5f, 0f, baseSize * 0.8f,
                intArrayOf(
                    Color.argb(200, 255, 255, 255),
                    Color.argb(150, 255, 150, 150),
                    Color.argb(0, 255, 100, 100)
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            
            canvas.drawRect(
                baseSize * 0.3f, -5f,
                baseSize * 1.2f, 5f,
                beamPaint
            )
            
            canvas.restore()
        }
    }
    
    private fun drawEnergyParticles(canvas: Canvas) {
        energyParticles.forEach { particle ->
            val rad = Math.toRadians(particle.angle.toDouble())
            val px = (kotlin.math.cos(rad) * particle.distance).toFloat()
            val py = (kotlin.math.sin(rad) * particle.distance).toFloat()
            
            // Màu trắng -> đỏ
            val colorFactor = (particle.distance - 40f) / 30f
            val red = 255
            val green = (255 * (1f - colorFactor)).toInt().coerceIn(0, 255)
            val blue = green
            
            beamPaint.shader = null
            beamPaint.color = Color.argb(particle.alpha, red, green, blue)
            canvas.drawCircle(px, py, 4f, beamPaint)
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
